package nl.bloneburg.furnace.listeners;

import nl.bloneburg.furnace.VirtualFurnace;
import nl.bloneburg.furnace.gui.VirtualFurnaceGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;

public class FurnaceListener implements Listener {

    private final VirtualFurnace plugin;

    public FurnaceListener(VirtualFurnace plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof VirtualFurnaceGUI gui)) return;

        int slot = event.getRawSlot();
        
        // Only allow interaction with slots 0, 1, 2 (input, fuel, output)
        if (slot >= 0 && slot < 9 && slot != VirtualFurnaceGUI.INPUT_SLOT 
            && slot != VirtualFurnaceGUI.FUEL_SLOT && slot != VirtualFurnaceGUI.OUTPUT_SLOT) {
            event.setCancelled(true);
            return;
        }

        // Output slot - only allow taking items out
        if (slot == VirtualFurnaceGUI.OUTPUT_SLOT) {
            if (event.isShiftClick() || event.getCursor() == null || event.getCursor().getType().isAir()) {
                // Allow taking out
            } else {
                event.setCancelled(true);
                return;
            }
        }

        // Save after click with a small delay
        plugin.getServer().getScheduler().runTaskLater(plugin, gui::saveItems, 1L);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof VirtualFurnaceGUI gui)) return;

        // Check if any dragged slots are invalid
        for (int slot : event.getRawSlots()) {
            if (slot >= 0 && slot < 9 && slot != VirtualFurnaceGUI.INPUT_SLOT 
                && slot != VirtualFurnaceGUI.FUEL_SLOT && slot != VirtualFurnaceGUI.OUTPUT_SLOT) {
                event.setCancelled(true);
                return;
            }
        }

        // Save after drag with a small delay
        plugin.getServer().getScheduler().runTaskLater(plugin, gui::saveItems, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        
        Inventory inv = event.getInventory();
        if (!(inv.getHolder() instanceof VirtualFurnaceGUI)) return;

        VirtualFurnaceGUI.closeGUI(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (VirtualFurnaceGUI.hasOpenGUI(player.getUniqueId())) {
            VirtualFurnaceGUI.closeGUI(player);
        }
    }
}
