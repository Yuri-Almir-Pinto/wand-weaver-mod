package wandweaver.keybindings;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;
import wandweaver.WandWeaver;
import wandweaver.WandWeaverClient;
import wandweaver.network.payloads.MagicalActionC2SPayload;
import wandweaver.utils.InscribedWand;

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
            if (InscribedWand.isInscribed(player.getStackInHand(Hand.MAIN_HAND)) && !WandWeaver.isAutoCasting) {
                ClientPlayNetworking.send(MagicalActionC2SPayload.erase());
            } else if (WandWeaver.isCasting && !WandWeaverClient.DIRECTION_LIST.isEmpty()) {
                ClientPlayNetworking.send(MagicalActionC2SPayload.inscribe(WandWeaverClient.DIRECTION_LIST.stream().toList()));
                WandWeaver.isCasting = false;
                WandWeaverClient.DIRECTION_LIST.clear();
                WandWeaverClient.currentSpell = null;
                client.player.stopUsingItem();
            } else {
                ClientPlayNetworking.send(MagicalActionC2SPayload.wand());
            }

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
