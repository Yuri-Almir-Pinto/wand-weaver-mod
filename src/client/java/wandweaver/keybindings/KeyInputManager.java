package wandweaver.keybindings;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.lwjgl.glfw.GLFW;
import wandweaver.WandWeaver;

public class KeyInputManager {
    public static final String KEY_CATEGORY_WAND_WEAVER = "key.category.wand-weaver";
    public static final String KEY_GRAB_WAND = "key.wand-weaver.grab_wand";

    public static KeyBinding grabWandKey;

    private static void registerKeyInputs() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!grabWandKey.wasPressed()) {
                return;
            }

            PlayerEntity player = client.player;

            if (player == null) {
                return;
            }

            PlayerInventory inventory = player.getInventory();

            inventory.setSelectedSlot(WandWeaver.WAND_SLOT);
        });
    }

    public static void register() {
        grabWandKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_GRAB_WAND,
                GLFW.GLFW_KEY_G,
                KEY_CATEGORY_WAND_WEAVER
        ));

        registerKeyInputs();
    }
}
