package games.player.home.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import games.player.home.HomeManager;
import java.util.UUID;
import javax.annotation.Nonnull;

public class DelHomeCommand
extends AbstractPlayerCommand {
    private final HomeManager homeManager;

    public DelHomeCommand(@Nonnull HomeManager homeManager) {
        super("delhome", "Delete a home. Usage: /delhome <name>");
        this.homeManager = homeManager;
        this.homeNameArg = this.withRequiredArg("name", "The name of the home to delete", (ArgumentType)ArgTypes.STRING);
    }

    protected boolean canGeneratePermission() {
        return false;
    }

    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        UUID uUID = playerRef.getUuid();
        String string = (String)commandContext.get(this.homeNameArg);
        if (string == null || string.isEmpty()) {
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            commandContext.sendMessage(Message.raw((String)"§c[!] Utilisation: /delhome <nom>"));
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            return;
        }
        if (!this.homeManager.hasHome(uUID, string = string.toLowerCase())) {
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            commandContext.sendMessage(Message.raw((String)"§c[!] Home '§f" + string + "§c' introuvable"));
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            return;
        }
        this.homeManager.removeHome(uUID, string);
        commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
        commandContext.sendMessage(Message.raw((String)"§a[✓] Home '§f" + string + "§a' supprime avec succes"));
        commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
    }

    private final RequiredArg<String> homeNameArg;
}
