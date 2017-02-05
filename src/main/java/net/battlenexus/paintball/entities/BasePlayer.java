package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.game.weapon.Weapon;
import net.minecraft.server.v1_11_R1.EntityLiving;

public interface BasePlayer {
    Team getCurrentTeam();

    Weapon getCurrentWeapon();

    EntityLiving getNMSEntity();
}