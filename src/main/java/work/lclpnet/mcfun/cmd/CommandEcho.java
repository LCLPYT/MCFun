package work.lclpnet.mcfun.cmd;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class CommandEcho extends CommandBase {

    public CommandEcho() {
        super("echo");
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> transform(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(MCCommands::permLevel2)
                .executes(CommandEcho::doEcho)
                .then(CommandManager.argument("message", StringArgumentType.string())
                        .executes(CommandEcho::doEchoMessage));
    }

    private static int doEcho(CommandContext<ServerCommandSource> ctx) {
        System.out.println("ECHO FROM COMMAND");
        return 0;
    }

    private static int doEchoMessage(CommandContext<ServerCommandSource> ctx) {
        String msg = ctx.getArgument("message", String.class);
        System.out.println(msg);
        return 0;
    }

}
