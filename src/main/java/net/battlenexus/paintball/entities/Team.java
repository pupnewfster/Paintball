package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.game.config.Config;
import net.battlenexus.paintball.game.config.ConfigParser;
import org.bukkit.Location;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class Team implements ConfigParser {
    private ArrayList<PBPlayer> players = new ArrayList<PBPlayer>();
    private String team_name;
    private Location spawn;

    public Team(Team blue_team) {
        this.team_name = blue_team.team_name;
        this.spawn = new Location(blue_team.spawn.getWorld(), blue_team.spawn.getX(), blue_team.spawn.getY(), blue_team.spawn.getZ(), blue_team.spawn.getYaw(), blue_team.spawn.getPitch());
    }

    public Team() { }

    public void spawnPlayer(PBPlayer player) {
        if (!contains(player))
            return;
        player.getBukkitPlayer().teleport(spawn);
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
    }

    public void leaveTeam(PBPlayer player) {
        if (!contains(player))
            return;
        players.remove(player);
    }


    @Override
    public void parse(Config config, NodeList childNodes) {
        if (childNodes != null && childNodes.getLength() > 0) {
            for (int i = 0; i < childNodes.getLength(); i++) {
                Element item = (Element)childNodes.item(0);

            }
        }
    }

    @Override
    public void save(ArrayList<String> lines) {
    }
}
