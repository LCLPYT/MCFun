package work.lclpnet.mcfun.cmd;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import work.lclpnet.mcfun.asm.type.IRopeNode;
import work.lclpnet.mcfun.cmd.base.CommandBase;
import work.lclpnet.mcfun.cmd.base.MCCommands;
import work.lclpnet.mcfun.util.Rope;

import static net.minecraft.server.command.CommandManager.argument;

public class CommandSetRopeLength extends CommandBase {

    public CommandSetRopeLength() {
        super("set-rope-length");
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> transform(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(MCCommands::permLevel2)
                .then(argument("entity1", EntityArgumentType.entity())
                        .then(argument("entity2", EntityArgumentType.entity())
                                .then(argument("length", FloatArgumentType.floatArg(1F))
                                        .executes(this::exec))));
    }

    private int exec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Entity firstEntity = EntityArgumentType.getEntity(ctx, "entity1"),
                secondEntity = EntityArgumentType.getEntity(ctx, "entity2");

        if(firstEntity.equals(secondEntity)) throw CommandConnect.ENTITIES_EQUAL.create();

        if(!(firstEntity instanceof LivingEntity))
            throw new SimpleCommandExceptionType(new TranslatableText("commands.connect.entities.not-living", firstEntity.getEntityName())).create();
        if(!(secondEntity instanceof LivingEntity))
            throw new SimpleCommandExceptionType(new TranslatableText("commands.connect.entities.not-living", secondEntity.getEntityName())).create();

        LivingEntity first = (LivingEntity) firstEntity;
        LivingEntity second = (LivingEntity) secondEntity;
        IRopeNode firstNode = IRopeNode.fromEntity(first);

        if(!firstNode.isConnectedTo(second)) throw CommandDisconnect.ENTITIES_NOT_LINKED.create();

        float length = FloatArgumentType.getFloat(ctx, "length");
        Rope rope = firstNode.getRopeConnection(second);
        if(rope != null) {
            rope.setLength(length);
            ctx.getSource().sendFeedback(new TranslatableText("commands.set_rope_length.success", length).formatted(Formatting.GREEN), true);
        }

        return 0;
    }

}
