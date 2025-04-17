package wandweaver.mixin;

import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractFurnaceBlockEntity.class)
public interface AbstractFurnaceBlockEntityAccessor {
    @Accessor("litTimeRemaining")
    int getLitTimeRemaining();

    @Accessor("litTimeRemaining")
    void setLitTimeRemaining(int value);

    @Accessor("litTotalTime")
    int getLitTotalTime();

    @Accessor("litTotalTime")
    void setLitTotalTime(int value);
}
