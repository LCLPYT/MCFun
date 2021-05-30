package work.lclpnet.mcfun.asm.type;

import net.minecraft.entity.LivingEntity;
import work.lclpnet.mcfun.networking.MCNetworking;
import work.lclpnet.mcfun.networking.packet.PacketUpdateRopeConnection;
import work.lclpnet.mcfun.util.Rope;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;

import static work.lclpnet.mcfun.asm.InstanceMixinHelper.castTo;

public interface IRopeNode {

    @Nullable
    Set<LivingEntity> getRopeConnectedEntities();

    void addRopeConnection(LivingEntity entity, Rope rope);

    void removeRopeConnection(LivingEntity entity);

    @Nullable
    Rope getRopeConnection(LivingEntity other);

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
     * @param other The other entity.
     */
    default void addConnectionWith(final LivingEntity other) {
        Objects.requireNonNull(other);
        if(isConnectedTo(other)) return;

        final LivingEntity entity = castTo(this, LivingEntity.class);

        Rope rope = new Rope(); // this rope instance is shared between the connected entities.
        rope.setOnUpdate(updatedRope -> {
            if(entity.world.isClient) return;
            MCNetworking.sendToAllTrackingIncludingSelf(entity, PacketUpdateRopeConnection.createUpdatePropertiesPacket(entity, other, updatedRope));
        });

        addRopeConnection(other, rope);
        fromEntity(other).addRopeConnection(entity, rope);
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

        removeRopeConnection(other);
        fromEntity(other).removeRopeConnection(castTo(this, LivingEntity.class));
    }

    @Nonnull
    static IRopeNode fromEntity(LivingEntity entity) {
        return (IRopeNode) entity;
    }
}
