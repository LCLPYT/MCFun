package work.lclpnet.mcfun.asm.mixin.common;

import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import work.lclpnet.mcfun.util.MCUtils;
import work.lclpnet.mcfun.util.Rope;

import java.util.EnumSet;
import java.util.Set;

@Mixin(EndGatewayBlockEntity.class)
public class MixinEndGatewayBlockEntity {

    @Inject(
            method = "tryTeleportingEntity(Lnet/minecraft/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/Entity;teleport(DDD)V"
            ),
            locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onTeleport(Entity entity, CallbackInfo ci, BlockPos blockPos, Entity teleportEntity) {
        if (!(teleportEntity instanceof LivingEntity)) return;

        final ServerWorld world = (ServerWorld) teleportEntity.world;
        final double x = blockPos.getX() + 0.5D, y = blockPos.getY(), z = blockPos.getZ() + 0.5D;
        final EnumSet<PlayerPositionLookS2CPacket.Flag> flags = EnumSet.of(PlayerPositionLookS2CPacket.Flag.X, PlayerPositionLookS2CPacket.Flag.Y, PlayerPositionLookS2CPacket.Flag.Z, PlayerPositionLookS2CPacket.Flag.X_ROT, PlayerPositionLookS2CPacket.Flag.Y_ROT);
        final float yaw = teleportEntity.yaw, pitch = teleportEntity.pitch;
        Set<LivingEntity> chainMembers = Rope.getAllMembersInChainExceptSelfOf((LivingEntity) teleportEntity);
        chainMembers.forEach(en -> MCUtils.teleport(en, world, x, y, z, flags, yaw, pitch));
    }
}
