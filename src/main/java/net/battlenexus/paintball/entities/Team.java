package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.game.config.Config;
import net.battlenexus.paintball.game.config.ConfigParser;
import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import net.battlenexus.paintball.game.weapon.impl.Sniper;
import org.bukkit.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Team implements ConfigParser {
    private ArrayList<PBPlayer> players = new ArrayList<PBPlayer>();
    private String team_name;
    private Location spawn;
    private String world_name;

    public Team(Team blue_team) {
        this.team_name = blue_team.team_name;
        this.spawn = new Location(blue_team.spawn.getWorld(), blue_team.spawn.getX(), blue_team.spawn.getY(), blue_team.spawn.getZ(), blue_team.spawn.getYaw(), blue_team.spawn.getPitch());
    }

    public Team() { }

    public void setTeamName(String team) {
        //This automates colors for team names.
        //So Team Blue or Blue Team will automatically get a blue color.
        for (ChatColor c : ChatColor.values()) {
            if (team.toLowerCase().contains(c.name().toLowerCase())) {
                team = c + team;
                break;
            }
        }
        this.team_name = team;
    }

    public void setSpawn(Location location) {
        this.spawn = location;
    }


    public Location getSpawn() {
        return spawn;
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
        player.getBukkitPlayer().teleport(spawn);
    }

    public List<PBPlayer> getAllPlayers() {
        return Collections.unmodifiableList(players);
    }

    public boolean contains(PBPlayer player) {
        return players.contains(player);
    }

    public int size() {
        return players.size();
    }

    public void joinTeam(PBPlayer player) {
        players.add(player);
        spawnPlayer(player);
        player.setWeapon(AbstractWeapon.createWeapon(Sniper.class, player)); //TODO Temp code, remove
    }

    public void leaveTeam(PBPlayer player) {
        if (!contains(player))
            return;
        players.remove(player);
    }


    @Override
    public void parse(Config config, NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            double x = 0, y = 0, z = 0, yaw = 0, pitch = 0;
            for (int i = 0; i < childNodes.getLength(); i++) {
                if (!(childNodes.item(i) instanceof Element))
                    continue;
                Element item = (Element)childNodes.item(i);
                if (item.getNodeName().equals("name")) {
                    team_name = item.getFirstChild().getNodeValue().replaceAll("@", "" + ChatColor.COLOR_CHAR);
                } else if (item.getNodeName().equals("x")) {
                    x = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("y")) {
                    y = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("z")) {
                    z = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("yaw")) {
                    yaw = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("pitch")) {
                    pitch = Double.parseDouble(item.getFirstChild().getNodeValue());
                } else if (item.getNodeName().equals("world")) {
                    world_name = item.getFirstChild().getNodeValue();
                }
            }
            spawn = new Location(Paintball.INSTANCE.paintball_world, x, y, z, (float)yaw, (float)pitch); //Use lobby as spawn world until a player needs to be spawned
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
