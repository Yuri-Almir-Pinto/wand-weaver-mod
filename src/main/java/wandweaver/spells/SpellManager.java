package wandweaver.spells;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.impl.SpellCastingContext;
import wandweaver.spells.context.impl.utilities.*;
import wandweaver.spells.impl.*;
import wandweaver.utils.Direction;

import java.util.HashMap;
import java.util.List;

public class SpellManager {
    private static final HashMap<List<Direction>, ISpell> spellsByPattern = new HashMap<>();
    private static final HashMap<String, ISpell> spellsById = new HashMap<>();

    private static void register(ISpell spell) {
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

    public static void register() {
        SpellManager.register(new IgniteSpell());
        SpellManager.register(new StupifySpell());
        SpellManager.register(new FeatherFallSpell());
        SpellManager.register(new MendSpell());
        SpellManager.register(new AscendSpell());
        SpellManager.register(new SpringSpell());
        SpellManager.register(new GrowthSpell());
        SpellManager.register(new CraftSpell());
        SpellManager.register(new StashSpell());
    }

    public static @Nullable ISpell getSpellByPattern(List<Direction> pattern) {
        return spellsByPattern.get(pattern);
    }

    public static @Nullable ISpell getSpellById(String id) {
        return spellsById.get(id);
    }

    public static ISpellCastingContext getSpellCastingContext(ServerPlayerEntity player) {
        SoundUtilities sound = new SoundUtilities(player);
        BlockConversionUtilities blockConversion = new BlockConversionUtilities(player, sound);
        ItemConversionUtilities itemConversion = new ItemConversionUtilities(player, sound);
        TargetingUtilities targeting = new TargetingUtilities(player);
        InteractionUtilities interaction = new InteractionUtilities(player, targeting);
        EntityUtilities entity = new EntityUtilities(player);

        return new SpellCastingContext(
                player,
                sound,
                interaction,
                targeting,
                blockConversion,
                itemConversion,
                entity
        );
    }
}
