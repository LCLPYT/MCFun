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

import java.util.Objects;
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

        if(!(entity1 instanceof LivingEntity))
            throw new SimpleCommandExceptionType(new TranslatableText("commands.connect.entities.not-living", entity1.getEntityName())).create();
        if(!(entity2 instanceof LivingEntity))
            throw new SimpleCommandExceptionType(new TranslatableText("commands.connect.entities.not-living", entity2.getEntityName())).create();

        LivingEntity le1 = (LivingEntity) entity1;
        LivingEntity le2 = (LivingEntity) entity2;

        IRopeConnectable conn1 = IRopeConnectable.getFrom(le1);
        IRopeConnectable conn2 = IRopeConnectable.getFrom(le2);

        if(!conn1.isConnectedTo(le2) && !conn2.isConnectedTo(le1)) throw ENTITIES_NOT_LINKED.create();

        Optional<Rope> e1toe2 = Objects.requireNonNull(conn1.getRopeConnections()).stream().filter(rope -> rope.getConnectedTo().equals(entity2)).findFirst();
        Optional<Rope> e2toe1 = Objects.requireNonNull(conn2.getRopeConnections()).stream().filter(rope -> rope.getConnectedTo().equals(entity1)).findFirst();

        if (e1toe2.isPresent()) {
            Rope rope = e1toe2.get();
            conn1.removeRopeConnection(rope, true);
        }

        if (e2toe1.isPresent()) {
            Rope rope2 = e2toe1.get();
            conn2.removeRopeConnection(rope2, true);
        }

        MutableText feedback = new TranslatableText("commands.disconnect.entities.disconnected", entity1.getName(), entity2.getName()).formatted(Formatting.RED);
        ctx.getSource().sendFeedback(feedback, true);

        return 0;
    }
}
