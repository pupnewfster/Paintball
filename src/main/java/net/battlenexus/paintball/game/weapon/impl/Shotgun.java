package net.battlenexus.paintball.game.weapon.impl;

import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import org.bukkit.Material;

public class Shotgun extends AbstractWeapon {
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
        return "Shotgun";
    }

    @Override
    public int clipSize() {
        return 30;
    }

    @Override
    public int startBullets() {
        return 350;
    }

    @Override
    public int damage() {
        return 3;
    }

    @Override
    public void shoot() {
        super.shoot(2);
    }

    @Override
    public float strength() {
        return 2;
    }

    @Override
    public int reloadDelay() {
        return 4;
    }

    @Override
    public int getShotRate() {
        return 4;
    }

    @Override
    public int getFireDelay() {
        return 1000;
    }

    @Override
    public Material getReloadItem() {
        return Material.FLINT;
    }
}