package wandweaver.spells;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.utils.Direction;

import java.util.List;

public abstract class AbstractSpell implements ISpell {
    @Override
    public abstract List<Direction> getBasePattern();

    @Override
    public abstract Text getName(@Nullable List<Direction> directions);

    @Override
    public abstract Text getDescription(@Nullable List<Direction> directions);

    @Override
    public abstract String getIdentifier();

    @Override
    public abstract void playerCast(ISpellCastingContext context, List<Direction> pattern);
}
