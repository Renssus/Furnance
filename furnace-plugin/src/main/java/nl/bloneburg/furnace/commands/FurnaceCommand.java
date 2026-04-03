package nl.bloneburg.furnace.commands;

import nl.bloneburg.furnace.VirtualFurnace;
import nl.bloneburg.furnace.gui.VirtualFurnaceInventory;
import nl.bloneburg.furnace.storage.FurnaceData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FurnaceCommand implements CommandExecutor {

    private final VirtualFurnace plugin;

    public FurnaceCommand(VirtualFurnace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDit commando kan alleen door spelers worden gebruikt!");
            return true;
        }

        if (!player.hasPermission("furnace.use.furnace")) {
            player.sendMessage("§cJe hebt geen toestemming om dit commando te gebruiken!");
            return true;
        }

        VirtualFurnaceInventory.openFurnace(player, FurnaceData.FurnaceType.FURNACE);
        return true;
    }
}
