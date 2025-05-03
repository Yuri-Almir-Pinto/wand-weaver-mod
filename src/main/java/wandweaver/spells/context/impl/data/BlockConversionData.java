package wandweaver.spells.context.impl.data;

import net.minecraft.block.Block;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.data.IBlockConversionData;

public record BlockConversionData(
        Block from,
        Block to,
        @Nullable SoundEvent soundOnConversion
        ) implements IBlockConversionData {
    public static class Builder {
        private final Block from;
        private final Block to;
        @Nullable SoundEvent soundOnConversion;


        public Builder(Block from, Block to) {
            this.from = from;
            this.to = to;
            this.soundOnConversion = null;
        }

        public static Builder begin(Block from, Block to) {
            return new Builder(from, to);
        }

        public Builder sound(SoundEvent sound) {
            this.soundOnConversion = sound;
            return this;
        }

        public BlockConversionData build() {
            return new BlockConversionData(this.from, this.to, this.soundOnConversion);
        }

    }
}
