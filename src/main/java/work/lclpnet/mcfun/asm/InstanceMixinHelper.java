package work.lclpnet.mcfun.asm;

/**
 * This class is a tiny workaround to remove IntelliJ IDEAs warnings for mixin instance checks.
 */
public class InstanceMixinHelper {

    public static <T> T castTo(Object obj, Class<T> clazz) {
        return clazz.cast(obj);
    }

    public static <T> boolean isInstance(Object obj, Class<T> clazz) {
        return clazz.isInstance(obj);
    }

}
