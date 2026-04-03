package nl.bloneburg.furnace;

import nl.bloneburg.furnace.commands.FurnaceCommand;
import nl.bloneburg.furnace.commands.BlastFurnaceCommand;
import nl.bloneburg.furnace.commands.SmokerCommand;
import nl.bloneburg.furnace.listeners.FurnaceListener;
import nl.bloneburg.furnace.storage.FurnaceStorage;
import org.bukkit.plugin.java.JavaPlugin;

public class VirtualFurnace extends JavaPlugin {

    private static VirtualFurnace instance;
    private FurnaceStorage furnaceStorage;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize storage
        furnaceStorage = new FurnaceStorage(this);
        furnaceStorage.loadAll();
        
        // Register commands
        getCommand("furnace").setExecutor(new FurnaceCommand(this));
        getCommand("blastfurnace").setExecutor(new BlastFurnaceCommand(this));
        getCommand("smoker").setExecutor(new SmokerCommand(this));
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new FurnaceListener(this), this);
        
        getLogger().info("VirtualFurnace plugin enabled!");
    }

    @Override
    public void onDisable() {
        // Save all furnace data
        if (furnaceStorage != null) {
            furnaceStorage.saveAll();
        }
        getLogger().info("VirtualFurnace plugin disabled!");
    }

    public static VirtualFurnace getInstance() {
        return instance;
    }

    public FurnaceStorage getFurnaceStorage() {
        return furnaceStorage;
    }
}
