package work.lclpnet.mcfun.rope;

import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

public interface IRopeConnectable {

    @Nullable
    Set<Rope> getRopeConnections();

    void addRopeConnection(Rope rope);

    void removeRopeConnection(Rope rope);

    default boolean isConnectedTo(Entity entity) {
        Objects.requireNonNull(entity);

        Set<Rope> connections = getRopeConnections();
        if (connections == null) return false;

        return connections.stream().anyMatch(rope -> rope.getConnectedTo().equals(entity));
    }

    @Nonnull
    static IRopeConnectable getFrom(Entity entity) {
        return (IRopeConnectable) entity;
    }

}
