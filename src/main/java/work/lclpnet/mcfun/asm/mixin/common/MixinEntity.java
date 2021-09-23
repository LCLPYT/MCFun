package work.lclpnet.mcfun.asm.mixin.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.mcfun.asm.type.IRopeNode;

import java.util.Set;

import static work.lclpnet.mcfun.asm.InstanceMixinHelper.isInstance;

@Mixin(Entity.class)
public class MixinEntity {

    @Inject(
            method = "tick()V",
            at = @At("RETURN")
    )
    public void onTick(CallbackInfo ci) {
        Entity en = (Entity) (Object) this;
        en.getPassengersDeep().stream().filter(passenger -> {
            if (!isInstance(passenger, LivingEntity.class)) return false;

            Set<LivingEntity> ropeConnectedEntities = IRopeNode.fromEntity((LivingEntity) passenger).getRopeConnectedEntities();
            return ropeConnectedEntities != null && !ropeConnectedEntities.isEmpty();
        }).forEach(passenger -> {

        });
    }
}
