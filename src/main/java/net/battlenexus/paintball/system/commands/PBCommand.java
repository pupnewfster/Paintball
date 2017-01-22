package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.command.CommandSender;

public interface PBCommand {
    void executePlayer(PBPlayer player, String[] args);

    void execute(CommandSender sender, String[] args);

    String[] getNames();
}