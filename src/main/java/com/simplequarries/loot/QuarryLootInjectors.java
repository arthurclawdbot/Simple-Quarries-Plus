package com.simplequarries.loot;

import com.simplequarries.SimpleQuarries;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class QuarryLootInjectors {
    private static final Map<Identifier, Float> TARGET_CHESTS = Map.ofEntries(
            Map.entry(Identifier.ofVanilla("chests/ruined_portal"), 0.03f),
            Map.entry(Identifier.ofVanilla("chests/abandoned_mineshaft"), 0.10f),
            Map.entry(Identifier.ofVanilla("chests/nether_bridge"), 0.10f),
            Map.entry(Identifier.ofVanilla("chests/bastion_other"), 0.30f),
            Map.entry(Identifier.ofVanilla("chests/bastion_treasure"), 0.30f),
            Map.entry(Identifier.ofVanilla("chests/bastion_bridge"), 0.30f),
            Map.entry(Identifier.ofVanilla("chests/bastion_hoglin_stable"), 0.30f),
            Map.entry(Identifier.ofVanilla("chests/stronghold_corridor"), 0.20f),
            Map.entry(Identifier.ofVanilla("chests/stronghold_crossing"), 0.20f),
            Map.entry(Identifier.ofVanilla("chests/stronghold_library"), 0.20f),
            Map.entry(Identifier.ofVanilla("chests/end_city_treasure"), 0.08f),
            Map.entry(Identifier.ofVanilla("chests/simple_dungeon"), 0.03f),
            Map.entry(Identifier.ofVanilla("chests/desert_pyramid"), 0.05f)
    );

    private QuarryLootInjectors() {}

    public static void register() {
        LootTableEvents.MODIFY.register((RegistryKey<LootTable> key, LootTable.Builder tableBuilder, LootTableSource source, net.minecraft.registry.RegistryWrapper.WrapperLookup registries) -> {
            if (!source.isBuiltin()) {
                return;
            }

            Float chance = TARGET_CHESTS.get(key.getValue());
            if (chance == null) {
                return;
            }

            addTemplateEntry(tableBuilder, chance);
        });
    }

    private static void addTemplateEntry(LootTable.Builder tableBuilder, float chance) {
        LootPool.Builder pool = LootPool.builder()
                .rolls(ConstantLootNumberProvider.create(1))
                .conditionally(RandomChanceLootCondition.builder(chance))
                .with(ItemEntry.builder(SimpleQuarries.QUARRY_UPGRADE_TEMPLATE));
        tableBuilder.pool(pool);
    }
}
