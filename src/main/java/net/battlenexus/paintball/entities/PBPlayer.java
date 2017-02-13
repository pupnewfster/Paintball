package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.game.GameService;
import net.battlenexus.paintball.game.PaintballGame;
import net.battlenexus.paintball.game.items.AbstractItem;
import net.battlenexus.paintball.game.weapon.Weapon;
import net.minecraft.server.v1_11_R1.EntityLiving;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;

import java.util.HashMap;

public class PBPlayer implements BasePlayer {
    private static ItemStack[] lobby_items;
    private static HashMap<String, PBPlayer> players = new HashMap<>();
    private Player player;
    private int kills;
    private int deaths;
    private boolean isSpectating;
    private Weapon weapon;
    private double defaultMaxHealth = 20;
    private double maxHealth = 20;

    public HashMap<PBPlayer, Integer> kill_cache = new HashMap<>();

    private static void createLobbyItems() {
        if (lobby_items != null)
            return;
        lobby_items = new ItemStack[1];

        lobby_items[0] = AbstractItem.addGlow(new ItemStack(Material.EMERALD));
        ItemMeta meta = lobby_items[0].getItemMeta();
        meta.setDisplayName("Weapon Shop");
        lobby_items[0].setItemMeta(meta);
    }

    private PBPlayer(Player player) {
        this.player = player;
    }

    public Player getBukkitPlayer() {
        return player;
    }

    public Weapon getCurrentWeapon() {
        return weapon;
    }

    public EntityLiving getNMSEntity() {
        return ((CraftPlayer) getBukkitPlayer()).getHandle();
    }

    public void setWeapon(Weapon weapon) {
        if (this.weapon != null && !this.weapon.equals(weapon))
            this.weapon.removeBar();
        else
            weapon.refill();
        this.weapon = weapon;
        ItemStack item = Weapon.WeaponUtils.toItemStack(weapon);
        if (player.getInventory().contains(item))
            return;
        player.getInventory().addItem(item);

        if (player.getInventory().getItem(0) != item) {
            Inventory i = player.getInventory();
            int item_index = i.first(item);
            while (item_index == -1) {
                player.getInventory().addItem(item);
                item_index = i.first(item);
            }
            ItemStack toMove = i.getItem(0);
            i.clear(0);
            i.clear(item_index);
            i.setItem(0, item);
            i.setItem(item_index, toMove);
        }

    }

    /**
     * Creates a new {@link PBPlayer} for a player
     *
     * @param player Player the player you would like to find
     * @return PBPlayer
     */
    @SuppressWarnings("ConstantConditions")
    private static PBPlayer newPlayer(Player player) {
        PBPlayer pbPlayer = new PBPlayer(player);
        if (pbPlayer.hasSaveData())
            pbPlayer.load();
        players.put(player.getName(), pbPlayer);

        return pbPlayer;
    }

    public Team getCurrentTeam() {
        return GameService.getCurrentGame() != null ? GameService.getCurrentGame().getTeamForPlayer(this) : null;
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
        if (players.containsKey(player.getName()))
            return players.get(player.getName());
        else
            return newPlayer(player);
    }

    public void hit(BasePlayer shooter) {
        if (shooter.getCurrentTeam() != null) {
            if (shooter.getCurrentTeam().contains(this)) {
                if (shooter instanceof PBPlayer)
                    ((PBPlayer) shooter).sendMessage("Watch out! " + getBukkitPlayer().getDisplayName() + ChatColor.GRAY + " is on your team!");
            } else {
                if (shooter.getCurrentWeapon().isOneHitKill() || wouldDie(shooter.getCurrentWeapon().damage())) {
                    refillHealth();
                    kill(shooter);
                } else
                    damagePlayer(shooter.getCurrentWeapon().damage());
            }
        }
    }

    public boolean isInGame() {
        return GameService.getCurrentGame() != null && getCurrentTeam() != null && !GameService.getCurrentGame().hasEnded();
    }

