package wandweaver.spells.context.utilities;

import net.minecraft.item.ItemStack;

public interface IInteractionUtilities {
    boolean interactAsIfHolding(ItemStack itemStack);
    boolean interactAsIfHolding(ItemStack itemStack, boolean includeFluids);
    boolean interactEntityAsIfHolding(ItemStack itemStack);
    boolean interactBlockAsIfHolding(ItemStack itemStack);
    boolean interactBlockAsIfHolding(ItemStack itemStack, boolean includeFluids);
    boolean interactItemAsIfHolding(ItemStack itemStack);
}
