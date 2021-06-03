package work.lclpnet.mcfun.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import work.lclpnet.mcfun.MCFun;

public class MCItems {

    public static final Item ROPE_ITEM = new RopeItem(new Item.Settings().group(ItemGroup.MISC).maxCount(1));

    public static void register() {
        Registry.register(Registry.ITEM, new Identifier(MCFun.MOD_ID, "rope"), ROPE_ITEM);
    }

}
