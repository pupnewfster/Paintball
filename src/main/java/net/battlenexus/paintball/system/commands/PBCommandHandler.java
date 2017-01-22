package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.system.commands.sign.PlaceSignCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PBCommandHandler implements CommandExecutor {
    private static final PBCommand[] COMMANDS = new PBCommand[]{
            new CreatePBMap(),
            new JoinQueue(),
            new LeaveQueue(),
            new PlaceSignCommand(),
            new Spectator()
    };

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        for (PBCommand cmd : COMMANDS) {
            for (String ss : cmd.getNames()) {
                if (ss.equalsIgnoreCase(command.getName())) {
                    String[] newargs;
                    if (strings.length > 0) {
                        newargs = new String[strings.length - 1];

                        if (newargs.length >= 2) {
                            System.arraycopy(strings, 0, newargs, 0, newargs.length);
                        } else if (newargs.length == 1) {
                            newargs[0] = strings[0];
                        }
                    } else {
                        newargs = new String[0];
                    }

                    if (commandSender instanceof Player) {
                        PBPlayer pb = PBPlayer.toPBPlayer((Player) commandSender);
                        cmd.executePlayer(pb, newargs);
                    } else {
                        cmd.execute(commandSender, newargs);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public PBCommand getCommand(String name) {
        for (PBCommand cmd : COMMANDS) {
            for (String ss : cmd.getNames()) {
                if (ss.equalsIgnoreCase(name))
                    return cmd;
            }
        }
        return null;
    }
}
