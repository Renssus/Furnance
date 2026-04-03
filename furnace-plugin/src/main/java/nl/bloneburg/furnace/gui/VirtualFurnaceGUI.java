package nl.bloneburg.furnace.gui;

import nl.bloneburg.furnace.VirtualFurnace;
import nl.bloneburg.furnace.storage.FurnaceData;
import nl.bloneburg.furnace.storage.FurnaceStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualFurnaceGUI implements InventoryHolder {

    private static final Map<UUID, VirtualFurnaceGUI> openGUIs = new HashMap<>();
    
    private final Player player;
    private final FurnaceData.FurnaceType type;
    private final Inventory inventory;
    private final FurnaceData data;

    // Slot positions (mimic real furnace layout in a 9-slot inventory)
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    public VirtualFurnaceGUI(Player player, FurnaceData.FurnaceType type) {
        this.player = player;
        this.type = type;
        
        VirtualFurnace plugin = VirtualFurnace.getInstance();
        FurnaceStorage storage = plugin.getFurnaceStorage();
        this.data = storage.getFurnaceData(player.getUniqueId(), type);

        String title = switch (type) {
            case FURNACE -> "Virtuele Furnace";
            case BLAST_FURNACE -> "Virtuele Blast Furnace";
            case SMOKER -> "Virtuele Smoker";
        };

        this.inventory = Bukkit.createInventory(this, 9, Component.text(title));
        loadItems();
    }

    private void loadItems() {
        if (data.getInputItem() != null) {
            inventory.setItem(INPUT_SLOT, data.getInputItem().clone());
        }
        if (data.getFuelItem() != null) {
            inventory.setItem(FUEL_SLOT, data.getFuelItem().clone());
        }
        if (data.getOutputItem() != null) {
            inventory.setItem(OUTPUT_SLOT, data.getOutputItem().clone());
        }
    }

    public void saveItems() {
        ItemStack input = inventory.getItem(INPUT_SLOT);
        ItemStack fuel = inventory.getItem(FUEL_SLOT);
        ItemStack output = inventory.getItem(OUTPUT_SLOT);

        data.setInputItem(input != null ? input.clone() : null);
        data.setFuelItem(fuel != null ? fuel.clone() : null);
        data.setOutputItem(output != null ? output.clone() : null);

        VirtualFurnace plugin = VirtualFurnace.getInstance();
        plugin.getFurnaceStorage().saveFurnaceData(player.getUniqueId(), type, data);
    }

    public static void openFurnace(Player player, FurnaceData.FurnaceType type) {
        VirtualFurnaceGUI gui = new VirtualFurnaceGUI(player, type);
        openGUIs.put(player.getUniqueId(), gui);
        player.openInventory(gui.getInventory());
    }

    public static void closeGUI(Player player) {
        VirtualFurnaceGUI gui = openGUIs.remove(player.getUniqueId());
        if (gui != null) {
            gui.saveItems();
        }
    }

    public static boolean hasOpenGUI(UUID uuid) {
        return openGUIs.containsKey(uuid);
    }

    public static VirtualFurnaceGUI getOpenGUI(UUID uuid) {
        return openGUIs.get(uuid);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public FurnaceData.FurnaceType getType() {
        return type;
    }

    public FurnaceData getData() {
        return data;
    }

    public Player getPlayer() {
        return player;
    }
}
