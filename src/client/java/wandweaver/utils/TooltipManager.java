package wandweaver.utils;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import wandweaver.items.ItemsManager;

public class TooltipManager {
    public static void addTooltip(Item item, ItemTooltipCallback callback) {
        ItemTooltipCallback.EVENT.register(
                (itemStack, tooltipContext, tooltipType, list) -> {
            if (itemStack.isOf(item)) {
                callback.getTooltip(itemStack, tooltipContext, tooltipType, list);
            }
        });
    }

    public static void registerTooltips() {
        addTooltip(ItemsManager.WAND, (stack, tooltipContext, tooltipType, lines) -> {
            lines.add(Text.translatable("item.wand-weaver.wand.description").styled(s -> s.withColor(Colors.GRAY)));
        });
    }
}
