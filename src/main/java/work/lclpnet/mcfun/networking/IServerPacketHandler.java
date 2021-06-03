package work.lclpnet.mcfun.networking;

import net.fabricmc.fabric.api.network.PacketContext;

public interface IServerPacketHandler {

    void handleServer(PacketContext ctx);

}
