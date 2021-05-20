package work.lclpnet.mcfun.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import work.lclpnet.mcfun.networking.MCNetworking;
import work.lclpnet.mcfun.networking.packet.PacketUpdateRopeConnection;
import work.lclpnet.mcfun.rope.IRopeConnectable;
import work.lclpnet.mcfun.rope.Rope;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Mixin(LivingEntity.class)
public class MixinLivingEntity implements IRopeConnectable {

    private Set<Rope> ropes = null;

    @Nullable
    @Override
    public Set<Rope> getRopeConnections() {
        return ropes;
    }

    @Override
    public void addRopeConnection(Rope rope, boolean sendPacket) {
        Objects.requireNonNull(rope);
        if (this.ropes == null) this.ropes = new HashSet<>();
        this.ropes.add(rope);

        if(sendPacket) {
            LivingEntity le = (LivingEntity) (Object) this;
            PacketUpdateRopeConnection packet = new PacketUpdateRopeConnection(PacketUpdateRopeConnection.Action.CONNECT, le, rope.getConnectedTo());
            if(le instanceof ServerPlayerEntity)
                MCNetworking.sendPacketTo(packet, (ServerPlayerEntity) le);
            MCNetworking.sendToAllTracking(le, packet);
        }
    }

    @Override
    public void removeRopeConnection(Rope rope, boolean sendPacket) {
        Objects.requireNonNull(rope);
        if(ropes == null) return;
        this.ropes.remove(rope);

        if(sendPacket) {
            LivingEntity le = (LivingEntity) (Object) this;
            PacketUpdateRopeConnection packet = new PacketUpdateRopeConnection(PacketUpdateRopeConnection.Action.DISCONNECT, le, rope.getConnectedTo());
            if(le instanceof ServerPlayerEntity)
                MCNetworking.sendPacketTo(packet, (ServerPlayerEntity) le);
            MCNetworking.sendToAllTracking(le, packet);
        }
    }
}
