package net.battlenexus.paintball.entities.ai;

import com.google.common.collect.Sets;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.entities.ai.pathfinder.PathfinderGoalTargetEnemyPlayer;
import net.battlenexus.paintball.entities.ai.pathfinder.PathfinderGoalTargetNearestEnemyAI;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.lang.reflect.Field;

public class SimpleSkeleton extends EntitySkeleton implements AIPlayer { //TODO add taking damage, respawning and such
    private final Team team;
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
        this.goalSelector.a(5, new PathfinderGoalRandomStrollLand(this, 1.0D));
        this.goalSelector.a(6, new PathfinderGoalLookAtPlayer(this, EntityHuman.class, 8.0F));//TODO edit the goal to look at enemies
        this.goalSelector.a(6, new PathfinderGoalRandomLookaround(this));
        this.goalSelector.a(8, new PathfinderGoalArrowAttack(this, 1.0D, 20, 15.0F));
        this.targetSelector.a(1, new PathfinderGoalTargetEnemyPlayer(this));
        this.targetSelector.a(2, new PathfinderGoalTargetNearestEnemyAI(this, SimpleSkeleton.class));
        //TODO add for all different AI types we make

        setName();
        setCustomNameVisible(true);
        setSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(this.team.getHelmet()));
        setSlot(EnumItemSlot.CHEST, CraftItemStack.asNMSCopy(this.team.getChestplate()));
        setSlot(EnumItemSlot.LEGS, CraftItemStack.asNMSCopy(this.team.getLeggings()));
        setSlot(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(this.team.getBoots()));
        setSlot(EnumItemSlot.MAINHAND, CraftItemStack.asNMSCopy(new org.bukkit.inventory.ItemStack(org.bukkit.Material.BOW))); //TODO set type of gun

        //Spawns the entity
        ((CraftWorld) spawn.getWorld()).getHandle().addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    private void setName() {
        int hearts = (int) getHealth();
        boolean endHalf = hearts % 2 == 1;
        hearts = hearts/2;
        String name = "§c";
        for (int i = 0; i < hearts; i++)
            name += "♥";
        if (endHalf)
            name += "♡";
        setCustomName(name);
    }

    public Team getTeam() {
        return this.team;
    }

    public void hit(PBPlayer shooter) {
        if (shooter.getCurrentTeam() != null) {
            if (shooter.getCurrentTeam().contains(this))
                shooter.sendMessage("Watch out! That bot is on your team!");
            else {
                if (shooter.getCurrentWeapon().isOneHitKill() || wouldDie(shooter.getCurrentWeapon().damage())) {
                    refillHealth();
                    kill(shooter);
                } else
                    damagePlayer(shooter.getCurrentWeapon().damage());
            }
        }
    }

    private void kill(final PBPlayer killer) {
        //if (!isInGame()) return; //Is always ingame if exists
        //addDeath();//does it matter if we keep track of this
        if (killer != null)
            killer.addKill();
        getBukkitEntity().teleport(team.getAISpawn());
        //GameService.getCurrentGame().onPlayerKill(killer, this);
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
}