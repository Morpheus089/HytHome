package games.player.home.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import games.player.home.HomeManager;
import java.util.UUID;
import javax.annotation.Nonnull;

public class SetHomeCommand
extends AbstractPlayerCommand {
    private final HomeManager homeManager;

    public SetHomeCommand(@Nonnull HomeManager homeManager) {
        super("sethome", "Set a home location. Usage: /sethome <name>");
        this.homeManager = homeManager;
        this.homeNameArg = this.withRequiredArg("name", "The name for your home", (ArgumentType)ArgTypes.STRING);
    }

    protected boolean canGeneratePermission() {
        return false;
    }

    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        UUID uUID = playerRef.getUuid();
        String string = (String)commandContext.get(this.homeNameArg);
        if (string == null || string.isEmpty()) {
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            commandContext.sendMessage(Message.raw((String)"§c[!] Utilisation: /sethome <nom>"));
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            return;
        }
        string = string.toLowerCase();
        TransformComponent transformComponent = (TransformComponent)store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) {
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            commandContext.sendMessage(Message.raw((String)"§c[!] Erreur: Position introuvable"));
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            return;
        }
        HeadRotation headRotation = (HeadRotation)store.getComponent(ref, HeadRotation.getComponentType());
        Vector3f vector3f = headRotation != null ? headRotation.getRotation() : new Vector3f(0.0f, 0.0f, 0.0f);
        Vector3d vector3d = transformComponent.getPosition();
        this.homeManager.setHome(uUID, string, world, vector3d, vector3f);
        commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
        commandContext.sendMessage(Message.raw((String)"§a[✓] Home '§f" + string + "§a' cree avec succes"));
        commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
    }

    private final RequiredArg<String> homeNameArg;
}
