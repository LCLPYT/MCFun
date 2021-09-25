package work.lclpnet.mcfun.networking.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import work.lclpnet.mcfun.MCFun;
import work.lclpnet.mcfun.asm.type.IRopeNode;
import work.lclpnet.mcfun.networking.IClientPacketHandler;
import work.lclpnet.mcfun.networking.IPacketDecoder;
import work.lclpnet.mcfun.networking.MCPacket;
import work.lclpnet.mcfun.util.Rope;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Consumer;

public class PacketUpdateRopeConnection extends MCPacket implements IClientPacketHandler {

    public static final Identifier ID = new Identifier(MCFun.MOD_ID,"update_rope_connection");

    private final Action action;
    private final int entityId, toEntityId;
    @Nullable
    private final Rope rope;

    protected PacketUpdateRopeConnection(Action action, int entityId, int toEntityId, @Nullable Rope rope) {
        super(ID);
        this.action = action;
        this.entityId = entityId;
        this.toEntityId = toEntityId;
        this.rope = rope;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(PacketContext ctx) {
        if(!(ctx instanceof ClientPlayNetworkHandler)) throw new IllegalStateException("Context is invalid");
        ClientPlayNetworkHandler handler = (ClientPlayNetworkHandler) ctx;
        ctx.getTaskQueue().execute(() -> handlePacket(handler, 0, run -> ctx.getTaskQueue().execute(run)));
    }

    @Environment(EnvType.CLIENT)
    private void handlePacket(ClientPlayNetworkHandler handler, int nTry, Consumer<Runnable> executor) {
        ClientWorld world = handler.getWorld();
        LivingEntity entity = (LivingEntity) world.getEntityById(this.entityId);

        if (entity == null) {
            if (nTry > 40) System.err.printf("Entity with id %s is unknown to the client.%n", this.entityId);
            else executor.accept(() -> handlePacket(handler, nTry + 1, executor)); // try again a tick later
            return;
        }

        if (action == Action.CONNECT) {
            Objects.requireNonNull(this.rope, String.format("Rope might not be null with %s packets.", action));

            LivingEntity other = (LivingEntity) world.getEntityById(this.toEntityId);
            Rope otherRope = null;
            if (other != null)
                otherRope = IRopeNode.fromEntity(other).getClientRopeConnection(this.entityId);

            IRopeNode.fromEntity(entity).addClientRopeConnection(this.toEntityId, otherRope == null ? this.rope : otherRope);
        } else if (action == Action.DISCONNECT) {
            IRopeNode.fromEntity(entity).removeClientRopeConnection(this.toEntityId);
        } else if (action == Action.UPDATE_PROPERTIES) {
            Objects.requireNonNull(this.rope, String.format("Rope might not be null with %s packets.", action));

            Rope rope = IRopeNode.fromEntity(entity).getClientRopeConnection(this.toEntityId);
            if (rope == null)
                throw new IllegalStateException("Trying to update a rope, which does not exist on the client.");

            rope.acceptUpdate(this.rope);
        }
    }

    @Override
    public void encodeTo(PacketByteBuf buffer) {
        buffer.writeEnumConstant(action);
        buffer.writeInt(this.entityId);
        buffer.writeInt(this.toEntityId);

        boolean ropePresent = this.rope != null;
        buffer.writeBoolean(ropePresent);
        if(ropePresent) rope.encodeTo(buffer);
    }

    public static PacketUpdateRopeConnection createConnectPacket(LivingEntity livingEntity, LivingEntity other, @Nonnull Rope rope) {
        return new PacketUpdateRopeConnection(Action.CONNECT, livingEntity.getEntityId(), other.getEntityId(), rope);
    }

    public static PacketUpdateRopeConnection createDisconnectPacket(LivingEntity livingEntity, LivingEntity other) {
        return new PacketUpdateRopeConnection(Action.DISCONNECT, livingEntity.getEntityId(), other.getEntityId(), null);
    }

    public static PacketUpdateRopeConnection createUpdatePropertiesPacket(LivingEntity livingEntity, LivingEntity other, @Nonnull Rope rope) {
        return new PacketUpdateRopeConnection(Action.UPDATE_PROPERTIES, livingEntity.getEntityId(), other.getEntityId(), rope);
    }

    protected enum Action {
        CONNECT,
        DISCONNECT,
        UPDATE_PROPERTIES
    }

    public static class Decoder implements IPacketDecoder<PacketUpdateRopeConnection> {

        @Override
        public PacketUpdateRopeConnection decode(PacketByteBuf buffer) {
            Action action = buffer.readEnumConstant(Action.class);
            int entityId = buffer.readInt();
            int toEntityId = buffer.readInt();

            boolean ropePresent = buffer.readBoolean();
            Rope rope = null;
            if(ropePresent) rope = Rope.decodeFrom(buffer);

            return new PacketUpdateRopeConnection(action, entityId, toEntityId, rope);
        }
    }

}
