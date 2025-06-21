package mods.Hileb.add_potion.common;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import mods.Hileb.add_potion.AddPotion;
import mods.Hileb.add_potion.api.AddPotionHelper;
import mods.Hileb.add_potion.api.PotionList;
import mods.Hileb.add_potion.api.event.AddVillagerTradePotionEvent;
import mods.Hileb.add_potion.common.block.PotionFactoryBlock;
import mods.Hileb.add_potion.common.block.PotionTableBlock;
import mods.Hileb.add_potion.common.block.entity.PotionFactoryBlockEntity;
import mods.Hileb.add_potion.common.menu.PotionFactoryMenu;
import mods.Hileb.add_potion.common.menu.PotionTableMenu;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;
import net.neoforged.neoforge.registries.*;

import java.util.*;

import static net.minecraft.world.item.alchemy.Potions.*;

public class AddPotionCommon {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(AddPotion.MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(AddPotion.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AddPotion.MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AddPotion.MODID);
    public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(AddPotion.MODID);
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, AddPotion.MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, AddPotion.MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, AddPotion.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, AddPotion.MODID);



    public static final DeferredBlock<PotionTableBlock> POTION_TABLE = BLOCKS.register(
            "potion_table", PotionTableBlock::new);

    public static final DeferredBlock<PotionFactoryBlock> POTION_FACTORY = BLOCKS.register(
            "potion_factory", PotionFactoryBlock::new);

    public static final DeferredItem<BlockItem> POTION_TABLE_ITEM = ITEMS.registerSimpleBlockItem("potion_table", POTION_TABLE);

    public static final DeferredItem<BlockItem> POTION_FACTORY_ITEM = ITEMS.registerSimpleBlockItem("potion_factory", POTION_FACTORY);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB = CREATIVE_MODE_TABS.register("add_potion", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.add_potion"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> PotionContents.createItemStack(Items.POTION, Potions.POISON))
            .displayItems((parameters, output) -> {
                output.accept(POTION_TABLE_ITEM.get());
                output.accept(POTION_FACTORY_ITEM.get());
            }).build());

