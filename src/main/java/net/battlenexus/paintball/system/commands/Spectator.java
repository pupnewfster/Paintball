package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.command.CommandSender;

public class Spectator implements PBCommand {
    @Override
    public void executePlayer(PBPlayer player, String[] args) {
        if (Paintball.INSTANCE.getGameService().getCurrentGame() == null) {
            player.sendMessage("There are no current games to spectate!");
            return;
        }
        if (player.isInGame())
            player.leaveGame(player.getCurrentGame());

        if (player.isSpectating()) {
            player.stopSpectating();
            player.sendMessage("You have stopped spectating!");
        } else {
            player.spectateGame(Paintball.INSTANCE.getGameService().getCurrentGame());
            player.sendMessage("You have started spectating!");
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("You must be in-game to use this command!");
    }

    @Override
    public String[] getNames() {
        return new String[]{
                "spectate",
                "spec",
                "s"
        };
    }
}
