package work.lclpnet.mcfun.asm.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import work.lclpnet.mcfun.asm.type.IRopeNode;
import work.lclpnet.mcfun.util.Rope;

import java.util.Set;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    // Makes the compiler aware of the dispatcher variable in the target class.
    @Final
    @Shadow
    protected EntityRenderDispatcher dispatcher;

    // provides extra logic, if an entity should be rendered, according to the rope connection state.
    // Logic copied from MobEntityRenderer#shouldRender() with the leash predicate.
    @Inject(
            method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z",
            at = @At(
                    value = "RETURN",
                    ordinal = 2
            ),
            cancellable = true
    )
    public void onShouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValueZ() || !(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) entity;

        Set<LivingEntity> connected = IRopeNode.fromEntity(livingEntity).getRopeConnectedEntities();
        if(connected == null) return;

        boolean shouldRender = connected.stream().anyMatch(en -> frustum.isVisible(en.getVisibilityBoundingBox()));

        cir.setReturnValue(shouldRender);
        cir.cancel();
    }

    // Inject rope render call at the end of the render() method
    @Inject(
            method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("RETURN")
    )
    public void onRender(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if(!(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) entity;

        Set<LivingEntity> connected = IRopeNode.fromEntity(livingEntity).getRopeConnectedEntities();
        if(connected == null) return;

        connected.stream()
                .filter(en -> shouldRenderRope(en, livingEntity))
                .forEach(en -> renderRope(livingEntity, tickDelta, matrices, vertexConsumers, en));
    }

    /**
     * Determines, if a rope from an entity to another should be rendered.
     * Otherwise, there would be two ropes connecting two entities each, which is unwanted.
     * This method determines, from which entity the rope should start (return value is 'true'). Otherwise this method returns 'false'.
     *
     * @param from The entity from which the rope starts.
     * @param to The entity to which the rope leads.
     * @return True, if the rope should be rendered from the given entity to the other. Otherwise, false.
     */
    private boolean shouldRenderRope(LivingEntity from, LivingEntity to) {
        boolean fromPlayer = from instanceof PlayerEntity, toPlayer = to instanceof PlayerEntity;

        if(fromPlayer) {
            if(toPlayer) return MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().player.equals(from);
            else return true;
        } else if(toPlayer) return false;
        else return from.getEntityId() > to.getEntityId();
    }

    /**
     * Actual method to render the rope.
     *
     * @param livingEntity The entity which is rendered by this renderer.
     * @param tickDelta Given tick delta.
     * @param matrixStack Given matrix stack context.
     * @param vertexConsumerProvider Given vertex consumer provider.
     * @param connected The entity which is connected with the entity rendererd by this renderer.
     * @param <E> The type of the connected entity.
     */
    private <E extends LivingEntity> void renderRope(LivingEntity livingEntity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, E connected) {
        Rope rope = IRopeNode.fromEntity(livingEntity).getRopeConnection(connected);
        if(rope == null) return;

        matrixStack.push();
        Vec3d vec3d = connected.getCameraPosVec(tickDelta).subtract(0D, 0.5D, 0D); // maybe make it less annoying
        double d = (double)(MathHelper.lerp(tickDelta, livingEntity.bodyYaw, livingEntity.prevBodyYaw) * 0.017453292F) + 1.5707963267948966D;
        Vec3d vec3d2 = livingEntity.method_29919();
        double e = Math.cos(d) * vec3d2.z + Math.sin(d) * vec3d2.x;
        double g = Math.sin(d) * vec3d2.z - Math.cos(d) * vec3d2.x;
        double h = MathHelper.lerp(tickDelta, livingEntity.prevX, livingEntity.getX()) + e;
        double i = MathHelper.lerp(tickDelta, livingEntity.prevY, livingEntity.getY()) + vec3d2.y;
        double j = MathHelper.lerp(tickDelta, livingEntity.prevZ, livingEntity.getZ()) + g;
        matrixStack.translate(e, vec3d2.y, g);
        float k = (float)(vec3d.x - h);
        float l = (float)(vec3d.y - i);
        float m = (float)(vec3d.z - j);
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLeash());
        Matrix4f matrix4f = matrixStack.peek().getModel();
        float o = MathHelper.fastInverseSqrt(k * k + m * m) * 0.025F / 2.0F;
        float p = m * o;
        float q = k * o;
        BlockPos blockPos = new BlockPos(livingEntity.getCameraPosVec(tickDelta));
        BlockPos blockPos2 = new BlockPos(connected.getCameraPosVec(tickDelta));
        int r = this.getBlockLight(livingEntity, blockPos);
        int s = this.dispatcher.getRenderer(connected).getBlockLight(connected, blockPos2);
        int t = livingEntity.world.getLightLevel(LightType.SKY, blockPos);
        int u = livingEntity.world.getLightLevel(LightType.SKY, blockPos2);

//        LINEAR ROPE TENSION
//        float approxDistance = 1F / MathHelper.fastInverseSqrt((float) livingEntity.squaredDistanceTo(connected));
//        float ropeTension = Math.max(0F, approxDistance - (MCFun.ROPE_LENGTH * 0.65F)) * 0.2F + 1F;

        // QUADRATIC ROPE TENSION
        float distanceSq = (float) livingEntity.squaredDistanceTo(connected);
        float overhang = distanceSq - (rope.getLengthSquared() * 0.4225F);
        float ropeTension = Math.max(0F, overhang) * rope.getTensionFactor() + 1F;

        method_23186_custom(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, p, q, ropeTension);
        method_23186_custom(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.0F, p, q, ropeTension);
        matrixStack.pop();
    }

    // Override for MobEntityRenderer#method_23186 to change the rope look to look different from leashes.
    private static void method_23186_custom(VertexConsumer vertexConsumer, Matrix4f matrix4f, float f, float g, float h, int i, int j, int k, int l, float n, float o, float p, float ropeTension) {
        for(int r = 0; r < 24; ++r) {
            float s = (float)r / 23.0F;
            int t = (int)MathHelper.lerp(s, (float)i, (float)j);
            int u = (int)MathHelper.lerp(s, (float)k, (float)l);
            int v = LightmapTextureManager.pack(t, u);
            method_23187_custom(vertexConsumer, matrix4f, v, f, g, h, n, r, false, o, p, ropeTension);
            method_23187_custom(vertexConsumer, matrix4f, v, f, g, h, n, r + 1, true, o, p, ropeTension);
        }
    }

    // Override for MobEntityRenderer#method_23187 to change the leash color.
    private static void method_23187_custom(VertexConsumer vertexConsumer, Matrix4f matrix4f, int i, float f, float g, float h, float k, int m, boolean bl, float n, float o, float ropeTension) {
        float red = MathHelper.clamp(0.24F * ropeTension, 0F, 1F);
        float green = 0.16F;
        float blue = 0.09F;

        if (m % 2 == 0) {
            red *= 0.7F;
            green *= 0.7F;
            blue *= 0.7F;
        }

        float s = (float)m / (float) 24;
        float t = f * s;
        float u = g > 0.0F ? g * s * s : g - g * (1.0F - s) * (1.0F - s);
        float v = h * s;
        if (!bl) {
            vertexConsumer.vertex(matrix4f, t + n, u + (float) 0.025 - k, v - o).color(red, green, blue, 1.0F).light(i).next();
        }

        vertexConsumer.vertex(matrix4f, t - n, u + k, v + o).color(red, green, blue, 1.0F).light(i).next();
        if (bl) {
            vertexConsumer.vertex(matrix4f, t + n, u + (float) 0.025 - k, v - o).color(red, green, blue, 1.0F).light(i).next();
        }
    }

    // Makes the compiler aware, that this function exists at compile time in the target class.
    @Shadow
    public abstract int getBlockLight(Entity entity, BlockPos blockPos);

}