    public static final DeferredHolder<MenuType<?>, MenuType<PotionTableMenu>> TABLE_MENU = MENUS.register("potion_table", () -> new MenuType<>(PotionTableMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final DeferredHolder<MenuType<?>, MenuType<PotionFactoryMenu>> FACTORY_MENU = MENUS.register("potion_factory", () -> new MenuType<>(PotionFactoryMenu::new, FeatureFlags.DEFAULT_FLAGS));


    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PotionFactoryBlockEntity>> POTION_FACTORY_BLOCK_ENTITY = BLOCK_ENTITIES.register("potion_factory", resourceLocation -> new BlockEntityType<>(PotionFactoryBlockEntity::new, Set.of(POTION_FACTORY.get()), Util.fetchChoiceType(References.BLOCK_ENTITY, resourceLocation.toString())));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PotionList>> DATA_EFFECTS = DATA_COMPONENTS.registerComponentType("effects", builder -> builder.networkSynchronized(PotionList.STREAM_CODEC).persistent(PotionList.CODEC).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<UUID>> DATA_OWNER = DATA_COMPONENTS.registerComponentType("owner", builder -> builder.networkSynchronized(UUIDUtil.STREAM_CODEC).persistent(UUIDUtil.CODEC).cacheEncoding());

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> DATA_SHOW = DATA_COMPONENTS.registerComponentType("show", builder -> builder.networkSynchronized(StreamCodec.of(FriendlyByteBuf::writeBoolean, FriendlyByteBuf::readBoolean)).persistent(Codec.BOOL).cacheEncoding());

    public static final DeferredHolder<PoiType, PoiType> POI_POTION_TABLE = POI_TYPES.register("potion_table", () -> new PoiType(Set.of(POTION_TABLE.get().getStateDefinition().getPossibleStates().toArray(BlockState[]::new)), 1, 1));

    public static final DeferredHolder<SoundEvent, SoundEvent> VILLAGER_WORK_APOTHECARY_SOUND = SOUND_EVENTS.register("entity.villager.work_apothecary", SoundEvent::createVariableRangeEvent);

    public static final DeferredHolder<VillagerProfession, VillagerProfession> PROF_APOTHECARY = VILLAGER_PROFESSIONS.register("apothecary",
            (name) ->
                    new VillagerProfession(name.toString(),
                            poiTypeHolder -> POI_POTION_TABLE.is(Objects.requireNonNull(Objects.requireNonNull(poiTypeHolder.getKey()))),
                            poiTypeHolder -> POI_POTION_TABLE.is(poiTypeHolder.getKey()),
                            ImmutableSet.of(),
                            ImmutableSet.of(),
                            VILLAGER_WORK_APOTHECARY_SOUND.get()));

    public static void initialize(IEventBus modbus, ModContainer container) {
        BLOCKS.register(modbus);
        ITEMS.register(modbus);
        CREATIVE_MODE_TABS.register(modbus);
        BLOCK_ENTITIES.register(modbus);
        DATA_COMPONENTS.register(modbus);
        MENUS.register(modbus);
        VILLAGER_PROFESSIONS.register(modbus);
        POI_TYPES.register(modbus);
        SOUND_EVENTS.register(modbus);


        NeoForge.EVENT_BUS.register(EventHandler.class);
    }

    public static class EventHandler {

        @SubscribeEvent
        public static void onFoodThrow(ProjectileImpactEvent event) {
            if (event.getProjectile() instanceof ItemSupplier itemSupplier && event.getProjectile().level() instanceof ServerLevel serverLevel) {
                ItemStack itemStack = itemSupplier.getItem();
                AddPotionHelper.onFoodEaten(null, serverLevel, event.getRayTraceResult().getLocation(), itemStack);
            }
        }

        @SubscribeEvent
        public static void onFoodEaten(LivingEntityUseItemEvent.Finish event) {
            LivingEntity entity = event.getEntity();
            if (entity.level() instanceof ServerLevel serverLevel) {
                AddPotionHelper.onFoodEaten(entity, serverLevel, event.getItem());
            }
        }

        @SubscribeEvent
        public static void registerTrades(VillagerTradesEvent event) {
            Int2ObjectMap<List<VillagerTrades.ItemListing>> trades = event.getTrades();

            ResourceLocation currentVillagerProfession = BuiltInRegistries.VILLAGER_PROFESSION.getKey(event.getType());

            if(PROF_APOTHECARY.is(currentVillagerProfession)) {
                trades.get(1).add(new PotionTrades.EmeraldForItems(Items.GLASS_BOTTLE, 9, 1, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_1_BUY, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(1).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.BREAD, 2, 1, 1, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_1_SELL, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(2).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.APPLE, 2, 1, 1, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_2_SELL, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(2).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.CARROT, 3, 1, 1, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_2_SELL, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(2).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.POTATO, 3, 1, 1, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_2_SELL, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(3).add(new PotionTrades.EmeraldForItems(Items.NETHER_WART, 22, 1, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_3_BUY, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(3).add(new PotionTrades.EmeraldForItems(Items.GOLD_INGOT, 3, 1, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_3_BUY, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(3).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.COOKIE, 2, 2, 1, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_3_SELL, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(4).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.GOLDEN_APPLE, 1, 3, 8, PotionTrades.UNCOMMON_ITEMS_SUPPLY, PotionTrades.XP_LEVEL_4_SELL, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(4).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.COOKED_PORKCHOP, 3, 2, 2, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_4_SELL, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(4).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.COOKED_BEEF, 3, 2, 2, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_4_SELL, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(5).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.RABBIT_STEW, 1, 3, 2, PotionTrades.UNCOMMON_ITEMS_SUPPLY, PotionTrades.XP_LEVEL_5_TRADE, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(5).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.MUSHROOM_STEW, 1, 3, 1, PotionTrades.UNCOMMON_ITEMS_SUPPLY, PotionTrades.XP_LEVEL_5_TRADE, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(5).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.GOLDEN_CARROT, 1, 3, 2, PotionTrades.UNCOMMON_ITEMS_SUPPLY, PotionTrades.XP_LEVEL_5_TRADE, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
                trades.get(5).add(new PotionTrades.FoodWithRandomEffectsForEmerald(Items.PUMPKIN_PIE, 1, 3, 2, PotionTrades.DEFAULT_SUPPLY, PotionTrades.XP_LEVEL_5_TRADE, PotionTrades.LOW_TIER_PRICE_MULTIPLIER));
            }

            NeoForge.EVENT_BUS.post(new AddVillagerTradePotionEvent(PotionTrades.ALL_BENEFICIAL_POTIONS, PotionTrades.ALL_HARMFUL_POTIONS));
        }

        public static class PotionTrades {

            public static final int DEFAULT_SUPPLY = 12;
            public static final int COMMON_ITEMS_SUPPLY = 16;
            public static final int UNCOMMON_ITEMS_SUPPLY = 3;
            public static final int XP_LEVEL_1_SELL = 1;
            public static final int XP_LEVEL_1_BUY = 2;
            public static final int XP_LEVEL_2_SELL = 5;
            public static final int XP_LEVEL_2_BUY = 10;
            public static final int XP_LEVEL_3_SELL = 10;
            public static final int XP_LEVEL_3_BUY = 20;
            public static final int XP_LEVEL_4_SELL = 15;
            public static final int XP_LEVEL_4_BUY = 30;
            public static final int XP_LEVEL_5_TRADE = 30;
            public static final float LOW_TIER_PRICE_MULTIPLIER = 0.05F;
            public static final float HIGH_TIER_PRICE_MULTIPLIER = 0.2F;

            private static final List<Holder<Potion>> ALL_BENEFICIAL_POTIONS = Lists.newArrayList(
                    NIGHT_VISION,
                    INVISIBILITY,
                    LEAPING,
                    FIRE_RESISTANCE,
                    SWIFTNESS,
                    WATER_BREATHING,
                    HEALING,
                    REGENERATION,
                    STRENGTH,
                    SLOW_FALLING
            );

            private static final List<Holder<Potion>> ALL_HARMFUL_POTIONS = Lists.newArrayList(
                    SLOWNESS,
                    HARMING,
                    POISON,
                    WEAKNESS
            );

            public record EmeraldForItems(ItemLike item, int itemCount, int emeraldCost, int maxUses, int villagerXp, float priceMultiplier) implements VillagerTrades.ItemListing {
                public MerchantOffer getOffer(Entity trader, RandomSource random) {
                    return new MerchantOffer(new ItemCost(item, itemCount), new ItemStack(Items.EMERALD, this.emeraldCost), this.maxUses, this.villagerXp, this.priceMultiplier);
                }
            }

            public record FoodWithRandomEffectsForEmerald(ItemLike item, int itemCount, int chooses, int emeraldCost, int maxUses, int villagerXp, float priceMultiplier) implements VillagerTrades.ItemListing {
                @Override
                public MerchantOffer getOffer(Entity trader, RandomSource random) {
                    ItemStack result = new ItemStack(this.item, this.itemCount);

                    List<Holder<Potion>> pool = random.nextBoolean() ? ALL_BENEFICIAL_POTIONS : ALL_HARMFUL_POTIONS;
                    assert this.chooses < pool.size() : "Chooses Must be lesser than the size of pool!";
                    boolean[] flags = new boolean[pool.size()];
                    Set<MobEffectInstance> set = HashSet.newHashSet(pool.size());
                    for (int i = 0; i < this.chooses; ++i) {
                        int choice;
                        do {
                            choice = random.nextInt(pool.size());
                        } while(flags[choice]);
                        flags[choice] = true;
                        set.addAll(pool.get(choice).value().getEffects());
                    }
                    if (!set.isEmpty()) {
                        if (!result.has(DATA_EFFECTS)) {
                            result.set(DATA_EFFECTS, new PotionList());
                        }
                        result.get(DATA_EFFECTS).addAll(set);
                    }

                    result.set(DATA_OWNER, trader.getUUID());

                    return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), result, this.maxUses, this.villagerXp, this.priceMultiplier);
                }
            }
        }
    }
}
