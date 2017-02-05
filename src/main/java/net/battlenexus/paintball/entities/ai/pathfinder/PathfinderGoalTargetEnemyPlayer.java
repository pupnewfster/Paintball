package net.battlenexus.paintball.entities.ai.pathfinder;

import com.google.common.base.Predicate;
import net.battlenexus.paintball.entities.PBPlayer;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.entities.ai.SimpleSkeleton;
import net.battlenexus.paintball.game.GameService;
import net.minecraft.server.v1_11_R1.*;

import javax.annotation.Nullable;
import java.util.List;

public class PathfinderGoalTargetEnemyPlayer extends PathfinderGoal {
    private final SimpleSkeleton b;
    private final Predicate<Entity> c;
    private final PathfinderGoalNearestAttackableTarget.DistanceComparator d;
    private EntityLiving e;

    public PathfinderGoalTargetEnemyPlayer(SimpleSkeleton skeleton) {
        this.b = skeleton;
        this.c = new Predicate() {
            public boolean a(@Nullable Entity entity) {
                if (!(entity instanceof EntityHuman))
                    return false;
                else if (((EntityHuman) entity).abilities.isInvulnerable)
                    return false;
                else {
                    Team other = GameService.getCurrentGame().getTeamForPlayer(PBPlayer.getPlayer(((EntityPlayer) entity).getBukkitEntity().getPlayer()));
                    Team ours = PathfinderGoalTargetEnemyPlayer.this.b.getTeam();
                    if (other != null && other.equals(ours))
                        return false;
                    double d0 = PathfinderGoalTargetEnemyPlayer.this.f();
                    if (entity.isSneaking())
                        d0 *= 0.800000011920929D;
                    if (entity.isInvisible()) {
                        float f = ((EntityHuman) entity).cO();
                        if (f < 0.1F)
                            f = 0.1F;
                        d0 *= (double) (0.7F * f);
                    }
                    return (double) entity.g(PathfinderGoalTargetEnemyPlayer.this.b) > d0 ? false : PathfinderGoalTarget.a(PathfinderGoalTargetEnemyPlayer.this.b, (EntityLiving) entity, false, true);
                }
            }

            public boolean apply(@Nullable Object object) {
                return this.a((Entity) object);
            }
        };
        this.d = new PathfinderGoalNearestAttackableTarget.DistanceComparator(skeleton);
    }

    public boolean a() {
        double d0 = this.f();
        List list = this.b.world.a(EntityHuman.class, this.b.getBoundingBox().grow(d0, 4.0D, d0), this.c);
        list.sort(this.d);
        if (list.isEmpty())
            return false;
        else {
            this.e = (EntityLiving) list.get(0);
            return true;
        }
    }

    public boolean b() {
        EntityLiving entityliving = this.b.getGoalTarget();
        if (entityliving == null || !entityliving.isAlive())
            return false;
        else if (entityliving instanceof EntityHuman && ((EntityHuman) entityliving).abilities.isInvulnerable)
            return false;
        else {
            double d0 = this.f();
            return this.b.h(entityliving) <= d0 * d0 && (!(entityliving instanceof EntityPlayer) || !((EntityPlayer) entityliving).playerInteractManager.isCreative());
        }
    }

    public void c() {
        this.b.setGoalTarget(this.e, org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_PLAYER, true); // CraftBukkit - added reason
        super.c();
    }

    public void d() {
        this.b.setGoalTarget(null);
        super.c();
    }

    private double f() {
        AttributeInstance attributeinstance = this.b.getAttributeInstance(GenericAttributes.FOLLOW_RANGE);
        return attributeinstance == null ? 16.0D : attributeinstance.getValue();
    }
}