package nl.bloneburg.furnace.listeners;

import nl.bloneburg.furnace.VirtualFurnace;
import nl.bloneburg.furnace.gui.VirtualFurnaceInventory;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.FurnaceInventory;

public class FurnaceListener implements Listener {

    private final VirtualFurnace plugin;

    public FurnaceListener(VirtualFurnace plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (!(event.getInventory() instanceof FurnaceInventory)) return;

        if (VirtualFurnaceInventory.hasActiveSession(player.getUniqueId())) {
            VirtualFurnaceInventory.closeSession(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (VirtualFurnaceInventory.hasActiveSession(player.getUniqueId())) {
            VirtualFurnaceInventory.closeSession(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Location loc = event.getBlock().getLocation();
        if (VirtualFurnaceInventory.isVirtualFurnaceLocation(loc)) {
            event.setCancelled(true);
        }
    }
}
