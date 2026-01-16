package games.player.home;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import games.player.home.commands.DelHomeCommand;
import games.player.home.commands.HomeCommand;
import games.player.home.commands.HomesCommand;
import games.player.home.commands.SetHomeCommand;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class HytHomePlugin
extends JavaPlugin {
    private HomeManager homeManager;

    public HytHomePlugin(@Nonnull JavaPluginInit javaPluginInit) {
        super(javaPluginInit);
    }

    protected void setup() {
        this.getLogger().at(Level.INFO).log("HytHome plugin is setting up...");
        this.homeManager = new HomeManager(this.getDataDirectory(), this.getLogger());
        this.getLogger().at(Level.INFO).log("HytHome setup complete!");
    }

    protected void start() {
        this.getLogger().at(Level.INFO).log("HytHome plugin is starting!");
        this.getCommandRegistry().registerCommand((AbstractCommand)new SetHomeCommand(this.homeManager));
        this.getCommandRegistry().registerCommand((AbstractCommand)new HomeCommand(this.homeManager));
        this.getCommandRegistry().registerCommand((AbstractCommand)new DelHomeCommand(this.homeManager));
        this.getCommandRegistry().registerCommand((AbstractCommand)new HomesCommand(this.homeManager));
        this.getLogger().at(Level.INFO).log("HytHome started successfully!");
        this.getLogger().at(Level.INFO).log("Commands available: /sethome, /home, /delhome, /homes");
    }

    protected void shutdown() {
        this.getLogger().at(Level.INFO).log("HytHome plugin is shutting down...");
        if (this.homeManager != null) {
            this.homeManager.save();
            this.getLogger().at(Level.INFO).log("Saved homes for %d players", this.homeManager.getPlayerCount());
        }
    }

    public HomeManager getHomeManager() {
        return this.homeManager;
    }
}
