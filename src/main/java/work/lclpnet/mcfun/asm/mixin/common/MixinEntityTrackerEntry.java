package work.lclpnet.mcfun.asm.mixin.common;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.server.network.EntityTrackerEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.mcfun.event.EntityTrackedSpawnPacketsCallback;

import java.util.function.Consumer;

@Mixin(EntityTrackerEntry.class)
public class MixinEntityTrackerEntry {

    @Shadow
    @Final
    private Entity entity;

    @Inject(
            method = "sendPackets(Ljava/util/function/Consumer;)V",
            at = @At("RETURN")
    )
    public void onPostSendPackets(Consumer<Packet<?>> sender, CallbackInfo ci) {
        EntityTrackedSpawnPacketsCallback.EVENT.invoker().onSendSpawnPackets(sender, this.entity);
    }

}
