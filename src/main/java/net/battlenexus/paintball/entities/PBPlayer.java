package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.game.PaintballGame;
import net.battlenexus.paintball.game.weapon.Weapon;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class PBPlayer {
    private static HashMap<String, PBPlayer> players = new HashMap<String, PBPlayer>();
    private Player player;
    private boolean frozen;
    private Location frozen_location;
    private int kills;
    private int deaths;
    private Weapon weapon;
    private PaintballGame current_game;

    private PBPlayer(Player player) {
        this.player = player;
    }

    public Player getBukkitPlayer() {
        return player;
    }

    public Weapon getCurrentWeapon() {
        return weapon;
    }

    public void setWeapon(Weapon weapon) {
        if (this.weapon != null) {
            player.getInventory().clear(); //TODO Maybe dont clear inventory
        }
        this.weapon = weapon;
        ItemStack item = Weapon.WeaponUtils.toItemStack(weapon);
        ItemStack reloadItem = Weapon.WeaponUtils.createReloadItem(weapon);

        player.getInventory().addItem(item, reloadItem);

    }

    /**
     * Creates a new {@link PBPlayer} for a player
     *
     * @param player Player the player you would like to find
     * @return PBPlayer
     */
    public static PBPlayer newPlayer(Player player) {
        PBPlayer pbPlayer = new PBPlayer(player);
        players.put(player.getName(), pbPlayer);

        return pbPlayer;
    }

    public void setCurrentGame(PaintballGame game) {
        if (current_game != null) {
            current_game.leaveGame(this);
        }
        current_game = game;
    }

    public PaintballGame getCurrentGame() {
        return current_game;
    }

    public Team getCurrentTeam() {
        return current_game != null ? current_game.getTeamForPlayer(this) : null;
    }

    public void handleFrozen() {
        if (!frozen || frozen_location == null)
            return;
        player.teleport(frozen_location);
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void freeze() {
        frozen_location = player.getLocation();
        frozen = true;
    }

    public void unfreeze() {
        frozen = false;
        frozen_location = null;
    }

    /**
     * Finds and returns the {@link PBPlayer} for the given player
     *
     * @param player Player The player you would like to find
     * @return PBPlayer
     */
    public static PBPlayer getPlayer(Player player) {
        return PBPlayer.players.get(player.getName());
    }

    public static PBPlayer toPBPlayer(Player player) {
        if (players.containsKey(player.getName())) {
            return players.get(player.getName());
        } else {
            return newPlayer(player);
        }
    }

    public boolean isInGame() {
        return current_game != null;
    }

    public void kill(final PBPlayer killer) {
        player.damage(20D);
        addDeath();
        killer.addKill();
        if (!isInGame() || getCurrentTeam() == null) {
            return;
        }
        getCurrentTeam().spawnPlayer(this);
    }

    public void joinGame(PaintballGame game) {

    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public void addKill() {
        this.kills++;
    }

    public int getDeaths() {
        return deaths;
    }

    public void setDeaths(int deaths) {
        this.deaths = deaths;
    }

    public void addDeath() {
        this.deaths++;
    }
}