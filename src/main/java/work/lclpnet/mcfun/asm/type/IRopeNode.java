package work.lclpnet.mcfun.asm.type;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

import static work.lclpnet.mcfun.asm.InstanceMixinHelper.castTo;

public interface IRopeNode {

    @Nullable
    Set<LivingEntity> getRopeConnectedEntities();

    void connectWith(LivingEntity entity);

    void disconnectFrom(LivingEntity entity);

    default boolean isConnectedTo(LivingEntity entity) {
        Objects.requireNonNull(entity);

        Set<LivingEntity> connections = getRopeConnectedEntities();
        if(connections == null) return false;

        return connections.stream().anyMatch(connected -> connected.equals(entity));
    }

    /**
     * Comfort method for connecting two entities with each other.
     * Establishes a rope connection between this entity and the given entity.
     * A rope connection will be created for this entity and for the other.
     *
     * @param other
     */
    default void addConnectionWith(LivingEntity other) {
        Objects.requireNonNull(other);

        connectWith(other);
        fromEntity(other).connectWith(castTo(this, LivingEntity.class));
    }

    /**
     * Comfort method for disconnecting two entities from each other.
     * Removes a rope connection between this entity and the given entity, if there is any.
     * The rope connection will be removed from this entity and from the other.
     *
     * @param other The other entity.
     */
    default void removeConnectionWith(LivingEntity other) {
        Objects.requireNonNull(other);

        disconnectFrom(other);
        fromEntity(other).disconnectFrom(castTo(this, LivingEntity.class));
    }

    @Nonnull
    static IRopeNode fromEntity(LivingEntity entity) {
        return (IRopeNode) entity;
    }
}
