package net.battlenexus.paintball.system.commands;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.game.GameService;
import net.battlenexus.paintball.system.inventory.impl.WeaponShopMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class JoinQueue implements PBCommand {
    @Override
    public void executePlayer(final PBPlayer player, final String[] args) {
        GameService service = Paintball.INSTANCE.getGameService();
        if (player.getCurrentWeapon() == null) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    WeaponShopMenu menu = new WeaponShopMenu(ChatColor.BOLD + "CHOOSE A WEAPON");
                    menu.displayInventory(player.getBukkitPlayer());
                    try {
                        menu.waitForClose();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    executePlayer(player, args);
                }
            }).start();
            return;
        }
        if (!service.isGameInProgress() || !service.canJoin()) {
            boolean result = service.joinNextGame(player);
            if (!result) {
                player.sendMessage("You are already in the queue!");
            } else {
                player.sendMessage("You have been added to the queue for the next game!");
            }
        } else {
            player.joinGame(service.getCurrentGame());
            if (!service.getCurrentGame().inCountdown()) {
                player.unfreeze();
            }
        }
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        sender.sendMessage("You must be in-game to use this command!");
    }

    @Override
    public String[] getNames() {
        return new String[]{
                "join",
                "j"
        };
    }
}
