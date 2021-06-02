package work.lclpnet.mcfun.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public interface UseItemAirCallback {

    Event<UseItemAirCallback> EVENT = EventFactory.createArrayBacked(UseItemAirCallback.class,
            (listeners) -> (player, world, hand) -> {
                for (UseItemAirCallback event : listeners) {
                    ActionResult result = event.interact(player, world, hand);

                    if (result != ActionResult.PASS) return result;
                }

                return ActionResult.PASS;
            }
    );

    ActionResult interact(PlayerEntity player, World world, Hand hand);
}
