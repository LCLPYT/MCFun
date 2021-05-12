package work.lclpnet.mcfun.rope;

import net.minecraft.entity.LivingEntity;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Rope {

    private final LivingEntity connectedTo;

    public Rope(LivingEntity connectedTo) {
        this.connectedTo = Objects.requireNonNull(connectedTo);
    }

    @Nonnull
    public LivingEntity getConnectedTo() {
        return connectedTo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Rope rope = (Rope) o;
        return connectedTo.equals(rope.connectedTo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectedTo);
    }
}
