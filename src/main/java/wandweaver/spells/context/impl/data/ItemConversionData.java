package wandweaver.spells.context.impl.data;

import net.minecraft.item.Item;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.data.IItemConversionData;

public record ItemConversionData(
        Item from,
        Item to,
        int cost,
        int resultAmount,
        boolean greedyConversion,
        @Nullable SoundEvent soundOnConversion
) implements IItemConversionData {
    public static class Builder {
        private final Item from;
        private final Item to;
        private int cost;
        private int resultAmount;
        private boolean greedyConversion;
        private @Nullable SoundEvent soundOnConversion;

        public Builder(Item from, Item to) {
            this.from = from;
            this.to = to;
            this.cost = 1;
            this.resultAmount = 1;
            this.greedyConversion = false;
            this.soundOnConversion = null;
        }

        public static Builder begin(Item from, Item to) {
            return new Builder(from, to);
        }

        public Builder cost(int value) {
            this.cost = value;
            return this;
        }

        public Builder result(int value) {
            this.resultAmount = value;
            return this;
        }

        public Builder greedy(boolean value) {
            this.greedyConversion = value;
            return this;
        }

        public Builder sound(SoundEvent value) {
            this.soundOnConversion = value;
            return this;
        }

        public ItemConversionData build() {
            return new ItemConversionData(
                    this.from,
                    this.to,
                    this.cost,
                    this.resultAmount,
                    this.greedyConversion,
                    this.soundOnConversion
            );
        }
    }
}
