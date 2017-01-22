package net.battlenexus.paintball.game.weapon.impl;

import net.battlenexus.paintball.game.weapon.AbstractWeapon;
import org.bukkit.Material;

public class MachineGun extends AbstractWeapon {
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
        return "Machine Gun";
    }

    @Override
    public int clipSize() {
        return 50;
    }

    @Override
    public int startBullets() {
        return 450;
    }

    @Override
    public int damage() {
        return 1;
    }

    @Override
    public float strength() {
        return 4;
    }

    @Override
    public int reloadDelay() {
        return 3;
    }

    @Override
    public int getShotRate() {
        return 1;
    }

    @Override
    public int getFireDelay() {
        return 0;
    }

    @Override
    public Material getReloadItem() {
        return Material.FLINT;
    }
}