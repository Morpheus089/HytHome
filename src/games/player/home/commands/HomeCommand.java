package games.player.home.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import games.player.home.HomeManager;
import java.util.UUID;
import javax.annotation.Nonnull;

public class HomeCommand extends AbstractPlayerCommand {
    
    private final HomeManager homeManager;
    private final RequiredArg<String> homeNameArg;

    public HomeCommand(@Nonnull HomeManager homeManager) {
        super(
            "home", 
            "Teleport to your home. Usage: /home <name>"
        );
        this.homeManager = homeManager;
        
        this.homeNameArg = this.withRequiredArg(
            "name", 
            "The name of the home to teleport to", 
            ArgTypes.STRING
        );
    }

    @Override
    protected boolean canGeneratePermission() {
        return false;
    }

    @Override
    protected void execute(
            @Nonnull CommandContext commandContext,
            @Nonnull Store<EntityStore> store,
            @Nonnull Ref<EntityStore> ref,
            @Nonnull PlayerRef playerRef,
            @Nonnull World currentWorld) {
        
        UUID playerUuid = playerRef.getUuid();
        String homeName = commandContext.get(this.homeNameArg);
        
        if (homeName == null || homeName.isEmpty()) {
            this.sendUsageError(commandContext, "Utilisation: /home <nom>");
            commandContext.sendMessage(
                Message.raw("§7Utilise /homes pour voir tes homes")
            );
            return;
        }
        
        homeName = homeName.toLowerCase();
        
        HomeManager.HomeLocation homeLocation = this.homeManager.getHome(
            playerUuid, 
            homeName
        );
        
        if (homeLocation == null) {
            commandContext.sendMessage(
                Message.raw("§8------------------------------")
            );
            commandContext.sendMessage(
                Message.raw("§c[!] Home '§f" + homeName + "§c' introuvable")
            );
            commandContext.sendMessage(
                Message.raw("§7Utilise /homes pour voir tes homes")
            );
            commandContext.sendMessage(
                Message.raw("§8------------------------------")
            );
            return;
        }
        
        World targetWorld = Universe.get().getWorld(homeLocation.worldName());
        if (targetWorld == null) {
            targetWorld = currentWorld;
        }

        final World finalTargetWorld = targetWorld;
        final String finalHomeName = homeName;

        finalTargetWorld.execute(() -> {
            Vector3f bodyRotation = new Vector3f(0.0f, homeLocation.yaw(), 0.0f);
            Vector3f headRotation = homeLocation.toRotation();

            Teleport teleport = Teleport.createForPlayer(
                finalTargetWorld,
                homeLocation.toPosition(),
                bodyRotation
            );
            
            teleport.setHeadRotation(headRotation);

            store.addComponent(ref, Teleport.getComponentType(), teleport);
            
            commandContext.sendMessage(Message.raw("§8------------------------------"));
            commandContext.sendMessage(
                Message.raw("§a[✓] Teleportation vers '§f" + finalHomeName + "§a'")
            );
            commandContext.sendMessage(Message.raw("§8------------------------------"));
        });
    }

    private void sendUsageError(
            @Nonnull CommandContext commandContext, 
            @Nonnull String message) {
        commandContext.sendMessage(Message.raw("§8------------------------------"));
        commandContext.sendMessage(Message.raw("§c[!] " + message));
        commandContext.sendMessage(Message.raw("§8------------------------------"));
    }
}
