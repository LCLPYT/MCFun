package work.lclpnet.mcfun.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

public interface IPacketDecoder<T extends MCPacket> {

    T decode(PacketByteBuf buffer);

    @Environment(EnvType.CLIENT)
    default void handleClient(PacketContext ctx, MCPacket msg) {
        if(msg instanceof IClientPacketHandler) ((IClientPacketHandler) msg).handleClient(ctx);
        else System.err.printf("Unhandled packet \"%s\" received on client.%n", msg.getIdentifier());
    }

    default void handleServer(PacketContext ctx, MCPacket msg) {
        if(msg instanceof IServerPacketHandler) ((IServerPacketHandler) msg).handleServer(ctx);
        else System.err.printf("Unhandled packet \"%s\" received on server.%n", msg.getIdentifier());
    }

}
