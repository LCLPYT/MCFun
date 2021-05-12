package work.lclpnet.mcfun.rope;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

public interface IRopeConnectable {

    @Nullable
    Set<Rope> getRopeConnections();

    void addRopeConnection(Rope rope, boolean sendPacket);

    void removeRopeConnection(Rope rope, boolean sendPacket);

    default boolean isConnectedTo(LivingEntity entity) {
        Objects.requireNonNull(entity);

        Set<Rope> connections = getRopeConnections();
        if(connections == null) return false;

        return connections.stream().anyMatch(rope -> rope.getConnectedTo().equals(entity));
    }

    @Nonnull
    static IRopeConnectable getFrom(LivingEntity entity) {
        return (IRopeConnectable) entity;
    }
}
