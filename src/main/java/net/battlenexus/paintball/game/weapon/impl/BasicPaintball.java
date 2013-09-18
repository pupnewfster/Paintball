package net.battlenexus.paintball.game.weapon.impl;

import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import org.bukkit.Material;

public class BasicPaintball extends AbstractWeapon {

    @Override
    public Material getBlueTeamMaterial() {
        return Material.ARROW;
    }

    @Override
    public Material getRedTeamMaterial() {
        return Material.ARROW;
    }

    @Override
    public Material getNormalMaterial() {
        return Material.ARROW;
    }

    @Override
    public String name() {
        return "Basic Paintball Gun";
    }

    @Override
    public int clipeSize() {
        return 20;
    }

    @Override
    public int startBullets() {
        return 100;
    }

    @Override
    public int strength() {
        return 1;
    }

    @Override
    public int reloadDelay() {
        return 10;
    }

    @Override
    public int getShotRate() {
        return 1;
    }

    @Override
    public Material getReloadItem() {
        return Material.FLINT;
    }
}
