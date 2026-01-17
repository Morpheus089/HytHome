package games.player.home.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import games.player.home.HomeManager;
import java.util.UUID;
import javax.annotation.Nonnull;

public class SetHomeCommand extends AbstractPlayerCommand {
    
    private final HomeManager homeManager;
    private final RequiredArg<String> homeNameArg;

    public SetHomeCommand(@Nonnull HomeManager homeManager) {
        super(
            "sethome", 
            "Set a home location. Usage: /sethome <name>"
        );
        this.homeManager = homeManager;
        
        this.homeNameArg = this.withRequiredArg(
            "name", 
            "The name for your home", 
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
            this.sendUsageError(commandContext, "Utilisation: /sethome <nom>");
            return;
        }
        
        homeName = homeName.toLowerCase();
        
        TransformComponent transformComponent = store.getComponent(
            ref, 
            TransformComponent.getComponentType()
        );
        
        if (transformComponent == null) {
            commandContext.sendMessage(
                Message.raw("§8------------------------------")
            );
            commandContext.sendMessage(
                Message.raw("§c[!] Erreur: Position introuvable")
            );
            commandContext.sendMessage(
                Message.raw("§8------------------------------")
            );
            return;
        }
        
        HeadRotation headRotation = store.getComponent(
            ref, 
            HeadRotation.getComponentType()
        );
        
        Vector3f rotation = headRotation != null 
            ? headRotation.getRotation() 
            : new Vector3f(0.0f, 0.0f, 0.0f);
        
        Vector3d position = transformComponent.getPosition();
        
        this.homeManager.setHome(
            playerUuid, 
            homeName, 
            world, 
            position, 
            rotation
        );
        
        commandContext.sendMessage(Message.raw("§8------------------------------"));
        commandContext.sendMessage(
            Message.raw("§a[✓] Home '§f" + homeName + "§a' cree avec succes")
        );
        commandContext.sendMessage(Message.raw("§8------------------------------"));
    }

    private void sendUsageError(@Nonnull CommandContext commandContext, @Nonnull String message) {
        commandContext.sendMessage(Message.raw("§8------------------------------"));
        commandContext.sendMessage(Message.raw("§c[!] " + message));
        commandContext.sendMessage(Message.raw("§8------------------------------"));
    }
}
