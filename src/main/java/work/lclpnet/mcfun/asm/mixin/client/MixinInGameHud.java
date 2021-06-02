package work.lclpnet.mcfun.asm.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import work.lclpnet.mcfun.asm.type.IRopeNode;
import work.lclpnet.mcfun.item.MCItems;
import work.lclpnet.mcfun.item.RopeItem;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(InGameHud.class)
public class MixinInGameHud {

    @Shadow
    @Final
    private MinecraftClient client;
    @Shadow
    private int scaledWidth;
    @Shadow
    private int scaledHeight;

    @Inject(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/hud/InGameHud;renderHotbar(FLnet/minecraft/client/util/math/MatrixStack;)V",
                    shift = At.Shift.AFTER
            )
    )
    public void postRenderHotbar(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if(client.player == null) return;

        ItemStack mainHand = client.player.getMainHandStack(),
                offHand = client.player.getOffHandStack();

        Predicate<ItemStack> condition = itemStack -> itemStack != null
                && !itemStack.isEmpty()
                && itemStack.getItem().equals(MCItems.ROPE_ITEM);

        if(!condition.test(mainHand) && !condition.test(offHand)) return;

        int nextIndex = 0;
        LivingEntity selected = RopeItem.getClientSelection();
        if(selected != null) {
            Set<LivingEntity> connected = IRopeNode.fromEntity(selected).getRopeConnectedEntities();
            if(connected != null && !connected.isEmpty())
                drawListText(matrices, nextIndex++, new TranslatableText("item.mcfun.rope.help.disconnect"), Optional.ofNullable(Formatting.RED.getColorValue()).orElse(0xffffffff));

            drawListText(matrices, nextIndex++, new TranslatableText("item.mcfun.rope.help.connect"), Optional.ofNullable(Formatting.GREEN.getColorValue()).orElse(0xffffffff));
            drawListText(matrices, nextIndex, new TranslatableText("item.mcfun.rope.help.current_selection").append(selected.getName()), Optional.ofNullable(Formatting.YELLOW.getColorValue()).orElse(0xffffffff));
        } else {
            drawListText(matrices, nextIndex, new TranslatableText("item.mcfun.rope.help.select"), Optional.ofNullable(Formatting.YELLOW.getColorValue()).orElse(0xffffffff));
        }
    }

    private void drawListText(MatrixStack matrices, int index, Text text, int color) {
        final float marginX = 10F, marginY = 135F, verticalSpacing = 1F;
        client.textRenderer.drawWithShadow(
                matrices,
                text,
                marginX,
                this.scaledHeight - marginY - (index + 1) * client.textRenderer.fontHeight - index * verticalSpacing,
                color
        );
    }
}
