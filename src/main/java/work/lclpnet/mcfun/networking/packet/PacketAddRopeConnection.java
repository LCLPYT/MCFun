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

public class PacketAddRopeConnection extends MCPacket implements IClientPacketHandler {

    public static final Identifier ID = new Identifier(MCFun.MOD_ID,"add_rope_connection");

    private final int entityId, toEntityId;

    public PacketAddRopeConnection(LivingEntity entity, LivingEntity toEntity) {
        this(entity.getEntityId(), toEntity.getEntityId());
    }

    public PacketAddRopeConnection(int entityId, int toEntityId) {
        super(ID);
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

            IRopeConnectable.getFrom(entity).addRopeConnection(new Rope(connectTo), false);
        });
    }

    @Override
    public void encodeTo(PacketByteBuf buffer) {
        buffer.writeInt(this.entityId);
        buffer.writeInt(this.toEntityId);
    }

    public static class Decoder implements IPacketDecoder<PacketAddRopeConnection> {

        @Override
        public PacketAddRopeConnection decode(PacketByteBuf buffer) {
            int entityId = buffer.readInt();
            int toEntityId = buffer.readInt();
            return new PacketAddRopeConnection(entityId, toEntityId);
        }
    }

}
