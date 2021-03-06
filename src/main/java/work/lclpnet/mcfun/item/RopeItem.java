package work.lclpnet.mcfun.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import work.lclpnet.mcfun.asm.type.IRopeNode;
import work.lclpnet.mcfun.networking.MCNetworking;
import work.lclpnet.mcfun.networking.packet.PacketRopeSelection;

import java.util.HashMap;
import java.util.Map;

public class RopeItem extends Item {

    private static final Map<ServerPlayerEntity, LivingEntity> selections = new HashMap<>();
    @Environment(EnvType.CLIENT)
    private static LivingEntity clientSelection = null;

    public RopeItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if(user.world.isClient || !(user instanceof ServerPlayerEntity)) return ActionResult.PASS;

        // on server

        if(hand == Hand.OFF_HAND) {
            ItemStack mainHand = user.inventory.getMainHandStack();
            if(!mainHand.isEmpty() && mainHand.getItem().equals(MCItems.ROPE_ITEM)) return ActionResult.PASS;
        }

        boolean fail = useOn((ServerPlayerEntity) user, entity);
        if (fail) return ActionResult.FAIL;

        return ActionResult.success(true);
    }

    public static boolean useOn(ServerPlayerEntity user, LivingEntity entity) {
        LivingEntity selected = selections.get(user);
        if(selected == null) {
            select(user, entity);
        } else {
            if(entity.equals(selected)) {
                user.sendSystemMessage(new TranslatableText("item.mcfun.rope.equal").formatted(Formatting.RED), Util.NIL_UUID);
                return true;
            }

            if(user.isSneaking()) {
                if(!IRopeNode.fromEntity(selected).isConnectedTo(entity)) {
                    user.sendSystemMessage(new TranslatableText("commands.disconnect.entities.not-linked").formatted(Formatting.RED), Util.NIL_UUID);
                    return true;
                }

                IRopeNode.fromEntity(selected).disconnectFrom(entity);
                user.sendSystemMessage(new TranslatableText("commands.disconnect.entities.disconnected", selected.getName(), entity.getName()).formatted(Formatting.GREEN), Util.NIL_UUID);
            } else {
                if(IRopeNode.fromEntity(selected).isConnectedTo(entity)) {
                    user.sendSystemMessage(new TranslatableText("commands.connect.entities.already-linked").formatted(Formatting.RED), Util.NIL_UUID);
                    return true;
                }

                IRopeNode.fromEntity(selected).connectWith(entity);
                user.sendSystemMessage(new TranslatableText("commands.connect.entities.connected", selected.getName(), entity.getName()).formatted(Formatting.GREEN), Util.NIL_UUID);
            }

            selections.remove(user);
            MCNetworking.sendPacketTo(new PacketRopeSelection(0), user);
        }
        return false;
    }

    public static void select(ServerPlayerEntity player, LivingEntity entity) {
        selections.put(player, entity);
        MCNetworking.sendPacketTo(new PacketRopeSelection(entity.getEntityId()), player);
        player.sendSystemMessage(new TranslatableText("item.mcfun.rope.selected", entity.getName()), Util.NIL_UUID);
    }

    @Environment(EnvType.CLIENT)
    public static LivingEntity getClientSelection() {
        return clientSelection;
    }

    @Environment(EnvType.CLIENT)
    public static void setClientSelection(LivingEntity livingEntity) {
        clientSelection = livingEntity;
    }
}
