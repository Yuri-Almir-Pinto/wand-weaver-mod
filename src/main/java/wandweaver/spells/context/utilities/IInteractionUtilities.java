package wandweaver.spells.context.utilities;

import net.minecraft.item.ItemStack;

public interface IInteractionUtilities {
    boolean interactAsIfHolding(ItemStack itemStack);
    boolean interactEntityAsIfHolding(ItemStack itemStack);
    boolean interactBlockAsIfHolding(ItemStack itemStack);
    boolean interactItemAsIfHolding(ItemStack itemStack);

}
