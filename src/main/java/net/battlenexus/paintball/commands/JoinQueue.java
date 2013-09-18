package net.battlenexus.paintball.commands;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.command.CommandSender;

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
        sender.sendMessage("You must be in-game to use this command!");
    }

    @Override
    public String[] getNames() {
        return new String[] {
                "join"
        };
    }
}
