package work.lclpnet.mcfun.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public abstract class MCPacket {

    private final Identifier identifier;

    public MCPacket(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public abstract void encodeTo(PacketByteBuf buffer);

}
