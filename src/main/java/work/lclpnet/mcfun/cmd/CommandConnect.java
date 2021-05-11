package work.lclpnet.mcfun.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static net.minecraft.server.command.CommandManager.argument;

public class CommandConnect extends CommandBase {

    public CommandConnect() {
        super("connect");
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> transform(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(MCCommands::permLevel2)
                .then(argument("player1", EntityArgumentType.player())
                        .then(argument("player2", EntityArgumentType.player())
                                .executes(ctx -> connectPlayers(EntityArgumentType.getPlayer(ctx, "player1"), EntityArgumentType.getPlayer(ctx, "player2")))));
    }

    public static int connectPlayers(ServerPlayerEntity p1, ServerPlayerEntity p2){
        System.out.println("You have created a chain!!");
        return 0;
    }
}
