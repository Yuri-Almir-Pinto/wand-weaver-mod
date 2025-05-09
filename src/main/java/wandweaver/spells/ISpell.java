package wandweaver.spells;

import net.minecraft.text.MutableText;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.ISpellQueryContext;
import wandweaver.utils.Direction;

import java.util.List;

public interface ISpell {
    List<Direction> getBasePattern();

    default int getAutoDrawTime() {
        return 3;
    }

    MutableText getName(@Nullable List<Direction> pattern);

    default int getColor(@Nullable List<Direction> pattern) {
        return 0xFFFFFF;
    }

    MutableText getDescription(@Nullable List<Direction> pattern);

    String getIdentifier();

    default boolean isUnlocked(ISpellQueryContext context) {
        return true;
    }

    void playerCast(ISpellCastingContext context, List<Direction> pattern);
}
