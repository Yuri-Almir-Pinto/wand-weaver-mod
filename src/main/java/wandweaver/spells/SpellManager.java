package wandweaver.spells;

import org.jetbrains.annotations.Nullable;
import wandweaver.utils.Direction;

import java.util.HashMap;
import java.util.List;

public class SpellManager {
    private static final HashMap<List<Direction>, ISpell> spellsByPattern = new HashMap<>();
    private static final HashMap<String, ISpell> spellsById = new HashMap<>();

    public static void register(ISpell spell) {
        List<Direction> pattern = spell.getBasePattern();

        if (spellsByPattern.containsKey(pattern)) {
            throw new IllegalArgumentException("Spell with pattern " + pattern + " already registered.");
        }

        spellsByPattern.put(pattern, spell);

        if (spellsById.containsKey(spell.getIdentifier())) {
            throw new IllegalArgumentException("Spell with ID " + spell.getIdentifier() + " already registered.");
        }

        spellsById.put(spell.getIdentifier(), spell);
    }

    public static @Nullable ISpell getSpellByPattern(List<Direction> pattern) {
        return spellsByPattern.get(pattern);
    }

    public static @Nullable ISpell getSpellById(String id) {
        return spellsById.get(id);
    }
}
