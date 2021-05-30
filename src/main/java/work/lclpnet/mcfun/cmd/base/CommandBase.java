package work.lclpnet.mcfun.cmd.base;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class CommandBase {

    protected String name;
    protected List<String> aliases = null;

    public CommandBase(String name) {
        this.name = Objects.requireNonNull(name);
    }

    protected abstract LiteralArgumentBuilder<ServerCommandSource> transform(LiteralArgumentBuilder<ServerCommandSource> builder);

    public List<String> getAliases() {
        return aliases;
    }

    public String getName() {
        return name;
    }

    public CommandBase setName(String name) {
        this.name = name;
        return this;
    }

    public CommandBase setAliases(List<String> aliases) {
        this.aliases = aliases;
        return this;
    }

    public CommandBase addAlias(String alias) {
        Objects.requireNonNull(alias);
        if(aliases == null) aliases = new ArrayList<>();
        aliases.add(alias);
        return this;
    }

    public void register(final CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(transform(CommandManager.literal(name)));

        /* Aliases */
        if (aliases != null) aliases.forEach(alias -> dispatcher.register(transform(CommandManager.literal(alias))));
    }

}
