package work.lclpnet.mcfun.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;

import java.util.function.Consumer;

/**
 * Called when an entity is tracked by a player.
 * Does not fire when the world is loaded and the player is nearby.
 * In that case, data packets should be sent by injecting a deserialize method of the entity.
 */
public interface EntityTrackedSpawnPacketsCallback {

    Event<EntityTrackedSpawnPacketsCallback> EVENT = EventFactory.createArrayBacked(EntityTrackedSpawnPacketsCallback.class,
            listeners -> (sender, entity) -> {
                for (EntityTrackedSpawnPacketsCallback event : listeners)
                    event.onSendSpawnPackets(sender, entity);
            }
    );

    void onSendSpawnPackets(Consumer<Packet<?>> sender, Entity entity);
}
