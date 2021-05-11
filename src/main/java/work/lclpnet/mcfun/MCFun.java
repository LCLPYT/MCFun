package work.lclpnet.mcfun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import work.lclpnet.mcfun.cmd.MCCommands;

import static net.minecraft.server.command.CommandManager.argument;

public class MCFun implements ModInitializer {

	@Override
	public void onInitialize() {
		System.out.println("Hello from fabric");

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			MCCommands.registerCommands(dispatcher);
			dispatcher.register(CommandManager.literal("connect")
					.requires(source -> source.hasPermissionLevel(2))
					.then(argument("player1", EntityArgumentType.player())
							.then(argument("player2", EntityArgumentType.player())
									.executes(ctx -> connectPlayers(EntityArgumentType.getPlayer(ctx, "player1"), EntityArgumentType.getPlayer(ctx, "player2"))))));
		});
	}

	public static int connectPlayers(ServerPlayerEntity p1, ServerPlayerEntity p2){
		System.out.println("You have created a chain!!");
		return 0;
	}

}
