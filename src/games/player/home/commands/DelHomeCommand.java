package games.player.home.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import games.player.home.HomeManager;
import java.util.UUID;
import javax.annotation.Nonnull;

public class DelHomeCommand extends AbstractPlayerCommand {
    
    private final HomeManager homeManager;
    private final RequiredArg<String> homeNameArg;

    public DelHomeCommand(@Nonnull HomeManager homeManager) {
        super(
            "delhome", 
            "Delete a home. Usage: /delhome <name>"
        );
        this.homeManager = homeManager;
        
        this.homeNameArg = this.withRequiredArg(
            "name", 
            "The name of the home to delete", 
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
            @Nonnull World world) {
        
        UUID playerUuid = playerRef.getUuid();
        String homeName = commandContext.get(this.homeNameArg);
        
        if (homeName == null || homeName.isEmpty()) {
            this.sendUsageError(commandContext, "Utilisation: /delhome <nom>");
            return;
        }
        
        homeName = homeName.toLowerCase();
        
        if (!this.homeManager.hasHome(playerUuid, homeName)) {
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
        
        this.homeManager.removeHome(playerUuid, homeName);
        
        commandContext.sendMessage(Message.raw("§8------------------------------"));
        commandContext.sendMessage(
            Message.raw("§a[✓] Home '§f" + homeName + "§a' supprime avec succes")
        );
        commandContext.sendMessage(
            Message.raw("§7Pour creer un nouveau home, utilise /sethome <nom>")
        );
        commandContext.sendMessage(Message.raw("§8------------------------------"));
    }

    private void sendUsageError(
            @Nonnull CommandContext commandContext, 
            @Nonnull String message) {
        commandContext.sendMessage(Message.raw("§8------------------------------"));
        commandContext.sendMessage(Message.raw("§c[!] " + message));
        commandContext.sendMessage(Message.raw("§8------------------------------"));
    }
}
