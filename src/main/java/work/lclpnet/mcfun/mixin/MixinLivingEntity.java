package work.lclpnet.mcfun.mixin;

import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
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

        /*if(sendPacket) {
            LivingEntity le = (LivingEntity) (Object) this;
            for(ServerPlayerEntity player : PlayerLookup.tracking(le))
                MCNetworking.sendAddRopePacket(player, le, rope.getConnectedTo());
        }*/
    }

    @Override
    public void removeRopeConnection(Rope rope, boolean sendPacket) {
        Objects.requireNonNull(rope);
        if(ropes == null) return;
        this.ropes.remove(rope);

        /*if(sendPacket) {
            LivingEntity le = (LivingEntity) (Object) this;
            for(ServerPlayerEntity player : PlayerLookup.tracking(le))
                MCNetworking.sendRemoveRopePacket(player, le, rope.getConnectedTo());
        }*/
    }
}
