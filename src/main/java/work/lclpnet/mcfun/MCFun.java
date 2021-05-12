package work.lclpnet.mcfun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import work.lclpnet.mcfun.cmd.MCCommands;
import work.lclpnet.mcfun.rope.IRopeConnectable;
import work.lclpnet.mcfun.rope.Rope;

public class MCFun implements ModInitializer {

	@Override
	public void onInitialize() {
		System.out.println("Hello from fabric");

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> MCCommands.registerCommands(dispatcher));

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(!(entity instanceof LivingEntity) || !world.isClient) return ActionResult.PASS;
			LivingEntity le = (LivingEntity) entity;

			IRopeConnectable.getFrom(le).addRopeConnection(new Rope(player), false);
			System.out.println(IRopeConnectable.getFrom(le).getRopeConnections());
			return ActionResult.PASS;
		});
	}
}
