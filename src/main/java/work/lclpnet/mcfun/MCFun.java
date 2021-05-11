package work.lclpnet.mcfun;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import work.lclpnet.mcfun.cmd.MCCommands;

public class MCFun implements ModInitializer {

	@Override
	public void onInitialize() {
		System.out.println("Hello from fabric");

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
			MCCommands.registerCommands(dispatcher);
		});
	}
}
