package work.lclpnet.mcfun.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import work.lclpnet.mcfun.MCFun;
import work.lclpnet.mcfun.event.LeftClickAirCallback;
import work.lclpnet.mcfun.networking.IServerPacketHandler;
import work.lclpnet.mcfun.networking.MCPacket;

public class PacketLeftClickAir extends MCPacket implements IServerPacketHandler {

    public static final Identifier ID = new Identifier(MCFun.MOD_ID, "left_click_air");

    public PacketLeftClickAir() {
        super(ID);
    }

    @Override
    public void encodeTo(PacketByteBuf buffer) {}

    @Override
    public void handleServer(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketSender responseSender) {
        LeftClickAirCallback.EVENT.invoker().interact(player, player.world);
    }
}
