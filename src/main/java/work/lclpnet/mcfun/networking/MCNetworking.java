package work.lclpnet.mcfun.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class MCNetworking {
    public static final Identifier CHANNEL_ID = new Identifier("mcfun","add_rope_connection");

    public static void sendAddRopePacket(ServerPlayerEntity player, LivingEntity le, LivingEntity connectedTo){
            PacketByteBuf buf = PacketByteBufs.create();
            buf.writeInt(le.getEntityId());
            buf.writeInt(connectedTo.getEntityId());

            ServerPlayNetworking.send(player, CHANNEL_ID, buf);
    }
}
