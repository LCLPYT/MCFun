package work.lclpnet.mcfun.asm.mixin.common;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.mcfun.asm.type.IRopeNode;
import work.lclpnet.mcfun.networking.MCNetworking;
import work.lclpnet.mcfun.networking.packet.PacketUpdateRopeConnection;
import work.lclpnet.mcfun.util.Rope;

import java.util.*;

import static work.lclpnet.mcfun.asm.InstanceMixinHelper.castTo;
import static work.lclpnet.mcfun.asm.InstanceMixinHelper.isInstance;

@Mixin(LivingEntity.class)
public class MixinLivingEntity implements IRopeNode {

    private Map<LivingEntity, Rope> ropeConnected = null;

    @Nullable
    @Override
    public Set<LivingEntity> getRopeConnectedEntities() {
        return ropeConnected == null ? null : ropeConnected.keySet();
    }

    @Override
    public void addRopeConnection(LivingEntity other, Rope rope) {
        Objects.requireNonNull(other);
        Objects.requireNonNull(rope);

        if (this.ropeConnected == null) this.ropeConnected = new HashMap<>();

        this.ropeConnected.put(other, rope);

        LivingEntity living = castTo(this, LivingEntity.class);
        if(living.world.isClient) return;

        // On server, change has to be sent to clients
        MCNetworking.sendToAllTrackingIncludingSelf(living, PacketUpdateRopeConnection.createConnectPacket(living, other, rope));
    }

    @Override
    public void removeRopeConnection(LivingEntity other) {
        Objects.requireNonNull(other);
        if(ropeConnected == null) return;

        if(!isConnectedTo(other)) return;

        this.ropeConnected.remove(other);

        LivingEntity living = castTo(this, LivingEntity.class);
        if(living.world.isClient) return;

        // On server, change has to be sent to clients
        MCNetworking.sendToAllTrackingIncludingSelf(living, PacketUpdateRopeConnection.createDisconnectPacket(living, other));
    }

    @Nullable
    @Override
    public Rope getRopeConnection(LivingEntity other) {
        Objects.requireNonNull(other);
        return ropeConnected.get(other);
    }

    // Mixin for applying rope force
    @Inject(
            method = "tick()V",
            at = @At("RETURN")
    )
    public void onTickApplyRopeForce(CallbackInfo ci) {
        LivingEntity entity = castTo(this, LivingEntity.class);
        if (entity.world.isClient) return;

        this.updateRopes();
        if (isInstance(entity, MobEntity.class) && entity.age % 5 == 0) {
            castTo(entity, MobEntity.class).updateGoalControls();
        }
    }

    private void updateRopes() {
        /* check if any rope connected entity is dead and if the rope should be removed */
        if(ropeConnected == null || ropeConnected.isEmpty()) return;

        LivingEntity living = castTo(this, LivingEntity.class);

        Set<LivingEntity> removeLater = new HashSet<>();

        // remove all ropes, if the entity is dead
        if (!living.isAlive()) removeLater.addAll(ropeConnected.keySet());

        // remove any rope connection where the connected entity is dead
        ropeConnected.keySet().stream()
                .filter(entity -> !entity.isAlive())
                .forEach(removeLater::add);

        // actually remove the connections (after iteration, to prevent ConcurrentModificationException)
        if (!removeLater.isEmpty()) {
            removeLater.forEach(this::disconnectFrom);
            removeLater.clear();
        }

        LivingEntity nearestConnected = null;
        boolean shouldCancelNearest = false;

        if(isInstance(living, PathAwareEntity.class)) {
            Pair<LivingEntity, Double> nearestEntry = ropeConnected.keySet().stream()
                    .map(entity -> new Pair<>(entity, entity.squaredDistanceTo(living)))
                    .min(Comparator.comparing(Pair::getRight))
                    .orElse(null);

            if(nearestEntry != null) {
                nearestConnected = nearestEntry.getLeft();

                if(nearestConnected.world == living.world) {
                    Rope rope = ropeConnected.get(nearestConnected);
                    shouldCancelNearest = updateLeashLikeBehaviour(castTo(living, PathAwareEntity.class), nearestConnected, nearestEntry.getRight(), rope);
                }
            }
        }

        final LivingEntity nearest = nearestConnected;
        final boolean cancelLogicWithNearest = shouldCancelNearest;

        ropeConnected.forEach((entity, rope) -> {
            if(entity.world != living.world || (entity.equals(nearest) && cancelLogicWithNearest)) return;

            // check rope distance violation
            double distanceSquared = entity.squaredDistanceTo(living);
            if(distanceSquared <= rope.getLengthSquared()) return;

            // do rope pull
            double distance = Math.sqrt(distanceSquared);
            double d = (entity.getX() - living.getX()) / distance;
            double e = (entity.getY() - living.getY()) / distance;
            double g = (entity.getZ() - living.getZ()) / distance;
            living.setVelocity(living.getVelocity().add(
                    Math.copySign(d * d * 0.4D, d),
                    Math.copySign(e * e * 0.4D, e),
                    Math.copySign(g * g * 0.4D, g)
            ));
            living.velocityModified = true;
        });
    }

    /**
     *
     * @param mob This class instance, casted to PathAwareEntity.
     * @param nearestConnected The nearest rope connected entity.
     * @param rope The rope connection associated with the nearestConnected entity.
     * @return True, if further rope logic should be canceled between the two entities.
     */
    private boolean updateLeashLikeBehaviour(PathAwareEntity mob, LivingEntity nearestConnected, double distanceSquared, Rope rope) {
        Objects.requireNonNull(rope);

        // partial content of PathAwareEntity#updateLeash

        mob.setPositionTarget(nearestConnected.getBlockPos(), 5);
        if(mob instanceof TameableEntity && ((TameableEntity) mob).isInSittingPose()) return true;

        float distance = (float) Math.sqrt(distanceSquared);
        mob.updateForLeashLength(distance);
        if(distance > rope.getLength() + 4.0F) mob.goalSelector.disableControl(Goal.Control.MOVE);
        else if(distance <= rope.getLength()) {
            mob.goalSelector.enableControl(Goal.Control.MOVE);

            Vec3d vec3d = new Vec3d(
                    nearestConnected.getX() - mob.getX(),
                    nearestConnected.getY() - mob.getY(),
                    nearestConnected.getZ() - mob.getZ()
            ).normalize().multiply(Math.max(distance - 2.0F, 0.0F));

            mob.getNavigation().startMovingTo(
                    mob.getX() + vec3d.x,
                    mob.getY() + vec3d.y,
                    mob.getZ() + vec3d.z,
                    mob.getRunFromLeashSpeed()
            );
        }
        return false;
    }

}
