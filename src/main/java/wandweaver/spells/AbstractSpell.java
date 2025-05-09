package wandweaver.spells;

import net.minecraft.text.MutableText;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.utils.Direction;

import java.util.List;

public abstract class AbstractSpell implements ISpell {
    @Override
    public abstract List<Direction> getBasePattern();

    @Override
    public abstract MutableText getName(@Nullable List<Direction> pattern);

    @Override
    public abstract MutableText getDescription(@Nullable List<Direction> pattern);

    @Override
    public abstract String getIdentifier();

    @Override
    public abstract void playerCast(ISpellCastingContext context, List<Direction> pattern);
}
