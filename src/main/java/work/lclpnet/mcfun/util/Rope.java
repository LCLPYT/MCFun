package work.lclpnet.mcfun.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class Rope {

    public static float DEFAULT_ROPE_LENGTH = 10F, MIN_ROPE_LENGTH = 1F;
    private float length, lengthSquared, tensionFactor;
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

    public float getTensionFactor() {
        return tensionFactor;
    }

    public void setLength(float length) {
        if(Float.isNaN(length)) throw new IllegalArgumentException("NaN length");
        else if(!Float.isFinite(length)) throw new IllegalArgumentException("Non finite length");
        else if(length <= MIN_ROPE_LENGTH) length = DEFAULT_ROPE_LENGTH;

        this.length = length;
        this.lengthSquared = length * length;
        this.tensionFactor = 0.04F / (length * 0.2F);

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

    public CompoundTag writeTo(CompoundTag nbt) {
        nbt.putFloat("Length", this.length);
        return nbt;
    }

    public void encodeTo(PacketByteBuf buffer) {
        buffer.writeFloat(length);
    }

    public static Rope readFrom(CompoundTag nbt) {
        float length = nbt.getFloat("Length");
        return new Rope(length);
    }

    @Environment(EnvType.CLIENT)
    public static Rope decodeFrom(PacketByteBuf buffer) {
        float length = buffer.readFloat();
        return new Rope(length);
    }

}
