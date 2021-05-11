package work.lclpnet.mcfun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;

public class MCFun implements ModInitializer {

	@Override
	public void onInitialize() {
		System.out.println("Hello from fabric");
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			dispatcher.register(CommandManager.literal("connect").executes(ctx -> {
				System.out.println("You created a Connection :)");
				return 0;
			}));
		});
	}
}
