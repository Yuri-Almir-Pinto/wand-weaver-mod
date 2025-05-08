package wandweaver.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import wandweaver.items.ItemsManager;

public class SpecialItem {
    public static boolean isSpecial(Item item) {
        return isSpecial(item.getDefaultStack());
    }

    public static boolean isSpecial(ItemStack stack) {
        return stack.isOf(ItemsManager.WAND);
    }

    public static ItemStack getDefault() {
        return ItemsManager.WAND.getDefaultStack();
    }
}
