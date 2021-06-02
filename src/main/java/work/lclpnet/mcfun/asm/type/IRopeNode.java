package work.lclpnet.mcfun.asm.type;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import work.lclpnet.mcfun.networking.MCNetworking;
import work.lclpnet.mcfun.networking.packet.PacketUpdateRopeConnection;
import work.lclpnet.mcfun.util.Rope;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static work.lclpnet.mcfun.asm.InstanceMixinHelper.castTo;

public interface IRopeNode {

    @Nullable
    Set<LivingEntity> getRopeConnectedEntities();

    void addServerRopeConnection(LivingEntity entity, Rope rope);

    void removeServerRopeConnection(LivingEntity entity);

    @Environment(EnvType.CLIENT)
    void addClientRopeConnection(int entityId, Rope rope);

    @Environment(EnvType.CLIENT)
    void removeClientRopeConnection(int entityId);

    @Nullable
    Rope getRopeConnection(LivingEntity other);

    @Nullable
    @Environment(EnvType.CLIENT)
    Rope getClientRopeConnection(int entityId);

    default boolean isConnectedTo(LivingEntity entity) {
        Objects.requireNonNull(entity);

        Set<LivingEntity> connections = getRopeConnectedEntities();
        if(connections == null) return false;

        return connections.stream().anyMatch(connected -> connected.equals(entity));
    }

    /**
     * Removes all rope connections from this entity and from all entities connected with it.
     * @return True, if there were any rope connections which were removed, false otherwise.
     */
    default boolean removeAllRopeConnectionPairs() {
        Set<LivingEntity> connected = getRopeConnectedEntities();
        if(connected == null) return false;

        boolean empty = connected.isEmpty();

        LivingEntity thisLiving = castTo(this, LivingEntity.class);
        if(thisLiving.world.isClient) new HashSet<>(connected).forEach(entity -> removeClientRopeConnection(entity.getEntityId()));
        else new HashSet<>(connected).forEach(this::disconnectFrom);

        return !empty;
    }

    /**
     * Comfort method for connecting two entities with each other.
     * Establishes a rope connection between this entity and the given entity.
     * A rope connection will be created for this entity and for the other.
     *
     * @param other The other entity.
     */
    default void connectWith(final LivingEntity other) {
        Objects.requireNonNull(other);
        if(isConnectedTo(other)) return;

        final LivingEntity entity = castTo(this, LivingEntity.class);

        Rope rope = new Rope(); // this rope instance is shared between the connected entities.
        rope.setOnUpdate(updatedRope -> {
            if(entity.world.isClient) return;
            MCNetworking.sendToAllTrackingIncludingSelf(entity, PacketUpdateRopeConnection.createUpdatePropertiesPacket(entity, other, updatedRope));
        });

        addServerRopeConnection(other, rope);
        fromEntity(other).addServerRopeConnection(entity, rope);
    }

    /**
     * Comfort method for disconnecting two entities from each other.
     * Removes a rope connection between this entity and the given entity, if there is any.
     * The rope connection will be removed from this entity and from the other.
     *
     * @param other The other entity.
     */
    default void disconnectFrom(LivingEntity other) {
        Objects.requireNonNull(other);

        removeServerRopeConnection(other);
        fromEntity(other).removeServerRopeConnection(castTo(this, LivingEntity.class));
    }

    @Nonnull
    static IRopeNode fromEntity(LivingEntity entity) {
        return (IRopeNode) entity;
    }
}
