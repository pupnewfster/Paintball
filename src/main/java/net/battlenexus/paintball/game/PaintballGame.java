package net.battlenexus.paintball.game;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.BasePlayer;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.config.MapConfig;
import net.battlenexus.paintball.game.config.impl.LocationOption;
import net.battlenexus.paintball.game.items.AbstractItem;
import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import net.battlenexus.paintball.game.weapon.Weapon;
import net.battlenexus.paintball.system.ScoreManager;
import net.battlenexus.paintball.system.listeners.Tick;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public abstract class PaintballGame implements Tick {
    public static final Random RANDOM = new Random();

    private MapConfig mapConfig;
    protected ScoreManager score = new ScoreManager();
    boolean ended;
    protected boolean started;
    private static boolean restart;

    protected PaintballGame() {
        score.setupScoreboard(getGamemodeName(), getGamemodeName());
    }

    protected abstract String getGamemodeName();

    public abstract List<Class<? extends Weapon>> allowedGuns();//TODO make this work and potentially replace it with a WeaponType

    public void beginGame() {
        if (restart) {
            tryRestart();
            return;
        }

        Paintball.INSTANCE.getTicker().addTick(this);
        onGameStart();
        for (PBPlayer player : getAllPlayers()) {
            player.hideLobbyItems();
            if (player.getCurrentWeapon() != null) {
                player.setWeapon(player.getCurrentWeapon()); //ensure they have there own gun..
                player.getCurrentWeapon().addBullets(player.getCurrentWeapon().startBullets());
            }
        }
        refillChests(false);
        started = true;
        sendGameMessage(ChatColor.GREEN + "GO!");
    }

    private void tryRestart() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            try {
                Paintball.INSTANCE.changeServer(p);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, () -> {
                /*//Unloading world
                if (lastMap != null) {
                    Paintball.log("Unloading " + lastMap.getWorld().getName() + "..");
                    boolean success = Bukkit.unloadWorld(lastMap.getWorld(), false);
                    if (!success)
                        Paintball.log("Failed to unload last map! A manual unload may be required..");
                    else
                        restoreBackup(lastMap.getWorld());
                }*/
            //Restart
            //Should this use Bukkit.spigot().restart(); instead of will that cause issues
            Bukkit.getScheduler().scheduleSyncDelayedTask(Paintball.INSTANCE, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "restart"), 20);
        }, 20 * 2);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasStarted() {
        return started;
    }

    protected abstract void onGameStart();

    void setConfig(MapConfig map_config) {
        mapConfig = map_config;
    }

    public final MapConfig getConfig() {
        return mapConfig;
    }

    protected void sendGameMessage(String s) {
        PBPlayer[] players = getAllPlayers();
        for (PBPlayer p : players)
            p.sendMessage(s);
    }

    public void onPlayerKill(BasePlayer killer, BasePlayer victim) {
        announceKill(killer, victim);
        if (victim.getCurrentWeapon() != null)
            victim.getCurrentWeapon().addBullets(victim.getCurrentWeapon().clipSize());
    }

    private void refillChests(boolean announce) {
        final Random random = new Random();
        boolean refilled = false;
        for (LocationOption lc : getConfig().getChests()) {
            Block b = lc.getLocation().getBlock();
            if (b.getType() == Material.CHEST) {
                Chest c = (Chest) b.getState();
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

                            ItemStack item1 = AbstractItem.createItem(ii.get(index).getMaterial(), random.nextInt(100) + 20, random.nextInt(4));
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
        if (mapConfig.getBlueTeam().size() < mapConfig.getRedTeam().size())
            mapConfig.getBlueTeam().joinTeam(p);
        else if (mapConfig.getRedTeam().size() < mapConfig.getBlueTeam().size())
            mapConfig.getRedTeam().joinTeam(p);
        else {
            if (new Random().nextBoolean())
                mapConfig.getBlueTeam().joinTeam(p);
            else
                mapConfig.getRedTeam().joinTeam(p);
        }
        if (started && p.getCurrentWeapon() != null && p.getCurrentWeapon() instanceof AbstractWeapon) {
            p.setWeapon(p.getCurrentWeapon()); //ensure they have there own gun..
            p.getCurrentWeapon().addBullets(p.getCurrentWeapon().startBullets());
        }
    }

    public List<Team> getTeams() {
        return Arrays.asList(mapConfig.getBlueTeam(), mapConfig.getRedTeam());
    }

    public void leaveGame(PBPlayer p) {
        Team team = getTeamForPlayer(p);
        if (team != null)
            team.leaveTeam(p);
    }

    public Team getTeamForPlayer(PBPlayer p) {
        if (mapConfig.getRedTeam().contains(p))
            return mapConfig.getRedTeam();
        else if (mapConfig.getBlueTeam().contains(p))
            return mapConfig.getBlueTeam();
        else
            return null;
    }

    private PBPlayer[] getAllPlayers() {
        List<PBPlayer> players = new ArrayList<>();
        players.addAll(mapConfig.getBlueTeam().getAllPlayers());
        players.addAll(mapConfig.getRedTeam().getAllPlayers());
        return players.toArray(new PBPlayer[players.size()]);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean hasEnded() {
        return ended;
    }

    public void onPlayerJoin(PBPlayer player) {
        sendGameMessage(ChatColor.GREEN + "+ " + ChatColor.GRAY + player.getBukkitPlayer().getDisplayName() + ChatColor.GRAY + " has joined the game.");
    }

    public void onPlayerLeave(PBPlayer player) {
        sendGameMessage(ChatColor.DARK_RED + "- " + ChatColor.GRAY + player.getBukkitPlayer().getDisplayName() + ChatColor.GRAY + " has left the game.");
    }

    private boolean ending;

    public void endGame() {
        ending = true;
        PBPlayer[] players = getAllPlayers();
        if (score != null)
            for (Team t : getTeams()) {
                t.removeAIPlayers();
                score.removeTeam(t.getName());
            }
        for (PBPlayer player : players)
            try {
                player.leaveGame();
            } catch (Throwable t) {
                t.printStackTrace();
                Paintball.INSTANCE.error("Error removing player \"" + player.getBukkitPlayer().getName() + "\" from paintball game!");
            }
        ending = false;
        ended = true;
        Paintball.INSTANCE.getTicker().removeTick(this);
        _wakeup();
        if (restart) //Check on the ending if it is supposed to restart
            tryRestart();
        //else TODO check if no players and then suspend the game
    }

    public boolean isEnding() {
        return ending;
    }

    private void announceKill(BasePlayer baseKiller, BasePlayer baseVictim) {
        if (!(baseKiller instanceof PBPlayer) || !(baseVictim instanceof PBPlayer))
            return; //TODO actually do something in this case
        String message = "shot";
        PBPlayer killer = (PBPlayer) baseKiller;
        PBPlayer victim = (PBPlayer) baseVictim;
        if (!killer.kill_cache.containsKey(victim))
            killer.kill_cache.put(victim, 1);
        else if (victim.kill_cache.containsKey(killer)) {
            int kills = victim.kill_cache.get(killer);
            if (kills > 3)
                message = "took revenge on";
            victim.kill_cache.remove(killer);
        } else {
            Integer kills = killer.kill_cache.get(victim);
            kills++;
            killer.kill_cache.put(victim, kills);

            if (kills > 25)
                message = ChatColor.DARK_RED + "WONT STOP KILLING" + ChatColor.GRAY;
            else if (kills > 20)
                message = ChatColor.RED + "IS OWNING" + ChatColor.GRAY;
            else if (kills > 15)
                message = ChatColor.RED + "IS DOMINATING" + ChatColor.GRAY;
            else if (kills > 10)
                message = "is hunting down";
            else if (kills > 3)
                message = "is killing";
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
        for (Team t : getTeams())
            score.addTeam(t.getName());
    }

    public ScoreManager getScoreManager() {
        return score;
    }

    static void restartNextGame() {
        restart = true;
    }
}