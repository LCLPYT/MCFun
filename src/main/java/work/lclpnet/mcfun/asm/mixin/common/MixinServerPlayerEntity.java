package work.lclpnet.mcfun.asm.mixin.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import work.lclpnet.mcfun.asm.type.IRopeNode;
import work.lclpnet.mcfun.util.CreditsHandler;
import work.lclpnet.mcfun.util.MCUtils;

import java.util.EnumSet;
import java.util.Set;

import static work.lclpnet.mcfun.asm.InstanceMixinHelper.castTo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity {

    @Inject(
            method = "changeDimension(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At(
                    value = "RETURN",
                    ordinal = 0
            )
    )
    public void onEndToOverworld(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity player = castTo(this, ServerPlayerEntity.class);
        CreditsHandler.handleCreditsScreenStart(player);
    }

    @Inject(
            method = "changeDimension(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At(
                    value = "RETURN",
                    ordinal = 1
            )
    )
    public void onChangedDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity player = castTo(this, ServerPlayerEntity.class);
        IRopeNode node = IRopeNode.fromEntity(player);
        Set<LivingEntity> connected = node.getRopeConnectedEntities();
        if (connected == null || connected.isEmpty()) return;

        final ServerWorld world = player.getServerWorld();
        final double x = player.getX(), y = player.getY(), z = player.getZ();
        final EnumSet<PlayerPositionLookS2CPacket.Flag> flags = EnumSet.of(PlayerPositionLookS2CPacket.Flag.X, PlayerPositionLookS2CPacket.Flag.Y, PlayerPositionLookS2CPacket.Flag.Z, PlayerPositionLookS2CPacket.Flag.X_ROT, PlayerPositionLookS2CPacket.Flag.Y_ROT);
        final float yaw = player.yaw, pitch = player.pitch;
        connected.forEach(en -> {
            en.netherPortalCooldown = en.getDefaultNetherPortalCooldown();
            MCUtils.teleport(en, world, x, y, z, flags, yaw, pitch);
        });
    }

    @Inject(
            method = "changeDimension(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onChangeDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        ServerPlayerEntity self = castTo(this, ServerPlayerEntity.class);
        if (CreditsHandler.isDimensionChangeBlocked(self)) {
            cir.setReturnValue(self);
            cir.cancel();
        }
    }
}
