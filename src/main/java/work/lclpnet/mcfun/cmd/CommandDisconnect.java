package work.lclpnet.mcfun.cmd;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import work.lclpnet.mcfun.rope.IRopeConnectable;
import work.lclpnet.mcfun.rope.Rope;

import java.util.Optional;
import java.util.Set;

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
        Entity firstEntity = EntityArgumentType.getEntity(ctx, "entity1"),
                secondEntity = EntityArgumentType.getEntity(ctx, "entity2");

        if(firstEntity.equals(secondEntity)) throw ENTITIES_EQUAL.create();

        if(!(firstEntity instanceof LivingEntity))
            throw new SimpleCommandExceptionType(new TranslatableText("commands.connect.entities.not-living", firstEntity.getEntityName())).create();
        if(!(secondEntity instanceof LivingEntity))
            throw new SimpleCommandExceptionType(new TranslatableText("commands.connect.entities.not-living", secondEntity.getEntityName())).create();

        LivingEntity first = (LivingEntity) firstEntity;
        LivingEntity second = (LivingEntity) secondEntity;
        IRopeConnectable firstRopeConnectable = IRopeConnectable.getFrom(first);
        IRopeConnectable secondRopeConnectable = IRopeConnectable.getFrom(second);

        if(!firstRopeConnectable.isConnectedTo(second) && !secondRopeConnectable.isConnectedTo(first)) throw ENTITIES_NOT_LINKED.create();

        Set<Rope> firstConnections = firstRopeConnectable.getRopeConnections();
        Set<Rope> secondConnections = secondRopeConnectable.getRopeConnections();

        if(firstConnections == null || secondConnections == null) {
            System.err.println("Error connections are null");
            return 0;
        }

        Optional<Rope> firstRope = firstConnections.stream().filter(r -> r.getConnectedTo().equals(second)).findFirst();
        Optional<Rope> secondRope = secondConnections.stream().filter(r -> r.getConnectedTo().equals(first)).findFirst();
        firstRope.ifPresent(rope -> firstRopeConnectable.removeRopeConnection(rope, true));
        secondRope.ifPresent(rope -> secondRopeConnectable.removeRopeConnection(rope, true));

        MutableText feedback = new TranslatableText("commands.disconnect.entities.disconnected", firstEntity.getName(), secondEntity.getName()).formatted(Formatting.RED);
        ctx.getSource().sendFeedback(feedback, true);

        return 0;
    }
}
