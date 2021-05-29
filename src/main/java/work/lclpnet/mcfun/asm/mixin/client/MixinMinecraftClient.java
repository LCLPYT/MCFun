package work.lclpnet.mcfun.asm.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.mcfun.event.LeftClickAirCallback;
import work.lclpnet.mcfun.networking.MCNetworking;
import work.lclpnet.mcfun.networking.packet.PacketLeftClickAir;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {

    @Shadow
    public HitResult crosshairTarget;
    @Shadow
    public ClientPlayerEntity player;

    @Inject(
            method = "doAttack()V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/network/ClientPlayerEntity;swingHand(Lnet/minecraft/util/Hand;)V"
            )
    )
    public void onDoAttack(CallbackInfo ci) {
        if(crosshairTarget.getType() == HitResult.Type.MISS) {
            if(player != null) LeftClickAirCallback.EVENT.invoker().interact(player, player.world);
            MCNetworking.sendPacketToServer(new PacketLeftClickAir());
        }
    }

}
