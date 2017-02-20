package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.system.commands.sign.PlaceSignCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PBCommandHandler implements CommandExecutor {
    private static final PBCommand[] COMMANDS = {
            new CreatePBMap(),
            new JoinQueue(),
            new LeaveQueue(),
            new PlaceSignCommand(),
            new Spectator(),
            new CreateBots()
    };

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (PBCommand cmd : COMMANDS) {
            for (String ss : cmd.getNames()) {
                if (ss.equalsIgnoreCase(command.getName())) {
                    String[] newArgs;
                    if (strings.length > 0) {
                        newArgs = new String[strings.length - 1];
                        if (newArgs.length >= 2)
                            System.arraycopy(strings, 0, newArgs, 0, newArgs.length);
                        else if (newArgs.length == 1)
                            newArgs[0] = strings[0];
                    } else
                        newArgs = new String[0];

                    if (commandSender instanceof Player)
                        cmd.executePlayer(PBPlayer.toPBPlayer((Player) commandSender), newArgs);
                    else
                        cmd.execute(commandSender, newArgs);
                    return true;
                }
            }
        }
        return false;
    }

    public PBCommand getCommand(String name) {
        for (PBCommand cmd : COMMANDS)
            for (String ss : cmd.getNames())
                if (ss.equalsIgnoreCase(name))
                    return cmd;
        return null;
    }
}