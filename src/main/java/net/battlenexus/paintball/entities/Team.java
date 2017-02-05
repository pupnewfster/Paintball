package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.Paintball;
import net.battlenexus.paintball.game.GameService;
import net.battlenexus.paintball.game.config.ConfigOption;
import net.battlenexus.paintball.game.config.ConfigWriter;
import net.battlenexus.paintball.game.config.MapConfig;
import net.battlenexus.paintball.game.config.impl.GenericConfigParse;
import net.battlenexus.paintball.game.config.impl.LocationOption;
import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import net.battlenexus.paintball.game.weapon.impl.BasicPaintball;
import org.bukkit.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Team implements ConfigOption {
    private final ArrayList<PBPlayer> players = new ArrayList<>();
    private String team_name;
    private String world_name;
    private int teamNumber;

    public Team(Team blue_team) {
        this.team_name = blue_team.team_name;
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

    @Deprecated
    public void setSpawn(Location location) {
        //TODO Delete this and use MapConfig.addSpawn instead
    }

    @Deprecated
    public Location getSpawn() {
        MapConfig config = GameService.getCurrentGame().getConfig();
        return config.getSpawnsFor(this).get(0).getPosition().getLocation();
    }

    public String getName() {
        return team_name;
    }

    public void spawnPlayer(PBPlayer player) {
        if (!contains(player))
            return;
        Location spawn = getSpawn();
        //TODO Make it so it only chooses start spawns

        Bukkit.getScheduler().runTask(Paintball.INSTANCE, () -> player.getBukkitPlayer().teleport(spawn));
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
        if (player.getCurrentWeapon() == null)
            player.setWeapon(AbstractWeapon.createWeapon(BasicPaintball.class, player));
    }

    public void leaveTeam(PBPlayer player) {
        if (!contains(player))
            return;
        players.remove(player);
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
}