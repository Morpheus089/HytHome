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

public class HomesCommand
extends AbstractPlayerCommand {
    private final HomeManager homeManager;

    public HomesCommand(@Nonnull HomeManager homeManager) {
        super("homes", "List all your homes. Usage: /homes");
        this.homeManager = homeManager;
    }

    protected boolean canGeneratePermission() {
        return false;
    }

    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        UUID uUID = playerRef.getUuid();
        List<String> list = this.homeManager.getHomeNames(uUID);
        commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
        if (list.isEmpty()) {
            commandContext.sendMessage(Message.raw((String)"§cAucun home enregistre."));
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            return;
        }
        commandContext.sendMessage(Message.raw((String)"§a[✓] Liste de tes homes"));
        for (String homeName : list) {
            HomeManager.HomeLocation location = this.homeManager.getHome(uUID, homeName);
            String dimension = location != null ? location.worldName() : "Inconnu";
            commandContext.sendMessage(Message.raw((String)"§7• §f" + homeName + " §8(" + dimension + ")"));
        }
        commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
    }
}
