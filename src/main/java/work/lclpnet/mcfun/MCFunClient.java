package work.lclpnet.mcfun;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.LivingEntity;
import work.lclpnet.mcfun.networking.MCNetworking;
import work.lclpnet.mcfun.rope.IRopeConnectable;
import work.lclpnet.mcfun.rope.Rope;

public class MCFunClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(MCNetworking.CHANNEL_ID, (client, handler, buf, responseSender) -> {
            int leID = buf.readInt();
            int connectedToID = buf.readInt();

            client.execute(() -> {
                LivingEntity le = (LivingEntity) client.world.getEntityById(leID);
                LivingEntity connectTo = (LivingEntity) client.world.getEntityById(connectedToID);

                IRopeConnectable.getFrom(le).addRopeConnection(new Rope(connectTo), false);
            });
        });
    }
}
