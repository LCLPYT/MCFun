package work.lclpnet.mcfun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.ActionResult;
import work.lclpnet.mcfun.cmd.MCCommands;
import work.lclpnet.mcfun.networking.MCNetworking;
import work.lclpnet.mcfun.rope.IRopeConnectable;

public class MCFun implements ModInitializer {

	public static final String MOD_ID = "mcfun";

	@Override
	public void onInitialize() {
		MCNetworking.registerPackets();

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> MCCommands.registerCommands(dispatcher));

		// remove later
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(!(entity instanceof LivingEntity) || !world.isClient) return ActionResult.PASS;
			// called on client, when living entites are attacked by the client player.
			LivingEntity le = (LivingEntity) entity;
			System.out.println("attacked: " + entity);

//			IRopeConnectable.getFrom(le).addRopeConnection(new Rope(player), false);
			System.out.print("attacked connections: ");
			System.out.println(IRopeConnectable.getFrom(le).getRopeConnections());

			System.out.print("player conntions: ");
			System.out.println(IRopeConnectable.getFrom(player).getRopeConnections());
			return ActionResult.PASS;
		});
	}
}
