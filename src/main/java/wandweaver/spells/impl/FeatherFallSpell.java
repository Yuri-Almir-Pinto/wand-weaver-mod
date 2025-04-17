package wandweaver.spells.impl;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.AbstractSpell;
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
    public Text getName(@Nullable List<Direction> directions) {
        return Text.translatable("spell.name.featherFall");
    }

    @Override
    public Text getDescription(@Nullable List<Direction> directions) {
        return Text.translatable("spell.description.featherFall");
    }

    @Override
    public String getIdentifier() {
        return "featherFall";
    }

    @Override
    public void playerCast(ServerPlayNetworking.Context context, List<Direction> pattern) {
        ServerPlayerEntity player = context.player();

        player.addStatusEffect(
                new StatusEffectInstance(StatusEffects.SLOW_FALLING, 20, 20, false, false, true)
        );
    }
}
