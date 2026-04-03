package nl.bloneburg.furnace.storage;

import org.bukkit.inventory.ItemStack;
import java.util.UUID;

public class FurnaceData {
    
    private UUID ownerUUID;
    private FurnaceType type;
    private ItemStack inputItem;
    private ItemStack fuelItem;
    private ItemStack outputItem;
    private int burnTime;
    private int cookTime;
    private int cookTimeTotal;

    public FurnaceData(UUID ownerUUID, FurnaceType type) {
        this.ownerUUID = ownerUUID;
        this.type = type;
        this.burnTime = 0;
        this.cookTime = 0;
        this.cookTimeTotal = 0;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public FurnaceType getType() {
        return type;
    }

    public ItemStack getInputItem() {
        return inputItem;
    }

    public void setInputItem(ItemStack inputItem) {
        this.inputItem = inputItem;
    }

    public ItemStack getFuelItem() {
        return fuelItem;
    }

    public void setFuelItem(ItemStack fuelItem) {
        this.fuelItem = fuelItem;
    }

    public ItemStack getOutputItem() {
        return outputItem;
    }

    public void setOutputItem(ItemStack outputItem) {
        this.outputItem = outputItem;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public void setBurnTime(int burnTime) {
        this.burnTime = burnTime;
    }

    public int getCookTime() {
        return cookTime;
    }

    public void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    public int getCookTimeTotal() {
        return cookTimeTotal;
    }

    public void setCookTimeTotal(int cookTimeTotal) {
        this.cookTimeTotal = cookTimeTotal;
    }

    public enum FurnaceType {
        FURNACE,
        BLAST_FURNACE,
        SMOKER
    }
}
