package net.battlenexus.paintball.game.config;

import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.game.config.impl.ArrayListOption;
import net.battlenexus.paintball.game.config.impl.LocationOption;
import net.battlenexus.paintball.game.config.impl.SpawnPointOption;
import org.bukkit.Location;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class MapConfig extends ReflectionConfig {
    @ConfigItem
    protected Team blue_team;
    @ConfigItem
    protected Team red_team;
    @ConfigItem
    protected String map_name;
    @ConfigItem
    protected Integer playerMax = 16;
    @ConfigItem
    protected ArrayListOption<LocationOption> chests = new ArrayListOption<>();
    @ConfigItem
    protected ArrayListOption<SpawnPointOption> spawns = new ArrayListOption<SpawnPointOption>();

    public MapConfig() {
        blue_team = new Team();
        red_team = new Team();
    }

    public MapConfig(MapConfig toClone) {
        if (toClone == null)
            throw new InvalidParameterException("toClone cannot be null!");
        this.map_name = toClone.map_name;
        this.blue_team = new Team(toClone.blue_team);
        this.red_team = new Team(toClone.red_team);
        this.chests = toClone.chests.clone();
    }

    public void addChest(Location l) {
        LocationOption lc = new LocationOption(l);
        chests.add(lc);
    }


    public void setMapName(String mapName) {
        this.map_name = mapName;
    }

    public void setTeamName(int team, String name) {
        if (team == 0)
            blue_team.setTeamName(name);
        else if (team == 1)
            red_team.setTeamName(name);
    }

    public void setTeamSpawn(int team, Location spawn) {
        if (team == 0)
            blue_team.setSpawn(spawn);
        else if (team == 1)
            red_team.setSpawn(spawn);
    }

    public Team getRedTeam() {
        return red_team;
    }

    public Team getBlueTeam() {
        return blue_team;
    }

    public String getMapName() {
        return map_name;
    }

    public int getPlayerMax() {
        return playerMax;
    }

    public void setPlayerMax(int playerMax) {
        this.playerMax = playerMax;
    }

    public ArrayListOption<LocationOption> getChests() {
        return chests;
    }

    public ArrayListOption<SpawnPointOption> getSpawns() {
        return spawns;
    }

    public void setSpawns(ArrayListOption<SpawnPointOption> spawns) {
        this.spawns = spawns;
    }

    public List<SpawnPointOption> getSpawnsFor(Team team) {
        return getSpawnsFor(team, false, false);
    }

    public List<SpawnPointOption> getSpawnsFor(Team team, boolean aiSpawn) {
        return getSpawnsFor(team, aiSpawn, false);
    }

    public List<SpawnPointOption> getSpawnsFor(Team team, boolean aiSpawn, boolean startSpawn) {
        ArrayList<SpawnPointOption> list = new ArrayList<>();
        for (SpawnPointOption option : spawns) {
            if (aiSpawn == option.isAiSpawn() && startSpawn == option.isStartPoint() && option.getTeam() == team.getTeamNumber()) {
                list.add(option);
            }
        }

        return list;
    }
}