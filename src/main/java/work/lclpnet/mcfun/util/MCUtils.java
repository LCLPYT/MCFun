package work.lclpnet.mcfun.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Set;

public class MCUtils {

    // from TeleportCommand
    @Nullable
    public static Entity teleport(Entity target, ServerWorld world, double x, double y, double z, Set<PlayerPositionLookS2CPacket.Flag> movementFlags, float yaw, float pitch) {
        BlockPos blockPos = new BlockPos(x, y, z);
        if (!World.method_25953(blockPos)) throw new IllegalArgumentException("Invalid teleport position.");

        if (target instanceof ServerPlayerEntity) {
            ChunkPos chunkPos = new ChunkPos(new BlockPos(x, y, z));
            world.getChunkManager().addTicket(ChunkTicketType.field_19347, chunkPos, 1, target.getEntityId());
            target.stopRiding();
            if (((ServerPlayerEntity)target).isSleeping()) {
                ((ServerPlayerEntity)target).wakeUp(true, true);
            }

            if (world == target.world) {
                ((ServerPlayerEntity)target).networkHandler.teleportRequest(x, y, z, yaw, pitch, movementFlags);
            } else {
                ((ServerPlayerEntity)target).teleport(world, x, y, z, yaw, pitch);
            }

            target.setHeadYaw(yaw);
        } else {
            float f = MathHelper.wrapDegrees(yaw);
            float g = MathHelper.wrapDegrees(pitch);
            g = MathHelper.clamp(g, -90.0F, 90.0F);
            if (world == target.world) {
                target.refreshPositionAndAngles(x, y, z, f, g);
                target.setHeadYaw(f);
            } else {
                target.detach();
                Entity entity = target;
                target = target.getType().create(world);
                if (target == null) return null;

                target.copyFrom(entity);
                target.refreshPositionAndAngles(x, y, z, f, g);
                target.setHeadYaw(f);
                world.onDimensionChanged(target);
                entity.removed = true;
            }
        }

        if (!(target instanceof LivingEntity) || !((LivingEntity)target).isFallFlying()) {
            target.setVelocity(target.getVelocity().multiply(1.0D, 0.0D, 1.0D));
            target.setOnGround(true);
        }

        if (target instanceof PathAwareEntity) {
            ((PathAwareEntity)target).getNavigation().stop();
        }

        return target;
    }
}
