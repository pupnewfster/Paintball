package net.battlenexus.paintball.entities.ai.pathfinder;

import com.google.common.base.Predicate;
import net.battlenexus.paintball.entities.Team;
import net.battlenexus.paintball.entities.ai.SimpleSkeleton;
import net.minecraft.server.v1_11_R1.*;

import javax.annotation.Nullable;
import java.util.List;

public class PathfinderGoalTargetNearestEnemyAI extends PathfinderGoal {
    private final SimpleSkeleton b;
    private final Predicate<EntityLiving> c;
    private final PathfinderGoalNearestAttackableTarget.DistanceComparator d;
    private EntityLiving e;
    private final Class<? extends EntityLiving> f;

    public PathfinderGoalTargetNearestEnemyAI(SimpleSkeleton skeleton, Class<? extends EntityLiving> oclass) {
        this.b = skeleton;
        this.f = oclass;

        this.c = new Predicate() {
            public boolean a(@Nullable EntityLiving entityliving) {
                Team other = null;
                if (entityliving instanceof SimpleSkeleton)
                    other = ((SimpleSkeleton) entityliving).getCurrentTeam();
                Team ours = PathfinderGoalTargetNearestEnemyAI.this.b.getCurrentTeam();
                if (other != null && other.equals(ours))
                    return false;

                double d0 = PathfinderGoalTargetNearestEnemyAI.this.f();
                if (entityliving.isSneaking())
                    d0 *= 0.800000011920929D;
                return !entityliving.isInvisible() && (double) entityliving.g(PathfinderGoalTargetNearestEnemyAI.this.b) <= d0 && PathfinderGoalTarget.a(PathfinderGoalTargetNearestEnemyAI.this.b, entityliving, false, true);
            }

            public boolean apply(@Nullable Object object) {
                return this.a((EntityLiving) object);
            }
        };
        this.d = new PathfinderGoalNearestAttackableTarget.DistanceComparator(skeleton);
    }

    public boolean a() {
        double d0 = this.f();
        List list = this.b.world.a(this.f, this.b.getBoundingBox().grow(d0, 4.0D, d0), this.c);

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
        else {
            double d0 = this.f();
            return this.b.h(entityliving) <= d0 * d0 && (!(entityliving instanceof EntityPlayer) || !((EntityPlayer) entityliving).playerInteractManager.isCreative());

        }
    }

    public void c() {
        this.b.setGoalTarget(this.e, org.bukkit.event.entity.EntityTargetEvent.TargetReason.CLOSEST_ENTITY, true); // CraftBukkit - reason
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
