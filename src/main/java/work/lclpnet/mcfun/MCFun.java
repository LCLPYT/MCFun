package work.lclpnet.mcfun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import work.lclpnet.mcfun.cmd.base.MCCommands;
import work.lclpnet.mcfun.event.LeftClickAirCallback;
import work.lclpnet.mcfun.item.MCItems;
import work.lclpnet.mcfun.item.RopeItem;
import work.lclpnet.mcfun.networking.MCNetworking;

public class MCFun implements ModInitializer {

	public static final String MOD_ID = "mcfun";

	@Override
	public void onInitialize() {
		MCNetworking.registerPackets();
		MCNetworking.registerServerPacketHandlers();
		MCItems.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> MCCommands.registerCommands(dispatcher));

		LeftClickAirCallback.EVENT.register((player, world) -> {
			if(world.isClient) return ActionResult.PASS;

			ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
			if(stack == null || stack.isEmpty() || !stack.getItem().equals(MCItems.ROPE_ITEM)) return ActionResult.PASS;

			return RopeItem.useOn(player, player) ? ActionResult.FAIL : ActionResult.SUCCESS;
		});
	}
}
