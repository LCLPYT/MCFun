package work.lclpnet.mcfun.networking;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.fabricmc.fabric.api.server.PlayerStream;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import work.lclpnet.mcfun.networking.packet.PacketLeftClickAir;
import work.lclpnet.mcfun.networking.packet.PacketRopeSelection;
import work.lclpnet.mcfun.networking.packet.PacketUpdateRopeConnection;
import work.lclpnet.mcfun.networking.packet.PacketUseItemAir;

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
        register(PacketUseItemAir.ID, new PacketUseItemAir.Decoder());
        register(PacketRopeSelection.ID, new PacketRopeSelection.Decoder());
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
                (id, serializer) -> ClientSidePacketRegistry.INSTANCE.register(id,
                        (context, buf) -> serializer.handleClient(context, serializer.decode(buf)))
        );
    }

    public static void registerServerPacketHandlers() {
        packetDecoderMap.forEach(
                (id, serializer) -> ServerSidePacketRegistry.INSTANCE.register(id,
                        (context, buf) -> serializer.handleServer(context, serializer.decode(buf)))
        );
    }

    public static void sendPacketTo(MCPacket packet, PlayerEntity player) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.encodeTo(buf);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, packet.getIdentifier(), buf);
    }

    public static void sendToAllTracking(Entity tracked, MCPacket packet) {
        Objects.requireNonNull(tracked);
        Objects.requireNonNull(packet);
        PlayerStream.watching(tracked).forEach(p -> sendPacketTo(packet, p));
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

        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.encodeTo(buf);

        return ServerSidePacketRegistry.INSTANCE.toPacket(channelName, buf);
    }

    @Environment(EnvType.CLIENT)
    public static void sendPacketToServer(MCPacket packet) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        packet.encodeTo(buf);
        ClientSidePacketRegistry.INSTANCE.sendToServer(packet.getIdentifier(), buf);
    }
}
