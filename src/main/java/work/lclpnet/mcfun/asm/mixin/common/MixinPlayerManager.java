package work.lclpnet.mcfun.asm.mixin.common;

import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import work.lclpnet.mcfun.util.CreditsHandler;

@Mixin(PlayerManager.class)
public class MixinPlayerManager {

    @Inject(
            method = "respawnPlayer(Lnet/minecraft/server/network/ServerPlayerEntity;Z)Lnet/minecraft/server/network/ServerPlayerEntity;",
            at = @At("RETURN")
    )
    public void onRespawn(ServerPlayerEntity player, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        if (!alive) return;

        CreditsHandler.handleRespawn(cir.getReturnValue()); // new player will be returned
    }
}
