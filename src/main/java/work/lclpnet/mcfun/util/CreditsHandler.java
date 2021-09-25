package work.lclpnet.mcfun.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import work.lclpnet.mcfun.asm.type.IRopeNode;

import java.util.*;

public class CreditsHandler {

    private static final Map<String, Set<LivingEntity>> chained = new HashMap<>();
    private static final Set<LivingEntity> dimensionChangeBlocked = new HashSet<>();

    public static void handleCreditsScreenStart(ServerPlayerEntity player) {
        IRopeNode node = IRopeNode.fromEntity(player);
        Set<LivingEntity> connected = node.getRopeConnectedEntities();
        if (connected == null || connected.isEmpty()) return;

        Set<LivingEntity> connectedClone = new HashSet<>(connected);
        chained.put(player.getUuidAsString(), connectedClone);
        dimensionChangeBlocked.addAll(Rope.getAllMembersInChainExceptSelfOf(player));
        connectedClone.forEach(node::disconnectFrom);
    }

    public static boolean isDimensionChangeBlocked(LivingEntity le) {
        return le != null && dimensionChangeBlocked.contains(le);
    }

    public static void handleRespawn(ServerPlayerEntity player) {
        String uuid = player.getUuidAsString();
        Set<LivingEntity> connected = chained.get(uuid);
        if (connected == null) return;

        Set<LivingEntity> chainMembers = new HashSet<>();
        connected.forEach(en -> chainMembers.addAll(Rope.getAllMembersInChainOf(en)));

        final IRopeNode node = IRopeNode.fromEntity(player);

        final EnumSet<PlayerPositionLookS2CPacket.Flag> flags = EnumSet.of(PlayerPositionLookS2CPacket.Flag.X, PlayerPositionLookS2CPacket.Flag.Y, PlayerPositionLookS2CPacket.Flag.Z, PlayerPositionLookS2CPacket.Flag.X_ROT, PlayerPositionLookS2CPacket.Flag.Y_ROT);
        final ServerWorld world = (ServerWorld) player.getEntityWorld();
        chainMembers.forEach(en -> {
            dimensionChangeBlocked.remove(en);
            boolean directlyConnected = connected.contains(en);
            Entity teleported = MCUtils.teleport(en, world, player.getX(), player.getY(), player.getZ(), flags, player.yaw, player.pitch);
            if (!(teleported instanceof LivingEntity)) return;

            LivingEntity living = (LivingEntity) teleported;
            if (directlyConnected) node.connectWith(living);
        });

        chained.remove(uuid);
    }
}
