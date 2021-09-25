package work.lclpnet.mcfun.util;

import net.minecraft.entity.LivingEntity;

import java.util.Map;

public class ConnectionStore {

    public final Map<LivingEntity, Rope> connected;

    public ConnectionStore(Map<LivingEntity, Rope> connected) {
        this.connected = connected;
    }
}
