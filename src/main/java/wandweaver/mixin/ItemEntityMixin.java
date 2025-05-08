package wandweaver.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wandweaver.utils.Sounds;
import wandweaver.utils.SpecialItem;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void wandWeaver$disallowSpecialItem(CallbackInfo ci) {
        if (SpecialItem.isSpecial(wandWeaver$this().getStack())) {
            Sounds.playSoundOnEntity(wandWeaver$this(), SoundEvents.ENTITY_PLAYER_TELEPORT);
            this.discard();
        }
    }

    @Unique
    private ItemEntity wandWeaver$this() {
        return (ItemEntity) (Object) this;
    }

}
