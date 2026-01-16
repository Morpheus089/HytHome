package games.player.home.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import games.player.home.HomeManager;
import java.util.UUID;
import javax.annotation.Nonnull;

public class HomeCommand
extends AbstractPlayerCommand {
    private final HomeManager homeManager;

    public HomeCommand(@Nonnull HomeManager homeManager) {
        super("home", "Teleport to your home. Usage: /home <name>");
        this.homeManager = homeManager;
        this.homeNameArg = this.withRequiredArg("name", "The name of the home to teleport to", (ArgumentType)ArgTypes.STRING);
    }

    protected boolean canGeneratePermission() {
        return false;
    }

    protected void execute(@Nonnull CommandContext commandContext, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world) {
        UUID uUID = playerRef.getUuid();
        String string = (String)commandContext.get(this.homeNameArg);
        if (string == null || string.isEmpty()) {
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            commandContext.sendMessage(Message.raw((String)"§c[!] Utilisation: /home <nom>"));
            commandContext.sendMessage(Message.raw((String)"§7Utilise /homes pour voir tes homes"));
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            return;
        }
        String homeName = string.toLowerCase();
        HomeManager.HomeLocation homeLocation = this.homeManager.getHome(uUID, homeName);
        if (homeLocation == null) {
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            commandContext.sendMessage(Message.raw((String)"§c[!] Home '§f" + homeName + "§c' introuvable"));
            commandContext.sendMessage(Message.raw((String)"§7Utilise /homes pour voir tes homes"));
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            return;
        }
        World world2 = Universe.get().getWorld(homeLocation.worldName());
        if (world2 == null) {
            world2 = world;
        }
        world2.execute(() -> {
            Vector3f vector3f = new Vector3f(0.0f, homeLocation.yaw(), 0.0f);
            Vector3f vector3f2 = homeLocation.toRotation();
            Teleport teleport = new Teleport(world, homeLocation.toPosition(), vector3f).withHeadRotation(vector3f2);
            store.addComponent(ref, Teleport.getComponentType(), teleport);
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
            commandContext.sendMessage(Message.raw((String)"§a[✓] Teleportation vers '§f" + homeName + "§a'"));
            commandContext.sendMessage(Message.raw((String)"§8------------------------------"));
        });
    }

    private final RequiredArg<String> homeNameArg;
}
