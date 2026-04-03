package nl.bloneburg.furnace.smelting;

import nl.bloneburg.furnace.VirtualFurnace;
import nl.bloneburg.furnace.storage.FurnaceData;
import nl.bloneburg.furnace.storage.FurnaceStorage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class SmeltingManager {

    private final VirtualFurnace plugin;
    private final FurnaceStorage storage;
    private final Map<String, SmeltingSession> activeSessions = new HashMap<>();

    public SmeltingManager(VirtualFurnace plugin, FurnaceStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        startSmeltingTask();
    }

    private void startSmeltingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                processAllFurnaces();
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every second (20 ticks)
    }

    private void processAllFurnaces() {
        for (UUID playerUUID : storage.getAllPlayerUUIDs()) {
            for (FurnaceData.FurnaceType type : FurnaceData.FurnaceType.values()) {
                FurnaceData data = storage.getFurnaceData(playerUUID, type);
                processFurnace(playerUUID, type, data);
            }
        }
    }

    private void processFurnace(UUID playerUUID, FurnaceData.FurnaceType type, FurnaceData data) {
        ItemStack input = data.getInputItem();
        ItemStack fuel = data.getFuelItem();
        ItemStack output = data.getOutputItem();

        // Check if we can smelt
        if (input == null || input.getType() == Material.AIR) {
            data.setBurnTime(Math.max(0, data.getBurnTime() - 1));
            data.setCookTime(0);
            return;
        }

        // Get the result for this input
        ItemStack result = getSmeltingResult(input.getType(), type);
        if (result == null) {
            return; // Not a smeltable item for this furnace type
        }

        // Check if output slot can accept the result
        if (output != null && output.getType() != Material.AIR) {
            if (output.getType() != result.getType() || output.getAmount() >= output.getMaxStackSize()) {
                return; // Output slot is full or incompatible
            }
        }

        // Process burn time
        if (data.getBurnTime() <= 0) {
            // Need to consume fuel
            if (fuel == null || fuel.getType() == Material.AIR) {
                data.setCookTime(0);
                return; // No fuel
            }

            int fuelTime = getFuelBurnTime(fuel.getType());
            if (fuelTime <= 0) {
                return; // Not a valid fuel
            }

            data.setBurnTime(fuelTime);
            
            // Consume one fuel
            if (fuel.getAmount() > 1) {
                fuel.setAmount(fuel.getAmount() - 1);
            } else {
                data.setFuelItem(null);
            }
        }

        // Decrease burn time
        data.setBurnTime(data.getBurnTime() - 1);

        // Increase cook time
        int cookTimeRequired = getCookTime(type);
        data.setCookTime(data.getCookTime() + 1);

        // Check if item is done smelting
        if (data.getCookTime() >= cookTimeRequired) {
            data.setCookTime(0);

            // Consume input
            if (input.getAmount() > 1) {
                input.setAmount(input.getAmount() - 1);
            } else {
                data.setInputItem(null);
            }

            // Add to output
            if (output == null || output.getType() == Material.AIR) {
                data.setOutputItem(result.clone());
            } else {
                output.setAmount(output.getAmount() + 1);
            }

            // Save changes
            storage.saveFurnaceData(playerUUID, type, data);
        }
    }

    private int getCookTime(FurnaceData.FurnaceType type) {
        return switch (type) {
            case FURNACE -> 10; // 10 seconds
            case BLAST_FURNACE, SMOKER -> 5; // 5 seconds (2x faster)
        };
    }

    private int getFuelBurnTime(Material fuel) {
        // Burn time in seconds
        return switch (fuel) {
            case COAL, CHARCOAL -> 80;
            case COAL_BLOCK -> 800;
            case LAVA_BUCKET -> 1000;
            case BLAZE_ROD -> 120;
            case OAK_LOG, SPRUCE_LOG, BIRCH_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG,
                 MANGROVE_LOG, CHERRY_LOG, CRIMSON_STEM, WARPED_STEM -> 15;
            case OAK_PLANKS, SPRUCE_PLANKS, BIRCH_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS,
                 DARK_OAK_PLANKS, MANGROVE_PLANKS, CHERRY_PLANKS, CRIMSON_PLANKS, WARPED_PLANKS -> 15;
            case STICK -> 5;
            case OAK_SLAB, SPRUCE_SLAB, BIRCH_SLAB, JUNGLE_SLAB, ACACIA_SLAB, DARK_OAK_SLAB -> 8;
            case BAMBOO -> 3;
            case DRIED_KELP_BLOCK -> 200;
            default -> 0;
        };
    }

    private ItemStack getSmeltingResult(Material input, FurnaceData.FurnaceType furnaceType) {
        // Blast furnace only smelts ores and metals
        if (furnaceType == FurnaceData.FurnaceType.BLAST_FURNACE) {
            return switch (input) {
                case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON -> new ItemStack(Material.IRON_INGOT);
                case GOLD_ORE, DEEPSLATE_GOLD_ORE, RAW_GOLD, NETHER_GOLD_ORE -> new ItemStack(Material.GOLD_INGOT);
                case COPPER_ORE, DEEPSLATE_COPPER_ORE, RAW_COPPER -> new ItemStack(Material.COPPER_INGOT);
                case ANCIENT_DEBRIS -> new ItemStack(Material.NETHERITE_SCRAP);
                case IRON_SWORD, IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_HOE,
                     IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS,
                     IRON_HORSE_ARMOR, CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE,
                     CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS -> new ItemStack(Material.IRON_NUGGET);
                case GOLD_SWORD, GOLD_PICKAXE, GOLD_AXE, GOLD_SHOVEL, GOLD_HOE,
                     GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS,
                     GOLDEN_HORSE_ARMOR -> new ItemStack(Material.GOLD_NUGGET);
                default -> null;
            };
        }

        // Smoker only cooks food
        if (furnaceType == FurnaceData.FurnaceType.SMOKER) {
            return switch (input) {
                case BEEF -> new ItemStack(Material.COOKED_BEEF);
                case PORKCHOP -> new ItemStack(Material.COOKED_PORKCHOP);
                case CHICKEN -> new ItemStack(Material.COOKED_CHICKEN);
                case COD -> new ItemStack(Material.COOKED_COD);
                case SALMON -> new ItemStack(Material.COOKED_SALMON);
                case MUTTON -> new ItemStack(Material.COOKED_MUTTON);
                case RABBIT -> new ItemStack(Material.COOKED_RABBIT);
                case POTATO -> new ItemStack(Material.BAKED_POTATO);
                case KELP -> new ItemStack(Material.DRIED_KELP);
                default -> null;
            };
        }

        // Regular furnace can smelt everything
        return switch (input) {
            // Ores
            case IRON_ORE, DEEPSLATE_IRON_ORE, RAW_IRON -> new ItemStack(Material.IRON_INGOT);
            case GOLD_ORE, DEEPSLATE_GOLD_ORE, RAW_GOLD, NETHER_GOLD_ORE -> new ItemStack(Material.GOLD_INGOT);
            case COPPER_ORE, DEEPSLATE_COPPER_ORE, RAW_COPPER -> new ItemStack(Material.COPPER_INGOT);
            case ANCIENT_DEBRIS -> new ItemStack(Material.NETHERITE_SCRAP);
            case DIAMOND_ORE, DEEPSLATE_DIAMOND_ORE -> new ItemStack(Material.DIAMOND);
            case LAPIS_ORE, DEEPSLATE_LAPIS_ORE -> new ItemStack(Material.LAPIS_LAZULI);
            case REDSTONE_ORE, DEEPSLATE_REDSTONE_ORE -> new ItemStack(Material.REDSTONE);
            case COAL_ORE, DEEPSLATE_COAL_ORE -> new ItemStack(Material.COAL);
            case EMERALD_ORE, DEEPSLATE_EMERALD_ORE -> new ItemStack(Material.EMERALD);
            case NETHER_QUARTZ_ORE -> new ItemStack(Material.QUARTZ);
            
            // Food
            case BEEF -> new ItemStack(Material.COOKED_BEEF);
            case PORKCHOP -> new ItemStack(Material.COOKED_PORKCHOP);
            case CHICKEN -> new ItemStack(Material.COOKED_CHICKEN);
            case COD -> new ItemStack(Material.COOKED_COD);
            case SALMON -> new ItemStack(Material.COOKED_SALMON);
            case MUTTON -> new ItemStack(Material.COOKED_MUTTON);
            case RABBIT -> new ItemStack(Material.COOKED_RABBIT);
            case POTATO -> new ItemStack(Material.BAKED_POTATO);
            case KELP -> new ItemStack(Material.DRIED_KELP);
            
            // Blocks
            case COBBLESTONE -> new ItemStack(Material.STONE);
            case STONE -> new ItemStack(Material.SMOOTH_STONE);
            case SAND -> new ItemStack(Material.GLASS);
            case RED_SAND -> new ItemStack(Material.GLASS);
            case SANDSTONE -> new ItemStack(Material.SMOOTH_SANDSTONE);
            case RED_SANDSTONE -> new ItemStack(Material.SMOOTH_RED_SANDSTONE);
            case CLAY_BALL -> new ItemStack(Material.BRICK);
            case CLAY -> new ItemStack(Material.TERRACOTTA);
            case NETHERRACK -> new ItemStack(Material.NETHER_BRICK);
            case STONE_BRICKS -> new ItemStack(Material.CRACKED_STONE_BRICKS);
            case BASALT -> new ItemStack(Material.SMOOTH_BASALT);
            case QUARTZ_BLOCK -> new ItemStack(Material.SMOOTH_QUARTZ);
            case CACTUS -> new ItemStack(Material.GREEN_DYE);
            case SEA_PICKLE -> new ItemStack(Material.LIME_DYE);
            case CHORUS_FRUIT -> new ItemStack(Material.POPPED_CHORUS_FRUIT);
            case WET_SPONGE -> new ItemStack(Material.SPONGE);
            
            // Metal recycling
            case IRON_SWORD, IRON_PICKAXE, IRON_AXE, IRON_SHOVEL, IRON_HOE,
                 IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS,
                 IRON_HORSE_ARMOR, CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE,
                 CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS -> new ItemStack(Material.IRON_NUGGET);
            case GOLDEN_SWORD, GOLDEN_PICKAXE, GOLDEN_AXE, GOLDEN_SHOVEL, GOLDEN_HOE,
                 GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS,
                 GOLDEN_HORSE_ARMOR -> new ItemStack(Material.GOLD_NUGGET);
            
            default -> null;
        };
    }

    public void shutdown() {
        // Save all furnace data on shutdown
        storage.saveAll();
    }
}
