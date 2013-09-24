package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.command.CommandSender;

public interface PBCommand {
    public void executePlayer(PBPlayer player, String[] args);

    public void execute(CommandSender sender, String[] args);

    public String[] getNames();
}
