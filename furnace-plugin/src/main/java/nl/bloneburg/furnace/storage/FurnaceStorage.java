package nl.bloneburg.furnace.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.bloneburg.furnace.VirtualFurnace;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FurnaceStorage {

    private final VirtualFurnace plugin;
    private final File dataFolder;
    private final Gson gson;
    
    // Map<PlayerUUID, Map<FurnaceType, FurnaceData>>
    private final Map<UUID, Map<FurnaceData.FurnaceType, FurnaceData>> playerFurnaces;

    public FurnaceStorage(VirtualFurnace plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.playerFurnaces = new ConcurrentHashMap<>();
        
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    public FurnaceData getFurnaceData(UUID playerUUID, FurnaceData.FurnaceType type) {
        // Load player data if not in memory
        if (!playerFurnaces.containsKey(playerUUID)) {
            loadPlayer(playerUUID);
        }
        
        playerFurnaces.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>());
        return playerFurnaces.get(playerUUID).computeIfAbsent(type, t -> new FurnaceData(playerUUID, t));
    }

    public void saveFurnaceData(UUID playerUUID, FurnaceData.FurnaceType type, FurnaceData data) {
        playerFurnaces.computeIfAbsent(playerUUID, k -> new ConcurrentHashMap<>()).put(type, data);
        savePlayer(playerUUID);
    }

    public Set<UUID> getAllPlayerUUIDs() {
        // Load all players from disk
        Set<UUID> uuids = new HashSet<>(playerFurnaces.keySet());
        
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File file : files) {
                try {
                    String uuidStr = file.getName().replace(".json", "");
                    UUID uuid = UUID.fromString(uuidStr);
                    if (!playerFurnaces.containsKey(uuid)) {
                        loadPlayer(uuid);
                    }
                    uuids.add(uuid);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        
        return uuids;
    }

    public void loadAll() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return;

        for (File file : files) {
            try {
                String uuidStr = file.getName().replace(".json", "");
                UUID playerUUID = UUID.fromString(uuidStr);
                loadPlayer(playerUUID);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid player data file: " + file.getName());
            }
        }
        plugin.getLogger().info("Loaded " + playerFurnaces.size() + " player furnace data files.");
    }

    public void saveAll() {
        for (UUID playerUUID : playerFurnaces.keySet()) {
            savePlayer(playerUUID);
        }
        plugin.getLogger().info("Saved " + playerFurnaces.size() + " player furnace data files.");
    }

    private void loadPlayer(UUID playerUUID) {
        File file = new File(dataFolder, playerUUID.toString() + ".json");
        if (!file.exists()) {
            playerFurnaces.put(playerUUID, new ConcurrentHashMap<>());
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            Map<FurnaceData.FurnaceType, FurnaceData> furnaces = new ConcurrentHashMap<>();

            for (FurnaceData.FurnaceType type : FurnaceData.FurnaceType.values()) {
                String typeKey = type.name().toLowerCase();
                if (root.has(typeKey)) {
                    JsonObject furnaceObj = root.getAsJsonObject(typeKey);
                    FurnaceData data = new FurnaceData(playerUUID, type);
                    
                    if (furnaceObj.has("input") && !furnaceObj.get("input").isJsonNull()) {
                        String inputStr = furnaceObj.get("input").getAsString();
                        if (!inputStr.isEmpty()) {
                            data.setInputItem(deserializeItem(inputStr));
                        }
                    }
                    if (furnaceObj.has("fuel") && !furnaceObj.get("fuel").isJsonNull()) {
                        String fuelStr = furnaceObj.get("fuel").getAsString();
                        if (!fuelStr.isEmpty()) {
                            data.setFuelItem(deserializeItem(fuelStr));
                        }
                    }
                    if (furnaceObj.has("output") && !furnaceObj.get("output").isJsonNull()) {
                        String outputStr = furnaceObj.get("output").getAsString();
                        if (!outputStr.isEmpty()) {
                            data.setOutputItem(deserializeItem(outputStr));
                        }
                    }
                    if (furnaceObj.has("burnTime")) {
                        data.setBurnTime(furnaceObj.get("burnTime").getAsInt());
                    }
                    if (furnaceObj.has("cookTime")) {
                        data.setCookTime(furnaceObj.get("cookTime").getAsInt());
                    }
                    if (furnaceObj.has("cookTimeTotal")) {
                        data.setCookTimeTotal(furnaceObj.get("cookTimeTotal").getAsInt());
                    }
                    
                    furnaces.put(type, data);
                }
            }

            playerFurnaces.put(playerUUID, furnaces);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load player data for " + playerUUID + ": " + e.getMessage());
            e.printStackTrace();
            playerFurnaces.put(playerUUID, new ConcurrentHashMap<>());
        }
    }

    public void savePlayer(UUID playerUUID) {
        Map<FurnaceData.FurnaceType, FurnaceData> furnaces = playerFurnaces.get(playerUUID);
        if (furnaces == null || furnaces.isEmpty()) return;

        File file = new File(dataFolder, playerUUID.toString() + ".json");
        JsonObject root = new JsonObject();

        for (Map.Entry<FurnaceData.FurnaceType, FurnaceData> entry : furnaces.entrySet()) {
            FurnaceData data = entry.getValue();
            JsonObject furnaceObj = new JsonObject();
            
            String inputSerialized = data.getInputItem() != null ? serializeItem(data.getInputItem()) : "";
            String fuelSerialized = data.getFuelItem() != null ? serializeItem(data.getFuelItem()) : "";
            String outputSerialized = data.getOutputItem() != null ? serializeItem(data.getOutputItem()) : "";
            
            furnaceObj.addProperty("input", inputSerialized);
            furnaceObj.addProperty("fuel", fuelSerialized);
            furnaceObj.addProperty("output", outputSerialized);
            furnaceObj.addProperty("burnTime", data.getBurnTime());
            furnaceObj.addProperty("cookTime", data.getCookTime());
            furnaceObj.addProperty("cookTimeTotal", data.getCookTimeTotal());
            
            root.add(entry.getKey().name().toLowerCase(), furnaceObj);
        }

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(root, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + playerUUID + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String serializeItem(ItemStack item) {
        if (item == null) return "";
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(item);
            dataOutput.close();
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to serialize item: " + e.getMessage());
            return "";
        }
    }

    private ItemStack deserializeItem(String data) {
        if (data == null || data.isEmpty()) return null;
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack item = (ItemStack) dataInput.readObject();
            dataInput.close();
            return item;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deserialize item: " + e.getMessage());
            return null;
        }
    }
}
