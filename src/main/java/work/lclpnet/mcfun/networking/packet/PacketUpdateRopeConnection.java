package work.lclpnet.mcfun.networking.packet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import work.lclpnet.mcfun.MCFun;
import work.lclpnet.mcfun.networking.IClientPacketHandler;
import work.lclpnet.mcfun.networking.IPacketDecoder;
import work.lclpnet.mcfun.networking.MCPacket;
import work.lclpnet.mcfun.rope.IRopeConnectable;
import work.lclpnet.mcfun.rope.Rope;

public class PacketUpdateRopeConnection extends MCPacket implements IClientPacketHandler {

    public static final Identifier ID = new Identifier(MCFun.MOD_ID,"update_rope_connection");

    private final Action action;
    private final int entityId, toEntityId;

    public PacketUpdateRopeConnection(Action action, LivingEntity entity, LivingEntity toEntity) {
        this(action, entity.getEntityId(), toEntity.getEntityId());
    }

    public PacketUpdateRopeConnection(Action action, int entityId, int toEntityId) {
        super(ID);
        this.action = action;
        this.entityId = entityId;
        this.toEntityId = toEntityId;
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void handleClient(MinecraftClient client, ClientPlayNetworkHandler handler, PacketSender sender) {
        ClientWorld world = client.world;
        if(world == null) throw new IllegalStateException("Client world is null");

        client.execute(() -> {
            LivingEntity entity = (LivingEntity) client.world.getEntityById(this.entityId);
            LivingEntity connectTo = (LivingEntity) client.world.getEntityById(this.toEntityId);

            if(action == Action.CONNECT) IRopeConnectable.getFrom(entity).addRopeConnection(new Rope(connectTo), false);
            else if(action == Action.DISCONNECT) IRopeConnectable.getFrom(entity).removeRopeConnection(new Rope(connectTo), false);
        });
    }

    @Override
    public void encodeTo(PacketByteBuf buffer) {
        buffer.writeEnumConstant(action);
        buffer.writeInt(this.entityId);
        buffer.writeInt(this.toEntityId);
    }

    public enum Action {

        CONNECT,
        DISCONNECT

    }

    public static class Decoder implements IPacketDecoder<PacketUpdateRopeConnection> {

        @Override
        public PacketUpdateRopeConnection decode(PacketByteBuf buffer) {
            Action action = buffer.readEnumConstant(Action.class);
            int entityId = buffer.readInt();
            int toEntityId = buffer.readInt();
            return new PacketUpdateRopeConnection(action, entityId, toEntityId);
        }
    }

}
