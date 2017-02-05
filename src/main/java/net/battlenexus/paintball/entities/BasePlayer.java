package net.battlenexus.paintball.entities;

import net.battlenexus.paintball.game.weapon.Weapon;

public interface BasePlayer {
    Team getCurrentTeam();

    Weapon getCurrentWeapon();
}