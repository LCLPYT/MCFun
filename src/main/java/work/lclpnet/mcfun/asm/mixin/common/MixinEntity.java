package work.lclpnet.mcfun.asm.mixin.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import work.lclpnet.mcfun.util.CreditsHandler;

import static work.lclpnet.mcfun.asm.InstanceMixinHelper.castTo;
import static work.lclpnet.mcfun.asm.InstanceMixinHelper.isInstance;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(
            method = "changeDimension(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/entity/Entity;",
            at = @At("HEAD"),
            cancellable = true
    )
    public void onChangeDimension(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        if (!isInstance(this, LivingEntity.class)) return;

        LivingEntity self = castTo(this, LivingEntity.class);
        if (CreditsHandler.isDimensionChangeBlocked(self)) {
            cir.setReturnValue(self.world instanceof ServerWorld && !self.removed ? self : null);
            cir.cancel();
        }
    }
}
