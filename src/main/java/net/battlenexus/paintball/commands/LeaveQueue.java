package net.battlenexus.paintball.commands;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.command.CommandSender;

public class LeaveQueue implements PBCommand {
    @Override
    public void executePlayer(PBPlayer player, String[] args) {
        boolean result = Paintball.INSTANCE.getGameService().leaveQueue(player);
        if (!result) {
            player.sendMessage("You are not in the queue!");
        } else {
            player.sendMessage("You have been removed from the queue!");
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("You must be in-game to use this command!");
    }

    @Override
    public String[] getNames() {
        return new String[] {
                "leave",
                "l"
        };
    }
}
