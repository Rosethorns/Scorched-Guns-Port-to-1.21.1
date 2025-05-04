package top.ribs.scguns.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import top.ribs.scguns.Config;

/**
 * Author: MrCrayfish
 */
    public class KeyBinds
    {
        public static final KeyMapping KEY_RELOAD = new KeyMapping("key.scguns.reload", GLFW.GLFW_KEY_R, "key.categories.scguns");
        public static final KeyMapping KEY_UNLOAD = new KeyMapping("key.scguns.unload", GLFW.GLFW_KEY_U, "key.categories.scguns");
        public static final KeyMapping KEY_ATTACHMENTS = new KeyMapping("key.scguns.attachments", GLFW.GLFW_KEY_Z, "key.categories.scguns");
        public static final KeyMapping KEY_MELEE = new KeyMapping("key.scguns.melee", GLFW.GLFW_KEY_V, "key.categories.scguns");
        public static final KeyMapping KEY_INSPECT = new KeyMapping("key.scguns.inspect", GLFW.GLFW_KEY_X, "key.categories.scguns");
        public static void registerKeyMappings(RegisterKeyMappingsEvent event)
        {
            event.register(KEY_RELOAD);
            event.register(KEY_UNLOAD);
            event.register(KEY_ATTACHMENTS);
            event.register(KEY_MELEE);
            event.register(KEY_INSPECT);
        }

        public static KeyMapping getAimMapping()
        {
            Minecraft mc = Minecraft.getInstance();
            return Config.CLIENT.controls.flipControls.get() ? mc.options.keyAttack : mc.options.keyUse;
        }

        public static KeyMapping getShootMapping()
        {
            Minecraft mc = Minecraft.getInstance();
            return Config.CLIENT.controls.flipControls.get() ? mc.options.keyUse : mc.options.keyAttack;
        }
    }
