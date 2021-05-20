package work.lclpnet.mcfun.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import work.lclpnet.mcfun.networking.packet.PacketUpdateRopeConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MCNetworking {

    private static final Map<Identifier, IPacketDecoder<? extends MCPacket>> packetDecoderMap = new HashMap<>();

    /* */

    public static void registerPackets() {
        register(PacketUpdateRopeConnection.ID, new PacketUpdateRopeConnection.Decoder());
    }

    /* */

    private static void register(Identifier id, IPacketDecoder<? extends MCPacket> serializer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(serializer);

        packetDecoderMap.put(id, serializer);
    }

    @Environment(EnvType.CLIENT)
    public static void registerClientPacketHandlers() {
        packetDecoderMap.forEach(
                (id, serializer) -> ClientPlayNetworking.registerGlobalReceiver(id,
                        (client, handler, buf, responseSender) -> serializer.handleClient(serializer.decode(buf), client, handler, responseSender))
        );
    }

    @Environment(EnvType.SERVER)
    public static void registerServerPacketHandlers() {
        packetDecoderMap.forEach(
                (id, serializer) -> ServerPlayNetworking.registerGlobalReceiver(id,
                        (server, player, handler, buf, responseSender) -> serializer.handleServer(serializer.decode(buf), server, player, handler, responseSender))
        );
    }

    public static void sendPacketTo(MCPacket packet, ServerPlayerEntity player) {
        PacketByteBuf buf = PacketByteBufs.create();
        packet.encodeTo(buf);
        ServerPlayNetworking.send(player, packet.getIdentifier(), buf);
    }

    public static void sendToAllTracking(Entity tracked, MCPacket packet) {
        Objects.requireNonNull(tracked);
        Objects.requireNonNull(packet);
        PlayerLookup.tracking(tracked).forEach(p -> sendPacketTo(packet, p));
    }

}
