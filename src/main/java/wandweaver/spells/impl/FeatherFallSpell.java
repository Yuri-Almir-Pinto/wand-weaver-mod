package wandweaver.spells.impl;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.AbstractSpell;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.utils.Direction;

import java.util.List;

public class FeatherFallSpell extends AbstractSpell {
    @Override
    public List<Direction> getBasePattern() {
        return List.of(
                Direction.DOWN,
                Direction.LEFT,
                Direction.DOWN,
                Direction.LEFT,
                Direction.DOWN,
                Direction.LEFT,
                Direction.DOWN,
                Direction.LEFT
        );
    }

    @Override
    public MutableText getName(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.name.featherFall");
    }

    @Override
    public int getColor(@Nullable List<Direction> pattern) {
        return 0xf2e6b3; // Yellowy white
    }

    @Override
    public int getAutoDrawTime() {
        return 1;
    }

    @Override
    public MutableText getDescription(@Nullable List<Direction> pattern) {
        return Text.translatable("spell.description.featherFall");
    }

    @Override
    public String getIdentifier() {
        return "featherFall";
    }

    @Override
    public void playerCast(ISpellCastingContext context, List<Direction> directions) {
        ServerPlayerEntity player = context.player();

        player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.SLOW_FALLING, 20, 1, false, false, true)
        );
    }
}
