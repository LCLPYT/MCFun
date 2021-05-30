package work.lclpnet.mcfun.cmd.base;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import work.lclpnet.mcfun.cmd.CommandConnect;
import work.lclpnet.mcfun.cmd.CommandDisconnect;
import work.lclpnet.mcfun.cmd.CommandDisconnectAll;

import java.util.function.Predicate;

public class MCCommands {

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        new CommandConnect().register(dispatcher);
        new CommandDisconnect().register(dispatcher);
        new CommandDisconnectAll().register(dispatcher);
    }

    /* */

    public static boolean permLevel1(ServerCommandSource cs) {
        return cs.hasPermissionLevel(1);
    }

    public static boolean permLevel2(ServerCommandSource cs) {
        return cs.hasPermissionLevel(2);
    }

    public static boolean permLevel3(ServerCommandSource cs) {
        return cs.hasPermissionLevel(3);
    }

    public static boolean permLevel4(ServerCommandSource cs) {
        return cs.hasPermissionLevel(4);
    }

    public static boolean isPlayer(ServerCommandSource cs) {
        return cs.getEntity() != null && cs.getEntity() instanceof PlayerEntity;
    }

    public static boolean isEntity(ServerCommandSource cs) {
        return cs.getEntity() != null;
    }

    public static boolean isPlayerPermLevel2(ServerCommandSource cs) {
        return permLevel2(cs) && isPlayer(cs);
    }

    @SafeVarargs
    public static boolean requires(ServerCommandSource cs, Predicate<ServerCommandSource>... predicates) {
        for (Predicate<ServerCommandSource> p : predicates)
            if (!p.test(cs)) return false;
        return true;
    }

}
