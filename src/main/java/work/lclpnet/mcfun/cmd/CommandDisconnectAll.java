package work.lclpnet.mcfun.cmd;

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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static net.minecraft.server.command.CommandManager.argument;

public class CommandDisconnectAll extends CommandBase {

    public CommandDisconnectAll() {
        super("disconnect-all");
    }

    @Override
    protected LiteralArgumentBuilder<ServerCommandSource> transform(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(MCCommands::permLevel2)
                .then(argument("target", EntityArgumentType.entities())
                        .executes(CommandDisconnectAll::exec));
    }

    private static int exec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        Collection<? extends Entity> entities = EntityArgumentType.getEntities(ctx, "target");

        if(entities.size() == 1) {
            Entity entity = entities.iterator().next();
            if(removeAllFrom(entity, true)) ctx.getSource().sendFeedback(new TranslatableText("commands.disconnect_all.success", entity.getName()).formatted(Formatting.GREEN), true);
            else ctx.getSource().sendError(new TranslatableText("commands.disconnect_all.no_connections", entity.getName()));
        } else if(entities.size() > 1) {
            int success = 0;

            for(Entity entity : entities)
                if (removeAllFrom(entity, false))
                    success++;

            ctx.getSource().sendFeedback(new TranslatableText("commands.disconnect_all.success_multiple", success).formatted(Formatting.GREEN), true);
        } else throw new IllegalStateException();

        return 0;
    }

    private static boolean removeAllFrom(Entity entity, boolean throwException) throws CommandSyntaxException {
        if(!(entity instanceof LivingEntity)) {
            if(throwException) throw new SimpleCommandExceptionType(new TranslatableText("commands.connect.entities.not-living", entity.getEntityName())).create();
            else return false;
        }

        IRopeNode node = IRopeNode.fromEntity((LivingEntity) entity);
        Set<LivingEntity> ropeConnectedEntities = node.getRopeConnectedEntities();
        if(ropeConnectedEntities == null || ropeConnectedEntities.isEmpty()) return false;

        new HashSet<>(ropeConnectedEntities).forEach(node::disconnectFrom);
        return true;
    }

}
