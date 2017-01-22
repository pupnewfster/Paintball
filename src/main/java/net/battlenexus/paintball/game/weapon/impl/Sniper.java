package net.battlenexus.paintball.game.weapon.impl;

import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import org.bukkit.Material;

public class Sniper extends AbstractWeapon {
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
        return "Sniper";
    }

    @Override
    public int clipSize() {
        return 5;
    }

    @Override
    public int startBullets() {
        return 50;
    }

    @Override
    public int damage() {
        return 10;
    }

    @Override
    public float strength() {
        return 400;
    }

    @Override
    public int reloadDelay() {
        return 6;
    }

    @Override
    public int getShotRate() {
        return 1;
    }

    @Override
    public int getFireDelay() {
        return 5000;
    }

    @Override
    public Material getReloadItem() {
        return Material.FLINT;
    }
}