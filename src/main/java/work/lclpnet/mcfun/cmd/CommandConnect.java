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

import static net.minecraft.server.command.CommandManager.argument;

public class CommandConnect extends CommandBase {

    private static final SimpleCommandExceptionType ENTITIES_EQUAL = new SimpleCommandExceptionType(new TranslatableText("commands.connect.entities.equal"));
    private static final SimpleCommandExceptionType ENTITIES_ALREADY_LINKED = new SimpleCommandExceptionType(new TranslatableText("commands.connect.entities.already-linked"));

    public CommandConnect() {
        super("connect");
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> transform(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(MCCommands::permLevel2)
                .requires(MCCommands::isPlayer)
                .then(argument("entity1", EntityArgumentType.entity())
                        .then(argument("entity2", EntityArgumentType.entity())
                                .executes(CommandConnect::connect)));
    }

    private static int connect(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
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

        if(firstRopeConnectable.isConnectedTo(second) || secondRopeConnectable.isConnectedTo(first)) throw ENTITIES_ALREADY_LINKED.create();

        firstRopeConnectable.addRopeConnection(new Rope(second), true);
        secondRopeConnectable.addRopeConnection(new Rope(first), true);

        MutableText feedback = new TranslatableText("commands.connect.entities.connected", firstEntity.getName(), secondEntity.getName()).formatted(Formatting.GREEN);
        ctx.getSource().sendFeedback(feedback, true);

        return 0;
    }
}