    private void kill(final BasePlayer killer) {
        if (!isInGame())
            return;
        addDeath();
        if (killer != null && killer instanceof PBPlayer)
            ((PBPlayer) killer).addKill();
        getCurrentTeam().spawnPlayer(this, false);
        GameService.getCurrentGame().onPlayerKill(killer, this);
        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 40, 0));
    }

    public void showLobbyItems() {
        createLobbyItems();
        Inventory inventory = getBukkitPlayer().getInventory();
        for (int i = 0; i < lobby_items.length; i++) {
            int index = 8 - i;
            inventory.remove(inventory.getItem(index));
            inventory.setItem(index, lobby_items[i]);
        }
    }

    public void hideLobbyItems() {
        createLobbyItems();
        Inventory inventory = getBukkitPlayer().getInventory();
        for (int i = 0; i < lobby_items.length; i++)
            inventory.remove(inventory.getItem(8 - i));
    }

    private void save() {
        //TODO Save data
    }

    private boolean hasSaveData() {
        //TODO Check if save data is available
        return false;
    }

    private void load() {
        //TODO Load save data, if hasSaveData returns false, then don't do anything.
    }


    public void spectateGame(PaintballGame game) {
        player.setGameMode(GameMode.SPECTATOR);

        Vector firstTeam = game.getConfig().getBlueTeam().getSpawn(false).toVector();
        Vector secondTeam = game.getConfig().getRedTeam().getSpawn(false).toVector();

        Vector midpoint = firstTeam.midpoint(secondTeam);
        Location lmidpoint = midpoint.toLocation(game.getConfig().getBlueTeam().getSpawn(false).getWorld());
        while (lmidpoint.getBlock().getType() != Material.AIR)
            lmidpoint.add(0, 1, 0);

        player.teleport(lmidpoint);
        isSpectating = true;

    }

    public void stopSpectating() {
        if (!isSpectating)
            return;
        isSpectating = false; //TODO make them rejoin being in the game? Or are they out until the next round
        player.teleport(Paintball.INSTANCE.paintball_world.getSpawnLocation());
        player.setGameMode(GameMode.ADVENTURE);
    }

    public boolean isSpectating() {
        return isSpectating;
    }

    public void joinGame() {
        PaintballGame game = GameService.getCurrentGame();
        if (isInGame())
            leaveGame();
        hideLobbyItems();//TODO try disabling this see if it lets gun type change midgame if so will need to make it so that we can change clip size
        game.joinNextOpenTeam(this);
        game.onPlayerJoin(this);
        Player bukkitP = getBukkitPlayer();
        refillHealth();
        bukkitP.setFoodLevel(20);
        bukkitP.setSaturation(20);
        bukkitP.getInventory().setHelmet(getCurrentTeam().getHelmet());
        bukkitP.getInventory().setChestplate(getCurrentTeam().getChestplate());
        bukkitP.getInventory().setLeggings(getCurrentTeam().getLeggings());
        bukkitP.getInventory().setBoots(getCurrentTeam().getBoots());
        bukkitP.setCanPickupItems(false);
        Bukkit.getScheduler().runTask(Paintball.INSTANCE, () -> getBukkitPlayer().setGameMode(GameMode.ADVENTURE));
    }

    public void leaveGame() {
        if (!isInGame())
            return;
        PaintballGame game = GameService.getCurrentGame();
        if (!game.hasEnded() && !game.isEnding())
            game.onPlayerLeave(this);
        getCurrentTeam().leaveTeam(null);
        maxHealth = defaultMaxHealth;
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());
        clearInventory();
        if (getCurrentWeapon() != null) {
            weapon.setOneHitKill(false);
            setWeapon(weapon); //Give the player back there gun..
        }
        showLobbyItems();
        kill_cache.clear(); //Empty kills if they leave game or game ends
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setCanPickupItems(true);
        Bukkit.getScheduler().runTask(Paintball.INSTANCE, () -> player.teleport(Paintball.INSTANCE.paintball_world.getSpawnLocation()));
    }

    public void clearInventory() {
        player.getInventory().clear();
        player.getInventory().setHelmet(new ItemStack(Material.AIR));
        player.getInventory().setChestplate(new ItemStack(Material.AIR));
        player.getInventory().setLeggings(new ItemStack(Material.AIR));
        player.getInventory().setBoots(new ItemStack(Material.AIR));
    }

    private boolean wouldDie(int damage) {
        return getBukkitPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - damage <= 0;
    }

    private void damagePlayer(int damage) {
        getBukkitPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(getBukkitPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() - damage);
        getBukkitPlayer().setHealth(getBukkitPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
    }

    private void refillHealth() {
        getBukkitPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(maxHealth);
        getBukkitPlayer().setHealth(maxHealth);
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

    public void increaseMaxHealth(double halfHearts) {
        maxHealth += halfHearts;
        player.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(getBukkitPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() + halfHearts);
        if (player.getHealth() + halfHearts <= player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
            player.setHealth(player.getHealth() + halfHearts);
    }

    public void sendMessage(String s) {
        getBukkitPlayer().sendMessage(Paintball.formatMessage(s));
    }

    public void dispose() {
        save();

        players.remove(player.getName());

        if (isInGame())
            GameService.getCurrentGame().leaveGame(this);

        player = null;
        weapon = null;
    }
}