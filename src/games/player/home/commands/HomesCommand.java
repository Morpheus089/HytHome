package games.player.home.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import games.player.home.HomeManager;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public class HomesCommand extends AbstractPlayerCommand {
    
    private final HomeManager homeManager;

    public HomesCommand(@Nonnull HomeManager homeManager) {
        super("homes", "List all your homes. Usage: /homes");
        this.homeManager = homeManager;
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
            @Nonnull World world) {
        
        UUID playerUuid = playerRef.getUuid();
        
        List<String> homeNames = this.homeManager.getHomeNames(playerUuid);
        
        commandContext.sendMessage(Message.raw("§8------------------------------"));
        
        if (homeNames.isEmpty()) {
            commandContext.sendMessage(
                Message.raw("§cAucun home enregistre.")
            );
            commandContext.sendMessage(
                Message.raw("§7Utilise /sethome <nom> pour creer un home")
            );
            
            commandContext.sendMessage(
                Message.raw("§8------------------------------")
            );
            
            return;
        }
        
        commandContext.sendMessage(
            Message.raw("§a[✓] Liste de tes homes")
        );
        
        for (String homeName : homeNames) {
            HomeManager.HomeLocation location = this.homeManager.getHome(
                playerUuid, 
                homeName
            );
            
            String dimension = location != null 
                ? location.worldName() 
                : "Inconnu";
            
            commandContext.sendMessage(
                Message.raw("§7• §f" + homeName + " §8(" + dimension + ")")
            );
        }
        
        commandContext.sendMessage(Message.raw("§8------------------------------"));
    }
}
