package wandweaver.utils;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;
import wandweaver.items.ItemsManager;
import wandweaver.spells.ISpell;
import wandweaver.spells.SpellManager;

import java.util.ArrayList;
import java.util.List;

public class InscribedWand {
    public static final String INSCRIPTION = "wandWeaver$inscription";

    public static ItemStack inscribe(ItemStack stack, List<Direction> pattern) {
        if (!stack.isOf(ItemsManager.WAND)) {
            return ItemStack.EMPTY;
        }

        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (component == null) {
            component = NbtComponent.DEFAULT;
        }

        NbtCompound compound = component.copyNbt();

        compound.putIntArray(INSCRIPTION, toArray(pattern));

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        return stack;
    }

    public static ItemStack uninscribe(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (component == null) {
            component = NbtComponent.DEFAULT;
        }

        NbtCompound compound = component.copyNbt();

        compound.remove(INSCRIPTION);

        stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(compound));

        return stack;
    }

    public static boolean isInscribed(ItemStack stack) {
        if (!stack.isOf(ItemsManager.WAND)) {
            return false;
        }

        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (component == null) {
            return false;
        }

        return component.copyNbt().contains(INSCRIPTION);
    }

    public static List<Direction> getInscription(ItemStack stack) {
        NbtComponent component = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (component == null) {
            return List.of();
        }

        return fromArray(component.copyNbt().getIntArray(INSCRIPTION).orElse(new int[0]));
    }

    public static @Nullable ISpell getSpell(ItemStack stack) {
        return SpellManager.getSpellByPattern(getInscription(stack));
    }

    private static int[] toArray(List<Direction> pattern) {
        return pattern.stream().mapToInt(Enum::ordinal).toArray();
    }

    private static List<Direction> fromArray(int[] pattern) {
        List<Direction> directions = new ArrayList<>();

        for (int number : pattern) {
            if (number >= 0 && number < Direction.values().length) {
                Direction value = Direction.values()[number];
                directions.add(value);
            } else {
                throw new IllegalArgumentException("Invalid ordinal for Direction: " + number);
            }
        }

        return directions;
    }


}
