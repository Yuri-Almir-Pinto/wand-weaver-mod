package wandweaver.spells;

import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.impl.SpellCastingContext;
import wandweaver.spells.context.impl.state.SacrificedItemsPersistentStateProvider;
import wandweaver.spells.context.impl.utilities.*;
import wandweaver.spells.impl.*;
import wandweaver.utils.Direction;
import java.util.*;
import java.util.stream.Collectors;

public class SpellManager {
    private static final HashMap<List<Direction>, ISpell> spellsByPattern = new HashMap<>();
    private static final HashMap<String, ISpell> spellsById = new HashMap<>();
    private static final List<ISpell> allSpells = new ArrayList<>();

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

        allSpells.add(spell);
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
        SpellManager.register(new SacrificeSpell());
    }

    public static @Nullable ISpell getSpellByPattern(List<Direction> pattern) {
        return spellsByPattern.get(pattern);
    }

    public static @Nullable ISpell getSpellById(String id) {
        return spellsById.get(id);
    }

    public static Set<Item> getAcceptedSacrificeItems() {
        return allSpells.stream()
                .filter(s -> s instanceof ISacrificeListener)
                .map(s -> ((ISacrificeListener) s).acceptedItems())
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    public static Set<ISacrificeListener> getSacrificeListeners() {
        return allSpells.stream()
                .filter(s -> s instanceof ISacrificeListener)
                .map(s -> (ISacrificeListener) s)
                .collect(Collectors.toSet());
    }

    public static ISpellCastingContext getSpellCastingContext(ServerPlayerEntity player) {
        SoundUtilities sound = new SoundUtilities(player);
        BlockConversionUtilities blockConversion = new BlockConversionUtilities(player, sound);
        ItemConversionUtilities itemConversion = new ItemConversionUtilities(player, sound);
        TargetingUtilities targeting = new TargetingUtilities(player);
        InteractionUtilities interaction = new InteractionUtilities(player, targeting);
        EntityUtilities entity = new EntityUtilities(player);
        SacrificedItemsPersistentStateProvider sacrificedItemsProvider =
                new SacrificedItemsPersistentStateProvider(player);

        return new SpellCastingContext(
                player,
                sound,
                interaction,
                targeting,
                blockConversion,
                itemConversion,
                entity,
                sacrificedItemsProvider
        );
    }
}
