package wandweaver.spells;

import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.ISpellQueryContext;
import wandweaver.utils.Direction;

import java.util.List;

public interface ISpell {
    List<Direction> getBasePattern();

    Text getName(@Nullable List<Direction> directions);

    Text getDescription(@Nullable List<Direction> directions);

    String getIdentifier();

    default boolean isUnlocked(ISpellQueryContext context) {
        return true;
    }

    void playerCast(ISpellCastingContext context, List<Direction> pattern);
}
