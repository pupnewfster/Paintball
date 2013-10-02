package net.battlenexus.paintball.game;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.config.MapConfig;
import net.battlenexus.paintball.game.config.impl.LocationConfig;
import net.battlenexus.paintball.game.items.AbstractItem;
import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import net.battlenexus.paintball.game.weapon.Weapon;
import net.battlenexus.paintball.system.ScoreManager;
import net.battlenexus.paintball.system.listeners.Tick;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class PaintballGame implements Tick {
    protected MapConfig mapConfig;
    protected ScoreManager score = new ScoreManager();
    protected boolean ended = false;
    protected boolean started = false;
    private boolean countdown = false;

    public PaintballGame() {
        score.setupScoreboard(getGamemodeName(), getGamemodeName());
    }

    public abstract String getGamemodeName();

    public void beginGame() {
        Paintball.INSTANCE.getTicker().addTick(this);
        onGameStart();
        countdown = true;
        for (int i = 20; i > 0; i--) {
            if (i > 15) {
                sendGameMessage("Game will start in: " + ChatColor.WHITE + i);
            } else if (i > 5) {
                sendGameMessage("Game will start in: " + ChatColor.YELLOW + i);
            } else {
                sendGameMessage("Game will start in: " + ChatColor.DARK_RED + i);
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        countdown = false;
        for (PBPlayer player : getAllPlayers()) {
            player.unfreeze();
            if (player.getCurrentWeapon() != null && player.getCurrentWeapon() instanceof AbstractWeapon) {
                player.getCurrentWeapon().emptyGun();
                player.setWeapon(player.getCurrentWeapon()); //ensure they have there own gun..
                ((AbstractWeapon)player.getCurrentWeapon()).addBullets(player.getCurrentWeapon().startBullets());
            }
        }
        refillChests(false);
        started = true;
        sendGameMessage(ChatColor.GREEN + "GO!");
    }

    public boolean hasStarted() {
        return started;
    }

    public boolean inCountdown() {
        return countdown;
    }

    public void showScore() {
        if(score != null) {
            score.showScoreboard();
        }
    }

    protected abstract void onGameStart();

    void setConfig(MapConfig map_config) {
        mapConfig = map_config;
    }

    public final MapConfig getConfig() {
        return mapConfig;
    }

    public void sendGameMessage(String s) {
        PBPlayer[] players = getAllPlayers();
        for (PBPlayer p : players) {
            p.sendMessage(s);
        }
    }

    public void onPlayerKill(PBPlayer killer, PBPlayer victim) {
        announceKill(killer, victim);
        if (killer.getCurrentWeapon() != null) {
            killer.getCurrentWeapon().addBullets(killer.getCurrentWeapon().clipeSize());
        }
    }

    public void refillChests(boolean announce) {
        final Random random = new Random();
        boolean refilled = false;
        for (LocationConfig lc : getConfig().getChests()) {
            Block b = lc.location.getBlock();
            if (b.getType() == Material.CHEST) {
                Chest c = (Chest)b.getState();
                Inventory i = c.getInventory();

                i.clear();

                int item_amount = random.nextInt(i.getSize() / 2);
                while (item_amount > 0) {
                    int type = random.nextInt(2);
                    switch (type) {
                        case 0:
                            int bullet_count = random.nextInt(100);
                            ItemStack item = Weapon.WeaponUtils.createReloadItem(Material.FLINT, bullet_count);
                            i.addItem(item);
                            refilled = true;
                            break;
                        case 1:
                            List<AbstractItem> ii = AbstractItem.getChestItems();
                            int index = random.nextInt(ii.size());

                            ItemStack item1 = AbstractItem.createItem(ii.get(index).getMaterial(), random.nextInt(100) + 20, random.nextInt(3) + 1);
                            i.addItem(item1);
                            refilled = true;
                            break;
                    }

                    item_amount--;
                }
            }
        }
        if (refilled && announce)
            sendGameMessage(ChatColor.GREEN + "All chests have been refilled!");
    }

    public void joinNextOpenTeam(PBPlayer p) {
        if (mapConfig.getBlueTeam().size() < mapConfig.getRedTeam().size()) {
            mapConfig.getBlueTeam().joinTeam(p);
        } else if (mapConfig.getRedTeam().size() < mapConfig.getBlueTeam().size()) {
            mapConfig.getRedTeam().joinTeam(p);
        } else {
            if (new Random().nextBoolean()) {
                mapConfig.getBlueTeam().joinTeam(p);
            } else {
                mapConfig.getRedTeam().joinTeam(p);
            }
        }
        if (countdown)
            p.freeze();
        else {
            if (p.getCurrentWeapon() != null && p.getCurrentWeapon() instanceof AbstractWeapon) {
                p.getCurrentWeapon().emptyGun();
                p.setWeapon(p.getCurrentWeapon()); //ensure they have there own gun..
                ((AbstractWeapon)p.getCurrentWeapon()).addBullets(p.getCurrentWeapon().startBullets());
            }
        }
    }

    public void leaveGame(PBPlayer p) {
        Team team = getTeamForPlayer(p);
        if (team != null) {
            team.leaveTeam(p);
        }
    }

    public Team getTeamForPlayer(PBPlayer p) {
        if (mapConfig.getRedTeam().contains(p))
            return mapConfig.getRedTeam();
        else if (mapConfig.getBlueTeam().contains(p))
            return mapConfig.getBlueTeam();
        else
            return null;
    }

    public PBPlayer[] getAllPlayers() {
        List<PBPlayer> players = new ArrayList<PBPlayer>();
        players.addAll(mapConfig.getBlueTeam().getAllPlayers());
        players.addAll(mapConfig.getRedTeam().getAllPlayers());

        return players.toArray(new PBPlayer[players.size()]);
    }

    public boolean hasEnded() {
        return ended;
    }

    public void onPlayerJoin(PBPlayer player) {
        sendGameMessage(ChatColor.GREEN + "+" + ChatColor.GRAY + player.getBukkitPlayer().getDisplayName() + ChatColor.GRAY + " has joined the game.");
    }

    public void onPlayerLeave(PBPlayer player) {
        sendGameMessage(ChatColor.DARK_RED + "-" + ChatColor.GRAY + player.getBukkitPlayer().getDisplayName() + ChatColor.GRAY + " has left the game.");
    }
    private boolean ending = false;
    protected void endGame() {
        ending = true;
        PBPlayer[] players = getAllPlayers();
        if(score != null) {
            score.hideScoreboard();
        }
        for (PBPlayer player : players) {
            try {
                player.leaveGame(this);
                if (score != null) {
                    score.hideScoreboardFor(player.getBukkitPlayer());
                }
            } catch (Throwable t) {
                t.printStackTrace();
                Paintball.INSTANCE.error("Error removing player \"" + player.getBukkitPlayer().getName() + "\" from paintball game!");
            }
        }
        ending = false;
        ended = true;
        Paintball.INSTANCE.getTicker().removeTick(this);
        _wakeup();
    }

    public boolean isEnding() {
        return ending;
    }

    protected void announceKill(PBPlayer killer, PBPlayer victim) {
        String message = "shot";
        if (killer == null) {
            sendGameMessage(victim.getBukkitPlayer().getDisplayName() + ChatColor.GRAY + " killed himself!");
            return;
        }
        if (!killer.kill_cache.containsKey(victim)) {
            killer.kill_cache.put(victim, 1);
        } else if (victim.kill_cache.containsKey(killer)) {
            int kills = victim.kill_cache.get(killer);
            if (kills > 3) {
                message = "took revenge on";
            }
            victim.kill_cache.remove(killer);
        } else {
            Integer kills = killer.kill_cache.get(victim);
            kills++;
            killer.kill_cache.put(victim, kills);

            if (kills > 25) {
                message = ChatColor.DARK_RED + "WONT STOP KILLING" + ChatColor.GRAY;
            } else if (kills > 20) {
                message = ChatColor.RED + "IS OWNING" + ChatColor.GRAY;
            } else if (kills > 15) {
                message = ChatColor.RED + "IS DOMINATING" + ChatColor.GRAY;
            } else if (kills > 10) {
                message = "is hunting down";
            } else if (kills > 3) {
                message = "is killing";
            }
        }

        sendGameMessage(killer.getBukkitPlayer().getDisplayName() + ChatColor.GRAY + " " + message + " " + victim.getBukkitPlayer().getDisplayName());


    }

    private synchronized void _wakeup() {
        super.notifyAll();
    }

    public synchronized void waitForEnd() throws InterruptedException {
        while (true) {
            if (ended)
                break;
            super.wait(0L);
        }
    }

    protected void setupScoreboard() {
        OfflinePlayer[] blue = new OfflinePlayer[1];
        OfflinePlayer[] red = new OfflinePlayer[1];
        blue[0] = Bukkit.getOfflinePlayer(getConfig().getBlueTeam().getName());
        red[0] = Bukkit.getOfflinePlayer(getConfig().getRedTeam().getName());
        score.addTeam(getConfig().getBlueTeam().getName(), blue);
        score.addTeam(getConfig().getRedTeam().getName(), red);
    }
}
