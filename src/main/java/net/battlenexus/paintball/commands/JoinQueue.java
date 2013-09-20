package net.battlenexus.paintball.commands;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinQueue implements PBCommand {
    @Override
    public void executePlayer(PBPlayer player, String[] args) {
        boolean result = Paintball.INSTANCE.getGameService().joinNextGame(player);
        if (!result) {
            player.sendMessage("You are already in the queue!");
        } else {
            player.sendMessage("You have been added to the queue!");
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if(args.length != 1) {
            sender.sendMessage("You must be in-game to use this command!");
            return;
        }
        //Preforms the command if commandblock has /join @p so that the player just steps on pressure plate
        Player target = Bukkit.getPlayer(args[0]);
        if(target != null) {
            target.performCommand("join");
        }
    }

    @Override
    public String[] getNames() {
        return new String[] {
                "join"
        };
    }
}
