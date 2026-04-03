package nl.bloneburg.furnace.commands;

import nl.bloneburg.furnace.VirtualFurnace;
import nl.bloneburg.furnace.gui.VirtualFurnaceGUI;
import nl.bloneburg.furnace.storage.FurnaceData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BlastFurnaceCommand implements CommandExecutor {

    private final VirtualFurnace plugin;

    public BlastFurnaceCommand(VirtualFurnace plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cDit commando kan alleen door spelers worden gebruikt!");
            return true;
        }

        if (!player.hasPermission("furnace.use.blastfurnace")) {
            player.sendMessage("§cJe hebt geen toestemming om dit commando te gebruiken!");
            return true;
        }

        VirtualFurnaceGUI.openFurnace(player, FurnaceData.FurnaceType.BLAST_FURNACE);
        return true;
    }
}
