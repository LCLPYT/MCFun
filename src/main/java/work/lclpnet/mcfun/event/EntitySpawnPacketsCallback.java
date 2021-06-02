package work.lclpnet.mcfun.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;

import java.util.function.Consumer;

public interface EntitySpawnPacketsCallback {

    Event<EntitySpawnPacketsCallback> EVENT = EventFactory.createArrayBacked(EntitySpawnPacketsCallback.class,
            listeners -> (sender, entity) -> {
                for (EntitySpawnPacketsCallback event : listeners)
                    event.onSendSpawnPackets(sender, entity);
            }
    );

    void onSendSpawnPackets(Consumer<Packet<?>> sender, Entity entity);
}
