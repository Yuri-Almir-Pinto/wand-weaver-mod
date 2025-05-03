package wandweaver.spells.context.impl.utilities;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.data.IItemConversionData;
import wandweaver.spells.context.utilities.IItemConversionUtilities;
import wandweaver.spells.context.utilities.ISoundUtilities;

import java.util.ArrayList;
import java.util.List;

public class ItemConversionUtilities implements IItemConversionUtilities {
    private final ServerPlayerEntity player;
    private final ISoundUtilities sound;

    public ItemConversionUtilities(ServerPlayerEntity player, ISoundUtilities sound) {
        this.player = player;
        this.sound = sound;
    }
    public boolean canConvert(IItemConversionData data) {
        ItemStack offhand = this.player.getOffHandStack();

        return !offhand.isEmpty()
                && offhand.getCount() >= data.cost()
                && offhand.getItem() == data.from();
    }

    public void convert(IItemConversionData data) {
        this.convert(data, null);
    }

    @Override
    public void convert(IItemConversionData data, @Nullable SoundEvent defaultSoundOnConversion) {
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
                    ItemStack offhandItemStack = this.player.getStackInHand(Hand.OFF_HAND);
                    offhandItemStack.decrement(1);
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

        if (data.soundOnConversion() != null) {
            this.sound.playSoundOnPlayer(data.soundOnConversion());
        } else if (defaultSoundOnConversion != null) {
            this.sound.playSoundOnPlayer(defaultSoundOnConversion);
        }
    }

    @Override
    public boolean attemptConversion(List<IItemConversionData> data, SoundEvent defaultSoundOnConversion) {
        for (IItemConversionData itemConversionData : data) {
            if (canConvert(itemConversionData)) {
                convert(itemConversionData);
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean attemptConversion(List<IItemConversionData> data) {
        return this.attemptConversion(data, null);
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
