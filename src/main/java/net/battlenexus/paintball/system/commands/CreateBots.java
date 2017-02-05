package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.GameService;
import org.bukkit.command.CommandSender;

public class CreateBots implements PBCommand {
    @Override
    public void executePlayer(PBPlayer player, String[] args) {
        GameService service = Paintball.INSTANCE.getGameService();
        if (!service.isGameInProgress() || !service.canJoin())
            player.sendMessage("Error: This command can only be used while in game");
        else { //This does not currently ensure they are ingame, but not going to bother checking as we will have autojoining
            for (Team t : GameService.getCurrentGame().getTeams())
                t.spawnAIPlayer();
            //todo make one on each team, later make that configurable in args
            //TODO Add a .opposite or .opponents to Team
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("You must be in-game to use this command!");
    }

    @Override
    public String[] getNames() {
        return new String[]{
                "createbots",
                "cbots"
        };
    }
}