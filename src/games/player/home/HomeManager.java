package games.player.home;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.World;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class HomeManager {
    
    private static final String HOMES_FILE = "homes.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type HOMES_TYPE = new TypeToken<Map<String, Map<String, HomeLocation>>>(){}.getType();
    
    private final Path dataDirectory;
    private final HytaleLogger logger;
    private final Map<UUID, Map<String, HomeLocation>> homes;

    public HomeManager(@Nonnull Path dataDirectory, @Nonnull HytaleLogger hytaleLogger) {
        this.dataDirectory = dataDirectory;
        this.logger = hytaleLogger;
        this.homes = new ConcurrentHashMap<>();
        this.load();
    }

    private void load() {
        Path homesFile = this.dataDirectory.resolve(HOMES_FILE);
        
        if (!Files.exists(homesFile, new LinkOption[0])) {
            this.logger.atInfo().log("No existing homes file found, starting fresh.");
            return;
        }
        
        try {
            String jsonContent = Files.readString(homesFile);
            Map<String, Map<String, HomeLocation>> loadedHomes = 
                GSON.fromJson(jsonContent, HOMES_TYPE);
            
            if (loadedHomes != null) {
                for (Map.Entry<String, Map<String, HomeLocation>> entry : loadedHomes.entrySet()) {
                    try {
                        UUID playerUuid = UUID.fromString(entry.getKey());
                        ConcurrentHashMap<String, HomeLocation> playerHomes = new ConcurrentHashMap<>();
                        
                        if (entry.getValue() != null) {
                            playerHomes.putAll(entry.getValue());
                        }
                        
                        this.homes.put(playerUuid, playerHomes);
                    } catch (IllegalArgumentException e) {
                        this.logger.atWarning().log("Invalid UUID in homes file: %s", entry.getKey());
                    }
                }
                
                int totalHomes = this.homes.values().stream()
                    .mapToInt(Map::size)
                    .sum();
                
                this.logger.atInfo().log(
                    "Loaded %d homes for %d players from disk.", 
                    totalHomes, 
                    this.homes.size()
                );
            }
        } catch (IOException e) {
            this.logger.atSevere().log(
                "Failed to load homes from disk: %s", 
                e.getMessage()
            );
        }
    }

    public void save() {
        try {
            if (!Files.exists(this.dataDirectory, new LinkOption[0])) {
                Files.createDirectories(this.dataDirectory, new FileAttribute[0]);
            }
            
            Map<String, Map<String, HomeLocation>> homesToSave = new HashMap<>();
            
            for (Map.Entry<UUID, Map<String, HomeLocation>> entry : this.homes.entrySet()) {
                homesToSave.put(
                    entry.getKey().toString(), 
                    new HashMap<>(entry.getValue())
                );
            }
            
            Path homesFile = this.dataDirectory.resolve(HOMES_FILE);
            String jsonContent = GSON.toJson(homesToSave, HOMES_TYPE);
            Files.writeString(homesFile, jsonContent, new OpenOption[0]);
            
            this.logger.atFine().log("Saved homes to disk.");
        } catch (IOException e) {
            this.logger.atSevere().log(
                "Failed to save homes to disk: %s", 
                e.getMessage()
            );
        }
    }

    public void setHome(
            @Nonnull UUID playerUuid, 
            @Nonnull String homeName, 
            @Nonnull World world, 
            @Nonnull Vector3d position, 
            @Nonnull Vector3f rotation) {
        
        HomeLocation homeLocation = new HomeLocation(
            world.getName(),
            position.getX(),
            position.getY(),
            position.getZ(),
            rotation.getYaw(),
            rotation.getPitch()
        );
        
        String normalizedName = homeName.toLowerCase();
        this.homes.computeIfAbsent(playerUuid, k -> new ConcurrentHashMap<>())
                  .put(normalizedName, homeLocation);
        
        this.save();
    }

    @Nullable
    public HomeLocation getHome(@Nonnull UUID playerUuid, @Nonnull String homeName) {
        Map<String, HomeLocation> playerHomes = this.homes.get(playerUuid);
        if (playerHomes == null) {
            return null;
        }
        return playerHomes.get(homeName.toLowerCase());
    }

    @Nullable
    public HomeLocation getHome(@Nonnull UUID playerUuid) {
        Map<String, HomeLocation> playerHomes = this.homes.get(playerUuid);
        if (playerHomes == null || playerHomes.isEmpty()) {
            return null;
        }
        
        HomeLocation defaultHome = playerHomes.get("home");
        if (defaultHome != null) {
            return defaultHome;
        }
        
        return playerHomes.values().iterator().next();
    }

    @Nullable
    public String getDefaultHomeName(@Nonnull UUID playerUuid) {
        Map<String, HomeLocation> playerHomes = this.homes.get(playerUuid);
        if (playerHomes == null || playerHomes.isEmpty()) {
            return null;
        }
        
        if (playerHomes.containsKey("home")) {
            return "home";
        }
        
        return playerHomes.keySet().iterator().next();
    }

    @Nonnull
    public Map<String, HomeLocation> getHomes(@Nonnull UUID playerUuid) {
        Map<String, HomeLocation> playerHomes = this.homes.get(playerUuid);
        if (playerHomes == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(playerHomes);
    }

    @Nonnull
    public List<String> getHomeNames(@Nonnull UUID playerUuid) {
        Map<String, HomeLocation> playerHomes = this.homes.get(playerUuid);
        if (playerHomes == null) {
            return Collections.emptyList();
        }
        
        List<String> homeNames = new ArrayList<>(playerHomes.keySet());
        Collections.sort(homeNames);
        return homeNames;
    }

    public boolean hasHome(@Nonnull UUID playerUuid, @Nonnull String homeName) {
        Map<String, HomeLocation> playerHomes = this.homes.get(playerUuid);
        return playerHomes != null && playerHomes.containsKey(homeName.toLowerCase());
    }

    public boolean hasHome(@Nonnull UUID playerUuid) {
        Map<String, HomeLocation> playerHomes = this.homes.get(playerUuid);
        return playerHomes != null && !playerHomes.isEmpty();
    }

    public int getHomeCount(@Nonnull UUID playerUuid) {
        Map<String, HomeLocation> playerHomes = this.homes.get(playerUuid);
        return playerHomes != null ? playerHomes.size() : 0;
    }

    public boolean removeHome(@Nonnull UUID playerUuid, @Nonnull String homeName) {
        Map<String, HomeLocation> playerHomes = this.homes.get(playerUuid);
        if (playerHomes == null) {
            return false;
        }
        
        String normalizedName = homeName.toLowerCase();
        HomeLocation removed = playerHomes.remove(normalizedName);
        
        if (removed != null) {
            this.save();
            return true;
        }
        return false;
    }

    public int getPlayerCount() {
        return this.homes.size();
    }

    public static final class HomeLocation {
        private final String worldName;
        private final double x;
        private final double y;
        private final double z;
        private final float yaw;
        private final float pitch;

        public HomeLocation(
                @Nonnull String worldName,
                double x,
                double y,
                double z,
                float yaw,
                float pitch) {
            this.worldName = worldName;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yaw = yaw;
            this.pitch = pitch;
        }

        @Nonnull
        public String worldName() {
            return this.worldName;
        }

        public double x() {
            return this.x;
        }

        public double y() {
            return this.y;
        }

        public double z() {
            return this.z;
        }

        public float yaw() {
            return this.yaw;
        }

        public float pitch() {
            return this.pitch;
        }

        @Nonnull
        public Vector3d toPosition() {
            return new Vector3d(this.x, this.y, this.z);
        }

        @Nonnull
        public Vector3f toRotation() {
            return new Vector3f(this.pitch, this.yaw, 0.0f);
        }

        @Override
        public String toString() {
            return String.format(
                "HomeLocation{world=%s, x=%.1f, y=%.1f, z=%.1f}",
                this.worldName, this.x, this.y, this.z
            );
        }
    }
}
