package work.lclpnet.mcfun.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import work.lclpnet.mcfun.networking.packet.PacketLeftClickAir;
import work.lclpnet.mcfun.networking.packet.PacketUpdateRopeConnection;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static work.lclpnet.mcfun.asm.InstanceMixinHelper.castTo;

public class MCNetworking {

    private static final Map<Identifier, IPacketDecoder<? extends MCPacket>> packetDecoderMap = new HashMap<>();

    /* */

    public static void registerPackets() {
        register(PacketUpdateRopeConnection.ID, new PacketUpdateRopeConnection.Decoder());
        register(PacketLeftClickAir.ID, buffer -> new PacketLeftClickAir());
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

    public static void sendToAllTrackingIncludingSelf(LivingEntity living, MCPacket packet) {
        MCNetworking.sendToAllTracking(living, packet);

        // if the entity is a player, the packet will not be sent to the player, since players do not track themselves.
        if (living instanceof ServerPlayerEntity)
            MCNetworking.sendPacketTo(packet, castTo(living, ServerPlayerEntity.class));
    }

    public static Packet<?> createVanillaS2CPacket(MCPacket packet) {
        Objects.requireNonNull(packet, "Packet cannot be null");

        Identifier channelName = packet.getIdentifier();
        Objects.requireNonNull(channelName, "Channel name cannot be null");

        PacketByteBuf buf = PacketByteBufs.create();
        packet.encodeTo(buf);

        return ServerPlayNetworking.createS2CPacket(channelName, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void sendPacketToServer(MCPacket packet) {
        PacketByteBuf buf = PacketByteBufs.create();
        packet.encodeTo(buf);
        ClientPlayNetworking.send(packet.getIdentifier(), buf);
    }
}
