package work.lclpnet.mcfun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import work.lclpnet.mcfun.asm.type.IRopeNode;
import work.lclpnet.mcfun.cmd.base.MCCommands;
import work.lclpnet.mcfun.event.EntityTrackedSpawnPacketsCallback;
import work.lclpnet.mcfun.event.LeftClickAirCallback;
import work.lclpnet.mcfun.event.UseItemAirCallback;
import work.lclpnet.mcfun.item.MCItems;
import work.lclpnet.mcfun.item.RopeItem;
import work.lclpnet.mcfun.networking.MCNetworking;
import work.lclpnet.mcfun.networking.packet.PacketUpdateRopeConnection;
import work.lclpnet.mcfun.util.Rope;

import java.util.Set;

public class MCFun implements ModInitializer {

	public static final String MOD_ID = "mcfun";

	@Override
	public void onInitialize() {
		MCNetworking.registerPackets();
		MCNetworking.registerServerPacketHandlers();
		MCItems.register();

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> MCCommands.registerCommands(dispatcher));

		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if(world.isClient || !(entity instanceof LivingEntity) || !(player instanceof ServerPlayerEntity)) return ActionResult.PASS;

			ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
			if(stack == null || stack.isEmpty() || !stack.getItem().equals(MCItems.ROPE_ITEM)) return ActionResult.PASS;

			RopeItem.select((ServerPlayerEntity) player, (LivingEntity) entity);
			return ActionResult.SUCCESS;
		});

		LeftClickAirCallback.EVENT.register((player, world) -> {
			if(world.isClient) return ActionResult.PASS;

			ItemStack stack = player.getStackInHand(Hand.MAIN_HAND);
			if(stack == null || stack.isEmpty() || !stack.getItem().equals(MCItems.ROPE_ITEM)) return ActionResult.PASS;

			RopeItem.select((ServerPlayerEntity) player, player);
			return ActionResult.PASS;
		});

		UseItemAirCallback.EVENT.register((player, world, hand) -> {
			if(world.isClient || !(player instanceof ServerPlayerEntity)) return ActionResult.PASS;

			ItemStack stack = player.getStackInHand(hand);
			if(stack == null || stack.isEmpty() || !stack.getItem().equals(MCItems.ROPE_ITEM)) return ActionResult.PASS;

			return RopeItem.useOn((ServerPlayerEntity) player, player) ? ActionResult.FAIL : ActionResult.PASS;
		});

		EntityTrackedSpawnPacketsCallback.EVENT.register((sender, entity) -> {
			if(!(entity instanceof LivingEntity)) return;
			LivingEntity living = (LivingEntity) entity;

			IRopeNode node = IRopeNode.fromEntity(living);
			Set<LivingEntity> connected = node.getRopeConnectedEntities();
			if(connected == null) return;

			connected.forEach(conn -> {
				Rope rope = node.getRopeConnection(conn);
				if(rope != null) {
					PacketUpdateRopeConnection packet = PacketUpdateRopeConnection.createConnectPacket(living, conn, rope);
					sender.accept(MCNetworking.createVanillaS2CPacket(packet)); // send the packet
				}
			});
		});
	}
}
