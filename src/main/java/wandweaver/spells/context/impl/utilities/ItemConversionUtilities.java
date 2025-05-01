package wandweaver.spells.context.impl.utilities;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import wandweaver.spells.context.data.IItemConversionData;
import wandweaver.spells.context.utilities.IItemConversionUtilities;

import java.util.ArrayList;
import java.util.List;

public class ItemConversionUtilities implements IItemConversionUtilities {
    private final ServerPlayerEntity player;

    public ItemConversionUtilities(ServerPlayerEntity player) {
        this.player = player;
    }
    public boolean canConvert(IItemConversionData data) {
        ItemStack offhand = this.player.getOffHandStack();

        return !offhand.isEmpty()
                && offhand.getCount() >= data.cost()
                && offhand.getItem() == data.from();
    }

    public void convert(IItemConversionData data) {
        ItemStack offhand = this.player.getOffHandStack();

        if (!canConvert(data)) {
            return;
        }

        int offhandAmount = offhand.getCount();

        if (!data.greedyConversion()) {
            List<ItemStack> itemStacks = getItemStacks(data.to(), data.resultAmount());

            for (int i = 0; i < itemStacks.size(); i++) {
                if (i == 0 && offhandAmount == data.cost()) {
                    this.player.setStackInHand(Hand.OFF_HAND, itemStacks.get(i));
                }
                else {
                    ItemStack stack = itemStacks.get(i);

                    this.player.giveOrDropStack(stack);
                }
            }

        }
        else {
            int conversionAmount = (int) Math.floor((double) offhandAmount / data.cost());

            int resultAmount = conversionAmount * data.resultAmount();
            int costAmount = conversionAmount * data.cost();

            offhand.decrement(costAmount);
            List<ItemStack> results = getItemStacks(data.to(), resultAmount);

            for (int i = 0; i < results.size(); i++) {
                if (i == 0 && offhand.isEmpty()) {
                    this.player.setStackInHand(Hand.OFF_HAND, results.get(i));
                }
                else {
                    ItemStack stack = results.get(i);

                    this.player.giveOrDropStack(stack);
                }
            }
        }
    }

    @Override
    public boolean attemptConversion(List<IItemConversionData> data) {
        for (IItemConversionData itemConversionData : data) {
            if (canConvert(itemConversionData)) {
                convert(itemConversionData);
                return true;
            }
        }

        return false;
    }

    private static List<ItemStack> getItemStacks(Item to, int resultAmount) {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();

        if (resultAmount <= 64) {
            itemStacks.add(new ItemStack(to, resultAmount));
        }
        else {
            int amount = resultAmount;
            while (amount > 0) {
                int stackAmount = Math.min(amount, 64);
                itemStacks.add(new ItemStack(to, stackAmount));
                amount -= stackAmount;
            }
        }

        return itemStacks;
    }
}
