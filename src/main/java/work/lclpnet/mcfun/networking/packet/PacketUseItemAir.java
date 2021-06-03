package work.lclpnet.mcfun.networking.packet;

import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import work.lclpnet.mcfun.MCFun;
import work.lclpnet.mcfun.event.UseItemAirCallback;
import work.lclpnet.mcfun.networking.IPacketDecoder;
import work.lclpnet.mcfun.networking.IServerPacketHandler;
import work.lclpnet.mcfun.networking.MCPacket;

import java.util.Objects;

public class PacketUseItemAir extends MCPacket implements IServerPacketHandler {

    public static final Identifier ID = new Identifier(MCFun.MOD_ID, "use_item_air");

    private final Hand hand;

    public PacketUseItemAir(Hand hand) {
        super(ID);
        this.hand = Objects.requireNonNull(hand);
    }

    @Override
    public void handleServer(PacketContext ctx) {
        PlayerEntity player = ctx.getPlayer();
        UseItemAirCallback.EVENT.invoker().interact(player, player.world, this.hand);
    }

    @Override
    public void encodeTo(PacketByteBuf buffer) {
        buffer.writeEnumConstant(hand);
    }

    public static class Decoder implements IPacketDecoder<PacketUseItemAir> {

        @Override
        public PacketUseItemAir decode(PacketByteBuf buffer) {
            Hand hand = buffer.readEnumConstant(Hand.class);
            return new PacketUseItemAir(hand);
        }
    }
}
