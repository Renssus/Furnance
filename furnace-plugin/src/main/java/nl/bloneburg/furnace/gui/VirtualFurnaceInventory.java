package nl.bloneburg.furnace.gui;

import nl.bloneburg.furnace.VirtualFurnace;
import nl.bloneburg.furnace.storage.FurnaceData;
import nl.bloneburg.furnace.storage.FurnaceStorage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Furnace;
import org.bukkit.block.BlastFurnace;
import org.bukkit.block.Smoker;
import org.bukkit.entity.Player;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VirtualFurnaceInventory {

    private static final Map<UUID, VirtualFurnaceSession> activeSessions = new HashMap<>();

    public static void openFurnace(Player player, FurnaceData.FurnaceType type) {
        VirtualFurnace plugin = VirtualFurnace.getInstance();
        FurnaceStorage storage = plugin.getFurnaceStorage();
        FurnaceData data = storage.getFurnaceData(player.getUniqueId(), type);

        // Find a safe location to place temporary furnace block
        Location loc = findSafeLocation(player);
        if (loc == null) {
            player.sendMessage("§cKon geen veilige locatie vinden voor de oven!");
            return;
        }

        // Store original block
        Block block = loc.getBlock();
        Material originalMaterial = block.getType();
        BlockState originalState = block.getState();

        // Place the appropriate furnace type
        Material furnaceMaterial = getFurnaceMaterial(type);
        block.setType(furnaceMaterial);

        // Get the furnace block state
        BlockState state = block.getState();
        if (!(state instanceof Furnace furnaceState)) {
            block.setType(originalMaterial);
            player.sendMessage("§cEr is een fout opgetreden!");
            return;
        }

        // Load saved items into the furnace
        FurnaceInventory inv = furnaceState.getInventory();
        if (data.getInputItem() != null) {
            inv.setSmelting(data.getInputItem());
        }
        if (data.getFuelItem() != null) {
            inv.setFuel(data.getFuelItem());
        }
        if (data.getOutputItem() != null) {
            inv.setResult(data.getOutputItem());
        }

        // Set burn time and cook time
        furnaceState.setBurnTime((short) data.getBurnTime());
        furnaceState.setCookTime((short) data.getCookTime());
        furnaceState.update();

        // Create session
        VirtualFurnaceSession session = new VirtualFurnaceSession(
            player.getUniqueId(),
            type,
            loc,
            originalMaterial,
            originalState
        );
        activeSessions.put(player.getUniqueId(), session);

        // Open the furnace inventory
        player.openInventory(furnaceState.getInventory());
    }

    public static void closeSession(Player player) {
        VirtualFurnaceSession session = activeSessions.get(player.getUniqueId());
        if (session == null) return;

        VirtualFurnace plugin = VirtualFurnace.getInstance();
        FurnaceStorage storage = plugin.getFurnaceStorage();
        
        Block block = session.getLocation().getBlock();
        BlockState state = block.getState();

        if (state instanceof Furnace furnaceState) {
            // Save the furnace contents - CLONE items before block is destroyed
            FurnaceData data = storage.getFurnaceData(player.getUniqueId(), session.getType());
            FurnaceInventory inv = furnaceState.getInventory();

            // Clone items to prevent them being lost when block is removed
            ItemStack inputClone = inv.getSmelting() != null ? inv.getSmelting().clone() : null;
            ItemStack fuelClone = inv.getFuel() != null ? inv.getFuel().clone() : null;
            ItemStack outputClone = inv.getResult() != null ? inv.getResult().clone() : null;
            short burnTime = furnaceState.getBurnTime();
            short cookTime = furnaceState.getCookTime();

            // Now remove from active sessions
            activeSessions.remove(player.getUniqueId());

            // Restore original block
            block.setType(session.getOriginalMaterial());

            // Now save the cloned data
            data.setInputItem(inputClone);
            data.setFuelItem(fuelClone);
            data.setOutputItem(outputClone);
            data.setBurnTime(burnTime);
            data.setCookTime(cookTime);

            storage.saveFurnaceData(player.getUniqueId(), session.getType(), data);
        } else {
            // Remove session and restore the block if not a furnace
            activeSessions.remove(player.getUniqueId());
            block.setType(session.getOriginalMaterial());
        }
    }

    public static boolean hasActiveSession(UUID playerUUID) {
        return activeSessions.containsKey(playerUUID);
    }

    public static VirtualFurnaceSession getSession(UUID playerUUID) {
        return activeSessions.get(playerUUID);
    }

    public static boolean isVirtualFurnaceLocation(Location loc) {
        for (VirtualFurnaceSession session : activeSessions.values()) {
            if (session.getLocation().equals(loc)) {
                return true;
            }
        }
        return false;
    }

    private static Location findSafeLocation(Player player) {
        // Use a location high in the sky at player's location
        Location playerLoc = player.getLocation();
        return new Location(playerLoc.getWorld(), playerLoc.getBlockX(), 319, playerLoc.getBlockZ());
    }

    private static Material getFurnaceMaterial(FurnaceData.FurnaceType type) {
        return switch (type) {
            case FURNACE -> Material.FURNACE;
            case BLAST_FURNACE -> Material.BLAST_FURNACE;
            case SMOKER -> Material.SMOKER;
        };
    }

    public static class VirtualFurnaceSession {
        private final UUID playerUUID;
        private final FurnaceData.FurnaceType type;
        private final Location location;
        private final Material originalMaterial;
        private final BlockState originalState;

        public VirtualFurnaceSession(UUID playerUUID, FurnaceData.FurnaceType type, Location location, 
                                     Material originalMaterial, BlockState originalState) {
            this.playerUUID = playerUUID;
            this.type = type;
            this.location = location;
            this.originalMaterial = originalMaterial;
            this.originalState = originalState;
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public FurnaceData.FurnaceType getType() {
            return type;
        }

        public Location getLocation() {
            return location;
        }

        public Material getOriginalMaterial() {
            return originalMaterial;
        }

        public BlockState getOriginalState() {
            return originalState;
        }
    }
}
