package work.lclpnet.mcfun.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import work.lclpnet.mcfun.MCFun;
import work.lclpnet.mcfun.item.RopeItem;
import work.lclpnet.mcfun.networking.IClientPacketHandler;
import work.lclpnet.mcfun.networking.IPacketDecoder;
import work.lclpnet.mcfun.networking.MCPacket;

public class PacketRopeSelection extends MCPacket implements IClientPacketHandler {

    public static final Identifier ID = new Identifier(MCFun.MOD_ID, "rope_selection");

    private final int entityId;

    public PacketRopeSelection(int entityId) {
        super(ID);
        this.entityId = entityId;
    }

    @Override
    public void handleClient(MinecraftClient client, ClientPlayNetworkHandler handler, PacketSender sender) {
        Entity entity = handler.getWorld().getEntityById(this.entityId);
        if(entity instanceof LivingEntity) RopeItem.setClientSelection((LivingEntity) entity);
        else if(this.entityId == 0) RopeItem.setClientSelection(null);
    }

    @Override
    public void encodeTo(PacketByteBuf buffer) {
        buffer.writeInt(this.entityId);
    }

    public static class Decoder implements IPacketDecoder<PacketRopeSelection> {

        @Override
        public PacketRopeSelection decode(PacketByteBuf buffer) {
            int entityId = buffer.readInt();
            return new PacketRopeSelection(entityId);
        }
    }
}
