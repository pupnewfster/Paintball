package net.battlenexus.paintball.entities.ai;

import com.google.common.collect.Sets;
import net.battlenexus.paintball.entities.BasePlayer;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.entities.ai.pathfinder.PathfinderGoalTargetEnemyPlayer;
import net.battlenexus.paintball.entities.ai.pathfinder.PathfinderGoalTargetNearestEnemyAI;
import net.battlenexus.paintball.game.GameService;
import net.battlenexus.paintball.game.impl.OneHitMinute;
import net.battlenexus.paintball.game.weapon.Weapon;
import net.battlenexus.paintball.game.weapon.impl.Pistol;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SimpleSkeleton extends EntitySkeleton implements AIPlayer {
    private final Team team;
    private Weapon gun;
    private int maxHealth = 20;

    public SimpleSkeleton(Team team, Location spawn) {
        super(((CraftWorld) spawn.getWorld()).getHandle());
        this.setPosition(spawn.getX(), spawn.getY(), spawn.getZ());
        this.team = team;

        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(goalSelector, Sets.newLinkedHashSet());
            bField.set(targetSelector, Sets.newLinkedHashSet());
            cField.set(goalSelector, Sets.newLinkedHashSet());
            cField.set(targetSelector, Sets.newLinkedHashSet());
            //this code clears fields B, C. so right now the mob wont walk
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(5, new PathfinderGoalRandomStroll(this, 1.0D, 20));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));//TODO edit the goal to look at enemies
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(8, new PathfinderGoalArrowAttack(this, 1.0D, 20, 15.0F));
        this.targetSelector.a(1, new PathfinderGoalTargetEnemyPlayer(this));
        this.targetSelector.a(2, new PathfinderGoalTargetNearestEnemyAI(this, SimpleSkeleton.class));
        this.targetSelector.a(3, new PathfinderGoalHurtByTarget(this, false, new Class[0]));//TODO make an enemy only version
        //TODO add for all different AI types we make

        setName();
        setCustomNameVisible(true);
        setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(this.team.getHelmet()));
        setSlot(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(this.team.getChestplate()));
        setSlot(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(this.team.getLeggings()));
        setSlot(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(this.team.getBoots()));

        pickGun();
        setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(Weapon.WeaponUtils.toItemStack(getCurrentWeapon())));

        //Spawns the entity
        ((CraftWorld) spawn.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    protected void initAttributes() {
        super.initAttributes();
        getAttributeInstance(GenericAttributes.maxHealth).setValue(20.0);
        getAttributeInstance(GenericAttributes.FOLLOW_RANGE).setValue(64.0D);
    }

    private void setName() {
        int hearts = (int) getHealth();
        boolean endHalf = hearts % 2 == 1;
        hearts = hearts / 2;
        String name = "§c";
        for (int i = 0; i < hearts; i++)
            name += "♥";
        if (endHalf)
            name += "♡";
        setCustomName(name);
    }

    private void pickGun() {
        List<Class<? extends Weapon>> weapons = GameService.getCurrentGame().allowedGuns();
        if (weapons == null || weapons.isEmpty()) { //TODO make the list be all the available weapons
            weapons = Collections.singletonList(Pistol.class);
        }
        try {
            gun = weapons.get(new Random().nextInt(weapons.size())).newInstance();
            gun.setOwner(this);
            gun.setOneHitKill(GameService.getCurrentGame() instanceof OneHitMinute); //TODO add other onehit types if we make any
        } catch (InstantiationException | IllegalAccessException e) {
            gun = new Pistol();
        }
    }

    public Weapon getCurrentWeapon() {
        return gun;
    }

    public Team getCurrentTeam() {
        return this.team;
    }

    public void hit(BasePlayer shooter) {
        if (shooter.getCurrentTeam() != null) {
            if (shooter.getCurrentTeam().contains(this)) {
                if (shooter instanceof PBPlayer)
                    ((PBPlayer) shooter).sendMessage("Watch out! That bot is on your team!");
            } else {
                if (shooter.getCurrentWeapon().isOneHitKill() || wouldDie(shooter.getCurrentWeapon().damage())) {
                    refillHealth();
                    kill(shooter);
                    lastDamager = null;
                } else {
                    damagePlayer(shooter.getCurrentWeapon().damage());
                    lastDamager = shooter.getNMSEntity();
                }
            }
        }
    }

    public EntityLiving getNMSEntity() {
        return this;
    }

    private void kill(final BasePlayer killer) {
        //if (!isInGame()) return; //Is always ingame if exists
        //addDeath();//does it matter if we keep track of this
        if (killer != null && killer instanceof PBPlayer)
            ((PBPlayer) killer).addKill();
        getBukkitEntity().teleport(team.getAISpawn());
        GameService.getCurrentGame().onPlayerKill(killer, this);
        Bukkit.getOnlinePlayers().forEach(p -> p.playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_THUNDER, 40, 0));
    }

    private void damagePlayer(int damage) {
        getAttributeInstance(GenericAttributes.maxHealth).setValue(getMaxHealth() - damage);
        setHealth(getMaxHealth());
        setName();
    }

    private void refillHealth() {
        getAttributeInstance(GenericAttributes.maxHealth).setValue(maxHealth);
        setHealth(maxHealth);
        setName();
    }

    private boolean wouldDie(int damage) {
        return getMaxHealth() - damage <= 0;
    }

    public void remove() {
        die();
    }
}