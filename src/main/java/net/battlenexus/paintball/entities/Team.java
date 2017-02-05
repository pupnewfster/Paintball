package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.entities.ai.AIPlayer;
import net.battlenexus.paintball.entities.ai.SimpleSkeleton;
import net.battlenexus.paintball.game.config.ConfigParser;
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

public class Team implements ConfigParser {
    private final ArrayList<PBPlayer> players = new ArrayList<>();
    private final ArrayList<AIPlayer> aiPlayers = new ArrayList<>(); //TODO remove ai players when round is over
    private String team_name;
    private Location spawn;
    private String world_name;

    public Team(Team blue_team) {
        this.team_name = blue_team.team_name;
        this.world_name = blue_team.world_name;
        this.spawn = new Location(blue_team.spawn.getWorld(), blue_team.spawn.getX(), blue_team.spawn.getY(), blue_team.spawn.getZ(), blue_team.spawn.getYaw(), blue_team.spawn.getPitch());
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

    public void setSpawn(Location location) {
        this.spawn = location;
    }


    public Location getSpawn() {
        if (world_name != null && !spawn.getWorld().getName().equals(world_name)) {
            World w = Bukkit.getServer().createWorld(new WorldCreator(world_name));
            if (w == null)
                return spawn;
            spawn.setWorld(w);
        }
        return spawn;
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
        meta.setColor(getColor()); //TODO not have to get it every time keep it stored
        item.setItemMeta(meta);
        return item;
    }

    private Color getColor() {
        Color c = getName().startsWith("" + ChatColor.COLOR_CHAR) ? getColor(getName().charAt(1)) : null;
        if (c == null)
            return Color.BLUE;//TODO potentially choose a different default color
        return c;
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

    public String getName() {
        return team_name;
    }

    public void spawnPlayer(PBPlayer player) {
        if (!contains(player))
            return;
        if (world_name != null && !spawn.getWorld().getName().equals(world_name)) {
            World w = Bukkit.getServer().createWorld(new WorldCreator(world_name));
            if (w == null) {
                player.getBukkitPlayer().sendMessage(Paintball.formatMessage("Could not find world \"" + world_name + "\"!"));
                return;
            }
            spawn.setWorld(w);
        }
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
        spawnPlayer(player);
        if (player.getCurrentWeapon() == null)
            player.setWeapon(AbstractWeapon.createWeapon(BasicPaintball.class, player));
        if (hasAIPlayers())
            this.aiPlayers.get(0).remove();
    }

    public void leaveTeam(PBPlayer player) {
        if (!contains(player))
            return;
        players.remove(player);
    }

    public void spawnAIPlayer() {
        this.aiPlayers.add(new SimpleSkeleton(this, getAISpawn()));
    }

    public void removeAIPlayers() {
        for (AIPlayer ai : this.aiPlayers)
            if (ai instanceof SimpleSkeleton)
                ai.remove();
    }

    public boolean hasAIPlayers() {
        return !this.aiPlayers.isEmpty();
    }

    public Location getAISpawn() {
        return getSpawn(); //TODO make it return a random ai spawn point
    }

    @Override
    public void parse(NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            double x = 0, y = 0, z = 0, yaw = 0, pitch = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element))
                    continue;
                Element item = (Element) childNodes.item(i);
                if (item.getNodeName().equals("name"))
                    team_name = item.getFirstChild().getNodeValue().replaceAll("@", "" + ChatColor.COLOR_CHAR);
                else if (item.getNodeName().equals("x"))
                    x = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("y"))
                    y = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("z"))
                    z = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("yaw"))
                    yaw = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("pitch"))
                    pitch = Double.parseDouble(item.getFirstChild().getNodeValue());
                else if (item.getNodeName().equals("world"))
                    world_name = item.getFirstChild().getNodeValue();
            }
            spawn = new Location(Paintball.INSTANCE.paintball_world, x, y, z, (float) yaw, (float) pitch); //Use lobby as spawn world until a player needs to be spawned
        }
    }

    @Override
    public void save(ArrayList<String> lines) {
        lines.add("<name>" + team_name.replaceAll("" + ChatColor.COLOR_CHAR, "@") + "</name>");
        lines.add("<x>" + spawn.getX() + "</x>");
        lines.add("<y>" + spawn.getY() + "</y>");
        lines.add("<z>" + spawn.getZ() + "</z>");
        lines.add("<pitch>" + spawn.getPitch() + "</pitch>");
        lines.add("<yaw>" + spawn.getYaw() + "</yaw>");
        lines.add("<world>" + spawn.getWorld().getName() + "</world>");
    }
}