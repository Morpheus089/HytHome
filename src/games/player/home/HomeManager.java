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
import java.util.Collection;
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
    private final Map<UUID, Map<String, HomeLocation>> homes = new ConcurrentHashMap<UUID, Map<String, HomeLocation>>();

    public HomeManager(@Nonnull Path path, @Nonnull HytaleLogger hytaleLogger) {
        this.dataDirectory = path;
        this.logger = hytaleLogger;
        this.load();
    }

    private void load() {
        Path path = this.dataDirectory.resolve(HOMES_FILE);
        if (!Files.exists(path, new LinkOption[0])) {
            ((HytaleLogger.Api)this.logger.atInfo()).log("No existing homes file found, starting fresh.");
            return;
        }
        try {
            String string = Files.readString(path);
            Map map = (Map)GSON.fromJson(string, HOMES_TYPE);
            if (map != null) {
                for (Object entryObj : map.entrySet()) {
                    Map.Entry entry = (Map.Entry) entryObj;
                    try {
                        UUID uUID = UUID.fromString((String)entry.getKey());
                        ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
                        if (entry.getValue() != null) {
                            concurrentHashMap.putAll((Map)entry.getValue());
                        }
                        this.homes.put(uUID, concurrentHashMap);
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        ((HytaleLogger.Api)this.logger.atWarning()).log("Invalid UUID in homes file: %s", entry.getKey());
                    }
                }
                int n = this.homes.values().stream().mapToInt(Map::size).sum();
                ((HytaleLogger.Api)this.logger.atInfo()).log("Loaded %d homes for %d players from disk.", n, this.homes.size());
            }
        }
        catch (IOException iOException) {
            ((HytaleLogger.Api)this.logger.atSevere()).log("Failed to load homes from disk: %s", (Object)iOException.getMessage());
        }
    }

    public void save() {
        try {
            if (!Files.exists(this.dataDirectory, new LinkOption[0])) {
                Files.createDirectories(this.dataDirectory, new FileAttribute[0]);
            }
            HashMap<String, HashMap<String, HomeLocation>> hashMap = new HashMap<String, HashMap<String, HomeLocation>>();
            for (Map.Entry<UUID, Map<String, HomeLocation>> object2 : this.homes.entrySet()) {
                hashMap.put(object2.getKey().toString(), new HashMap<String, HomeLocation>(object2.getValue()));
            }
            Path path = this.dataDirectory.resolve(HOMES_FILE);
            String string = GSON.toJson(hashMap, HOMES_TYPE);
            Files.writeString(path, (CharSequence)string, new OpenOption[0]);
            ((HytaleLogger.Api)this.logger.atFine()).log("Saved homes to disk.");
        }
        catch (IOException iOException) {
            ((HytaleLogger.Api)this.logger.atSevere()).log("Failed to save homes to disk: %s", (Object)iOException.getMessage());
        }
    }

    public void setHome(@Nonnull UUID uUID, @Nonnull String string, @Nonnull World world, @Nonnull Vector3d vector3d, @Nonnull Vector3f vector3f) {
        HomeLocation homeLocation = new HomeLocation(world.getName(), vector3d.getX(), vector3d.getY(), vector3d.getZ(), vector3f.getYaw(), vector3f.getPitch());
        String string2 = string.toLowerCase();
        this.homes.computeIfAbsent(uUID, uUID2 -> new ConcurrentHashMap()).put(string2, homeLocation);
        this.save();
    }

    @Nullable
    public HomeLocation getHome(@Nonnull UUID uUID, @Nonnull String string) {
        Map<String, HomeLocation> map = this.homes.get(uUID);
        if (map == null) {
            return null;
        }
        return map.get(string.toLowerCase());
    }

    @Nullable
    public HomeLocation getHome(@Nonnull UUID uUID) {
        Map<String, HomeLocation> map = this.homes.get(uUID);
        if (map == null || map.isEmpty()) {
            return null;
        }
        HomeLocation homeLocation = map.get("home");
        if (homeLocation != null) {
            return homeLocation;
        }
        return map.values().iterator().next();
    }

    @Nullable
    public String getDefaultHomeName(@Nonnull UUID uUID) {
        Map<String, HomeLocation> map = this.homes.get(uUID);
        if (map == null || map.isEmpty()) {
            return null;
        }
        if (map.containsKey("home")) {
            return "home";
        }
        return map.keySet().iterator().next();
    }

    @Nonnull
    public Map<String, HomeLocation> getHomes(@Nonnull UUID uUID) {
        Map<String, HomeLocation> map = this.homes.get(uUID);
        if (map == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(map);
    }

    @Nonnull
    public List<String> getHomeNames(@Nonnull UUID uUID) {
        Map<String, HomeLocation> map = this.homes.get(uUID);
        if (map == null) {
            return Collections.emptyList();
        }
        List<String> list = new ArrayList<>(map.keySet());
        Collections.sort(list);
        return list;
    }

    public boolean hasHome(@Nonnull UUID uUID, @Nonnull String string) {
        Map<String, HomeLocation> map = this.homes.get(uUID);
        return map != null && map.containsKey(string.toLowerCase());
    }

    public boolean hasHome(@Nonnull UUID uUID) {
        Map<String, HomeLocation> map = this.homes.get(uUID);
        return map != null && !map.isEmpty();
    }

    public int getHomeCount(@Nonnull UUID uUID) {
        Map<String, HomeLocation> map = this.homes.get(uUID);
        return map != null ? map.size() : 0;
    }

    public boolean removeHome(@Nonnull UUID uUID, @Nonnull String string) {
        Map<String, HomeLocation> map = this.homes.get(uUID);
        if (map == null) {
            return false;
        }
        String string2 = string.toLowerCase();
        HomeLocation homeLocation = map.remove(string2);
        if (homeLocation != null) {
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

        public HomeLocation(@Nonnull String string, double d, double d2, double d3, float f, float f2) {
            this.worldName = string;
            this.x = d;
            this.y = d2;
            this.z = d3;
            this.yaw = f;
            this.pitch = f2;
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

        public Vector3d toPosition() {
            return new Vector3d(this.x, this.y, this.z);
        }

        public Vector3f toRotation() {
            return new Vector3f(this.pitch, this.yaw, 0.0f);
        }

        public String toString() {
            return String.format("HomeLocation{world=%s, x=%.1f, y=%.1f, z=%.1f}", this.worldName, this.x, this.y, this.z);
        }
    }
}
