package work.lclpnet.mcfun;

import net.fabricmc.api.DedicatedServerModInitializer;
import work.lclpnet.mcfun.networking.MCNetworking;

public class MCFunServer implements DedicatedServerModInitializer {

    @Override
    public void onInitializeServer() {
        MCNetworking.registerServerPacketHandlers();
    }

}
