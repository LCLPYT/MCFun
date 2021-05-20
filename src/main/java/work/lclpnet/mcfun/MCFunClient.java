package work.lclpnet.mcfun;

import net.fabricmc.api.ClientModInitializer;
import work.lclpnet.mcfun.networking.MCNetworking;

public class MCFunClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        MCNetworking.registerClientPacketHandlers();
    }
}
