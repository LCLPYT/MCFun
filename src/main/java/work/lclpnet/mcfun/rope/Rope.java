package work.lclpnet.mcfun.rope;

import net.minecraft.entity.Entity;

import javax.annotation.Nonnull;
import java.util.Objects;

public class Rope {

    private final Entity connectedTo;

    public Rope(Entity connectedTo) {
        this.connectedTo = Objects.requireNonNull(connectedTo);
    }

    @Nonnull
    public Entity getConnectedTo() {
        return connectedTo;
    }

}
