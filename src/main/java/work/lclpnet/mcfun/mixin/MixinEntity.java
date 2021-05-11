package work.lclpnet.mcfun.mixin;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import work.lclpnet.mcfun.rope.IRopeConnectable;
import work.lclpnet.mcfun.rope.Rope;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Mixin(Entity.class)
public class MixinEntity implements IRopeConnectable {

    private Set<Rope> ropes = null;

    @Nullable
    @Override
    public Set<Rope> getRopeConnections() {
        return ropes;
    }

    @Override
    public void addRopeConnection(Rope rope) {
        Objects.requireNonNull(rope);
        if (this.ropes == null) this.ropes = new HashSet<>();
        this.ropes.add(rope);
    }

    @Override
    public void removeRopeConnection(Rope rope) {
        Objects.requireNonNull(rope);
        if (ropes == null) return;
        this.ropes.remove(rope);
    }
}
