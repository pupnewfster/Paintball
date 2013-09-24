package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.GameService;
import org.bukkit.command.CommandSender;

public class JoinQueue implements PBCommand {
    @Override
    public void executePlayer(PBPlayer player, String[] args) {
        GameService service = Paintball.INSTANCE.getGameService();
        if (!service.isGameInProgress() || !service.canJoin()) {
            boolean result = service.joinNextGame(player);
            if (!result) {
                player.sendMessage("You are already in the queue!");
            } else {
                player.sendMessage("You have been added to the queue for the next game!");
            }
        } else {
            player.joinGame(service.getCurrentGame());
            player.unfreeze();
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("You must be in-game to use this command!");
    }

    @Override
    public String[] getNames() {
        return new String[] {
                "join",
                "j"
        };
    }
}
