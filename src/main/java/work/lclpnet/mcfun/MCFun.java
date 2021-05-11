package work.lclpnet.mcfun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.util.ActionResult;
import work.lclpnet.mcfun.cmd.MCCommands;
import work.lclpnet.mcfun.rope.IRopeConnectable;

public class MCFun implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("Hello from fabric");

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> MCCommands.registerCommands(dispatcher));
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            System.out.printf("Connected to: %s\n", IRopeConnectable.getFrom(entity).getRopeConnections());
            return ActionResult.PASS;
        });
    }
}
