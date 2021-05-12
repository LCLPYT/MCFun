package work.lclpnet.mcfun.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import work.lclpnet.mcfun.rope.IRopeConnectable;
import work.lclpnet.mcfun.rope.Rope;

import java.util.Optional;

import static net.minecraft.server.command.CommandManager.argument;

public class CommandDisconnect extends CommandBase {

    private static final SimpleCommandExceptionType ENTITIES_EQUAL = new SimpleCommandExceptionType(new TranslatableText("commands.disconnect.entities.equal"));
    private static final SimpleCommandExceptionType ENTITIES_NOT_LINKED = new SimpleCommandExceptionType(new TranslatableText("commands.disconnect.entities.not-linked"));

    public CommandDisconnect() {
        super("disconnect");
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> transform(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(MCCommands::permLevel2)
                .then(argument("entity1", EntityArgumentType.entity())
                        .then(argument("entity2", EntityArgumentType.entity())
                                .executes(CommandDisconnect::disconnect)));
    }

    private static int disconnect(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Entity entity1 = EntityArgumentType.getEntity(ctx, "entity1"),
                entity2 = EntityArgumentType.getEntity(ctx, "entity2");

        if(entity1.equals(entity2)) throw ENTITIES_EQUAL.create();

        IRopeConnectable conn1 = IRopeConnectable.getFrom(entity1);
        IRopeConnectable conn2 = IRopeConnectable.getFrom(entity2);

        if(!conn1.isConnectedTo(entity2) && !conn2.isConnectedTo(entity1)) throw ENTITIES_NOT_LINKED.create();

        Optional<Rope> first = conn1.getRopeConnections().stream().findFirst();
        Optional<Rope> first1 = conn2.getRopeConnections().stream().findFirst();

        Rope fst = first.get();
        conn1.removeRopeConnection((Rope) fst);
        Rope fst1 = first1.get();
        conn2.removeRopeConnection((Rope) fst1);

        MutableText feedback = new TranslatableText("commands.disconnect.entities.disconnected", entity1.getName(), entity2.getName()).formatted(Formatting.RED);
        ctx.getSource().sendFeedback(feedback, true);

        return 0;
    }
}
