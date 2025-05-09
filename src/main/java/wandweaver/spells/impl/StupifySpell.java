package wandweaver.spells.impl;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.utils.Direction;

import java.util.List;
import java.util.Optional;

public class StupifySpell extends AbstractSpell {
    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.RIGHT,
                Direction.RIGHT,
                Direction.RIGHT,
                Direction.UP,
                Direction.UP,
                Direction.UP,
                Direction.LEFT,
                Direction.LEFT,
                Direction.LEFT,
                Direction.DOWN,
                Direction.DOWN,
                Direction.DOWN,
                Direction.DOWN,
                Direction.DOWN
        );
    }

    @Override
    public MutableText getName(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.name.stupefy");
    }

    @Override
    public MutableText getDescription(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.description.stupefy");
    }

    @Override
    public String getIdentifier() {
        return "stupify";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> pattern) {
        PlayerEntity player = context.player();
        ItemStack offhandStack = player.getOffHandStack();

        if (offhandStack.isEmpty() || offhandStack.getItem() != Items.POTION) {
            return;
        }

        PotionContentsComponent potionContents = offhandStack.get(DataComponentTypes.POTION_CONTENTS);

        if (potionContents == null) {
            return;
        }

        Optional<RegistryEntry<Potion>> offhandPotion = potionContents.potion();

        if (offhandPotion.isPresent()) {
            if (offhandPotion.get() != Potions.WATER) {
                return;
            }

            PotionContentsComponent newPotionContents = new PotionContentsComponent(Potions.AWKWARD);
            offhandStack.set(DataComponentTypes.POTION_CONTENTS, newPotionContents);

            context.sound().playSoundOnPlayer(SoundEvents.ITEM_BOTTLE_FILL);
        }
    }
}
