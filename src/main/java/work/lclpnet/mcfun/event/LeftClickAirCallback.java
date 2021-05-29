package work.lclpnet.mcfun.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;

public interface LeftClickAirCallback {

    Event<LeftClickAirCallback> EVENT = EventFactory.createArrayBacked(LeftClickAirCallback.class,
            (listeners) -> (player, world) -> {
                for (LeftClickAirCallback event : listeners) {
                    ActionResult result = event.interact(player, world);

                    if (result != ActionResult.PASS) return result;
                }

                return ActionResult.PASS;
            }
    );

    ActionResult interact(PlayerEntity player, World world);
}
