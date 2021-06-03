package work.lclpnet.mcfun.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.PacketContext;

public interface IClientPacketHandler {

    @Environment(EnvType.CLIENT)
    void handleClient(PacketContext ctx);

}
