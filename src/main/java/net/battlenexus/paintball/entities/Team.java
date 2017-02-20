package net.battlenexus.paintball.entities;

import gg.galaxygaming.necessities.Necessities;
import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.ai.AIPlayer;
import net.battlenexus.paintball.entities.ai.SimpleSkeleton;
import net.battlenexus.paintball.game.GameService;
import net.battlenexus.paintball.game.PaintballGame;
import net.battlenexus.paintball.game.config.ConfigOption;
import net.battlenexus.paintball.game.config.ConfigWriter;
import net.battlenexus.paintball.game.config.MapConfig;
import net.battlenexus.paintball.game.config.impl.SpawnPointOption;
import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import net.battlenexus.paintball.game.weapon.impl.BasicPaintball;
import org.bukkit.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Team implements ConfigOption {
    private final ArrayList<PBPlayer> players = new ArrayList<>();
    private final ArrayList<AIPlayer> aiPlayers = new ArrayList<>(); //TODO remove ai players when round is over
    private String team_name;
    private String world_name;
    private int teamNumber;

    public Team(Team blue_team) {
        this.team_name = blue_team.team_name;
        this.teamNumber = blue_team.teamNumber;
        this.world_name = blue_team.world_name;
    }

    public Team() {
    }

    public void setTeamName(String team) {
        //This automates colors for team names.
        //So Team Blue or Blue Team will automatically get a blue color.
        for (ChatColor c : ChatColor.values())
            if (team.toLowerCase().contains(c.name().toLowerCase())) {
                team = c + team;
                break;
            }
        this.team_name = team;
    }

    public ItemStack getHelmet() {
        return setColors(Material.LEATHER_HELMET);
    }

    public ItemStack getChestplate() {
        return setColors(Material.LEATHER_CHESTPLATE);
    }

    public ItemStack getLeggings() {
        return setColors(Material.LEATHER_LEGGINGS);
    }

    public ItemStack getBoots() {
        return setColors(Material.LEATHER_BOOTS);
    }


    private ItemStack setColors(Material m) { //TODO only have to use this once per item instead of each time
        if (!m.equals(Material.LEATHER_HELMET) && !m.equals(Material.LEATHER_CHESTPLATE) && !m.equals(Material.LEATHER_LEGGINGS) && !m.equals(Material.LEATHER_BOOTS))
            return new ItemStack(m, 1);
        ItemStack item = new ItemStack(m, 1);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setUnbreakable(true);
        Color c = getName().startsWith("" + ChatColor.COLOR_CHAR) ? getColor(getName().charAt(1)) : null;
        if (c == null)
            c = Color.BLUE;//TODO potentially choose a different default color
        meta.setColor(c); //TODO not have to get it every time keep it stored
        item.setItemMeta(meta);
        return item;
    }

    public ChatColor getColor() {
        ChatColor c = getName().startsWith("" + ChatColor.COLOR_CHAR) ? ChatColor.getByChar(getName().charAt(1)) : null;
        return c == null ? ChatColor.BLUE : c; //TODO potentially choose a different default color
    }

    private Color getColor(char c) {
        ChatColor cColor = ChatColor.getByChar(c);
        if (cColor == null)
            return null; //Not a valid color
        switch (cColor) {
            case BLACK:
                return Color.BLACK;
            case DARK_BLUE:
                return Color.NAVY;
            case DARK_GREEN:
                return Color.GREEN;
            case DARK_AQUA:
                return Color.TEAL;
            case DARK_RED:
                return Color.MAROON;
            case DARK_PURPLE:
                return Color.PURPLE;
            case GOLD:
                return Color.ORANGE;
            case GRAY:
                return Color.SILVER;
            case DARK_GRAY:
                return Color.GRAY;
            case BLUE:
                return Color.BLUE;
            case GREEN:
                return Color.LIME;
            case AQUA:
                return Color.AQUA;
            case RED:
                return Color.RED;
            case LIGHT_PURPLE:
                return Color.FUCHSIA;
            case YELLOW:
                return Color.YELLOW;
            case WHITE:
                return Color.WHITE;
            default:
                return null;
        }
    }

    public Location getSpawn(boolean startSpawn) {
        MapConfig config = GameService.getCurrentGame().getConfig();
        List<SpawnPointOption> options = config.getSpawnsFor(this, false, startSpawn);
        return options.get(PaintballGame.RANDOM.nextInt(options.size())).getPosition().getLocation();
    }

    public String getName() {
        return team_name;
    }

    public void spawnPlayer(PBPlayer player, boolean startSpawn) {
        if (!contains(player))
            return;

        Location spawn = getSpawn(startSpawn);
        Bukkit.getScheduler().runTask(Paintball.INSTANCE, () -> player.getBukkitPlayer().teleport(spawn));
    }

    public List<PBPlayer> getAllPlayers() {
        return Collections.unmodifiableList(players);
    }

    public boolean contains(PBPlayer player) {
        return players.contains(player);
    }

    public boolean contains(AIPlayer player) {
        return aiPlayers.contains(player);
    }

    public int size() {
        return players.size();
    }

    public void joinTeam(PBPlayer player) {
        players.add(player);
        spawnPlayer(player, true);
        if (player.getBukkitPlayer() != null)
            Necessities.getUM().getUser(player.getBukkitPlayer().getUniqueId()).setPrefix("(" + ChatColor.translateAlternateColorCodes(ChatColor.COLOR_CHAR, getName()) + ChatColor.RESET + ") ");
        if (player.getCurrentWeapon() == null)
            player.setWeapon(AbstractWeapon.createWeapon(BasicPaintball.class, player));
        if (hasAIPlayers())
            this.aiPlayers.get(0).remove();
    }

    public void leaveTeam(PBPlayer player) {
        if (!contains(player))
            return;
        players.remove(player);
        if (player.getBukkitPlayer() != null)
            Necessities.getUM().getUser(player.getBukkitPlayer().getUniqueId()).setPrefix("");
    }

    public void spawnAIPlayer() {
        this.aiPlayers.add(new SimpleSkeleton(this, getAISpawn(true)));
    }

    public void removeAIPlayers() {
        for (AIPlayer ai : this.aiPlayers)
            if (ai instanceof SimpleSkeleton)
                ai.remove();
    }

    public boolean hasAIPlayers() {
        return !this.aiPlayers.isEmpty();
    }

    public Location getAISpawn(boolean startSpawn) {
        MapConfig config = GameService.getCurrentGame().getConfig();
        List<SpawnPointOption> options = config.getSpawnsFor(this, true, startSpawn);

        return options.get(PaintballGame.RANDOM.nextInt(options.size())).getPosition().getLocation();
    }

    @Override
    public void parse(NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element))
                    continue;
                Element item = (Element) childNodes.item(i);
                if (item.getNodeName().equals("name"))
                    team_name = item.getFirstChild().getNodeValue().replaceAll("@", "" + ChatColor.COLOR_CHAR);
                else if (item.getNodeName().equals("teamNumber"))
                    teamNumber = Integer.parseInt(item.getFirstChild().getNodeValue());
            }
        }
    }

    @Override
    public void save(ConfigWriter configWriter) {
        configWriter.addConfig("name", team_name.replaceAll("" + ChatColor.COLOR_CHAR, "@"));
        configWriter.addConfig("teamNumber", teamNumber);
    }

    public int getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = teamNumber;
    }
}