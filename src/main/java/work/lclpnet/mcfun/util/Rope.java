package work.lclpnet.mcfun.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class Rope {

    public static float DEFAULT_ROPE_LENGTH = 10F;
    private float length, lengthSquared;
    @Nullable
    private Consumer<Rope> onUpdate = null;

    public Rope() {
        this(DEFAULT_ROPE_LENGTH);
    }

    public Rope(float length) {
        this.setLength(length);
    }

    public float getLength() {
        return length;
    }

    public float getLengthSquared() {
        return lengthSquared;
    }

    public void setLength(float length) {
        if(Float.isNaN(length)) throw new IllegalArgumentException("NaN length");
        else if(!Float.isFinite(length)) throw new IllegalArgumentException("Non finite length");

        this.length = length;
        this.lengthSquared = length * length;

        this.onUpdate();
    }

    protected void onUpdate() {
        if(onUpdate != null) onUpdate.accept(this);
    }

    public void setOnUpdate(@Nullable Consumer<Rope> onUpdate) {
        this.onUpdate = onUpdate;
    }

    /**
     * Called when this rope is updated through a packet.
     * @param rope The updated rope, which properties should be copied.
     */
    @Environment(EnvType.CLIENT)
    public void acceptUpdate(Rope rope) {
        this.setLength(rope.getLength());
    }

    public void encodeTo(PacketByteBuf buffer) {
        buffer.writeFloat(length);
    }

    @Environment(EnvType.CLIENT)
    public static Rope decodeFrom(PacketByteBuf buffer) {
        float length = buffer.readFloat();
        return new Rope(length);
    }
}
