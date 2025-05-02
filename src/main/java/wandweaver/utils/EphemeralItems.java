package wandweaver.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;

public class EphemeralItems {
    // Ephemeral items are items that only exist to allow for the casting of some spell, such as the ignite spells, which
    // aims to emulate a flint and steel. They should not exist outside that narrow situation, and should disappear
    // whenever encountered in any other context.
    public static final String EPHEMERAL = "wandWeaver$ephemeral";

    public static boolean isEphemeral(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (component == null) {
            return false;
        }

        return component.copyNbt().contains(EPHEMERAL);
    }

    public static ItemStack turnEphemeral(ItemStack itemStack) {
        NbtComponent component = itemStack.get(DataComponentTypes.CUSTOM_DATA);

        NbtCompound originalNbt;

        if (component == null) {
            originalNbt = new NbtCompound();
        } else {
            originalNbt = component.copyNbt();
        }

        originalNbt.putBoolean(EPHEMERAL, true);

        itemStack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(originalNbt));

        return itemStack;
    }
}
