package work.lclpnet.mcfun.mixin;

import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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
import work.lclpnet.mcfun.rope.IRopeConnectable;
import work.lclpnet.mcfun.rope.Rope;

import java.util.Set;

@Mixin(EntityRenderer.class)
public abstract class MixinEntityRenderer {

    @Final
    @Shadow
    protected EntityRenderDispatcher dispatcher;

    @Inject(
            method = "shouldRender(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/render/Frustum;DDD)Z",
            at = @At(
                    value = "RETURN",
                    ordinal = 2
            ),
            cancellable = true
    )
    public void onShouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if(!cir.getReturnValueZ() || !(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) entity;

        Set<Rope> connections = IRopeConnectable.getFrom(livingEntity).getRopeConnections();
        if(connections == null) return;

        boolean shouldRender = connections.stream()
                .anyMatch(rope -> frustum.isVisible(rope.getConnectedTo().getVisibilityBoundingBox()));

        cir.setReturnValue(shouldRender);
        cir.cancel();
    }

    @Inject(
            method = "render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
            at = @At("TAIL")
    )
    public void onRender(Entity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if(!(entity instanceof LivingEntity)) return;
        LivingEntity livingEntity = (LivingEntity) entity;

        Set<Rope> connections = IRopeConnectable.getFrom(livingEntity).getRopeConnections();
        if(connections == null) return;

        connections.forEach(rope -> renderRope(livingEntity, tickDelta, matrices, vertexConsumers, rope.getConnectedTo()));
    }

    private <E extends Entity> void renderRope(LivingEntity mobEntity, float f, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, E entity) {
        matrixStack.push();
        Vec3d vec3d = entity.method_30951(f);
        double d = (double)(MathHelper.lerp(f, mobEntity.bodyYaw, mobEntity.prevBodyYaw) * 0.017453292F) + 1.5707963267948966D;
        Vec3d vec3d2 = mobEntity.method_29919();
        double e = Math.cos(d) * vec3d2.z + Math.sin(d) * vec3d2.x;
        double g = Math.sin(d) * vec3d2.z - Math.cos(d) * vec3d2.x;
        double h = MathHelper.lerp(f, mobEntity.prevX, mobEntity.getX()) + e;
        double i = MathHelper.lerp(f, mobEntity.prevY, mobEntity.getY()) + vec3d2.y;
        double j = MathHelper.lerp(f, mobEntity.prevZ, mobEntity.getZ()) + g;
        matrixStack.translate(e, vec3d2.y, g);
        float k = (float)(vec3d.x - h);
        float l = (float)(vec3d.y - i);
        float m = (float)(vec3d.z - j);
        float n = 0.025F;
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getLeash());
        Matrix4f matrix4f = matrixStack.peek().getModel();
        float o = MathHelper.fastInverseSqrt(k * k + m * m) * 0.025F / 2.0F;
        float p = m * o;
        float q = k * o;
        BlockPos blockPos = new BlockPos(mobEntity.getCameraPosVec(f));
        BlockPos blockPos2 = new BlockPos(entity.getCameraPosVec(f));
        int r = this.getBlockLight(mobEntity, blockPos);
        int s = this.dispatcher.getRenderer(entity).getBlockLight(entity, blockPos2);
        int t = mobEntity.world.getLightLevel(LightType.SKY, blockPos);
        int u = mobEntity.world.getLightLevel(LightType.SKY, blockPos2);
        MobEntityRenderer.method_23186(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.025F, p, q);
        MobEntityRenderer.method_23186(vertexConsumer, matrix4f, k, l, m, r, s, t, u, 0.025F, 0.0F, p, q);
        matrixStack.pop();
    }

    @Shadow
    public abstract int getBlockLight(Entity entity, BlockPos blockPos);

}
