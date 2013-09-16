package net.battlenexus.paintball.game.weapon;

import net.battlenexus.paintball.entities.PBPlayer;
import org.bukkit.Material;

public interface Weapon {
    public String name();

    public int clipeSize();
    
    public int startBullets();
    
    public int strength();
    
    public int reloadDelay();

    public int getShotRate();
    
    public Material getRedTeamMaterial();
    
    public Material getBlueTeamMaterial();
    
    public Material getReloadItem();

    public PBPlayer getOwner();
}
