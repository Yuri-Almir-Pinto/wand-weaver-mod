package wandweaver.spells;

import net.minecraft.item.Item;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;
import wandweaver.spells.context.ISpellCastingContext;
import wandweaver.spells.context.ISpellQueryContext;
import wandweaver.spells.context.impl.SpellCastingContext;
import wandweaver.spells.context.impl.SpellQueryContext;
import wandweaver.spells.context.impl.state.SacrificedItemsPersistentStateProvider;
import wandweaver.spells.context.impl.utilities.*;
import wandweaver.spells.impl.*;
import wandweaver.utils.Direction;
import java.util.*;
import java.util.stream.Collectors;

public class SpellManager {
    private static final DirectionTrie spellsByPattern = new DirectionTrie();
    private static final HashMap<String, ISpell> spellsById = new HashMap<>();
    private static final List<ISpell> allSpells = new ArrayList<>();

    private static void register(ISpell spell) {
        List<Direction> pattern = spell.getBasePattern();

        boolean noConflict = spellsByPattern.insert(pattern, spell);

        if (!noConflict) {
            throw new IllegalArgumentException("Spell with pattern " + pattern + " already registered.");
        }

        if (spellsById.containsKey(spell.getIdentifier())) {
            throw new IllegalArgumentException("Spell with ID " + spell.getIdentifier() + " already registered.");
        }

        spellsById.put(spell.getIdentifier(), spell);

        if (spell.getAutoDrawTime() == 0) {
            throw new IllegalArgumentException("Spell with ID " + spell.getIdentifier() + " cannot have auto draw time of 0.");
        }

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
        return spellsByPattern.getSpellByPattern(pattern);
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

    public static ISpellQueryContext getSpellQueryContext(ServerPlayerEntity player) {
        return new SpellQueryContext(
                new SacrificedItemsPersistentStateProvider(player)
        );
    }

    public static ISpellQueryContext toSpellQueryContext(ISpellCastingContext context) {
        return new SpellQueryContext(
                context.sacrificedItems()
        );
    }

    public static class DirectionTrie {
        private static class DirectionTrieNode {
            private static final int DIR_COUNT = Direction.values().length;
            DirectionTrieNode[] children = new DirectionTrieNode[DIR_COUNT];
            ISpell spell = null;

            DirectionTrieNode get(Direction dir) {
                return children[dir.ordinal()];
            }

            DirectionTrieNode computeIfAbsent(Direction dir) {
                int index = dir.ordinal();
                if (children[index] == null) {
                    children[index] = new DirectionTrieNode();
                }
                return children[index];
            }
        }

        private final DirectionTrieNode root = new DirectionTrieNode();

        public boolean insert(List<Direction> pattern, ISpell spell) {
            DirectionTrieNode node = root;

            for (Direction dir : pattern) {
                if (node.spell != null) {
                    return false;
                }

                node = node.computeIfAbsent(dir);
            }

            if (node.spell != null) {
                return false;
            }

            node.spell = spell;

            return true;
        }

        public @Nullable ISpell getSpellByPattern(List<Direction> inputPattern) {
            DirectionTrieNode node = root;

            for (Direction dir : inputPattern) {
                node = node.get(dir);

                if (node == null) {
                    break;
                }

                if (node.spell != null) {
                    return node.spell;
                }
            }

            return null;
        }
    }


}
