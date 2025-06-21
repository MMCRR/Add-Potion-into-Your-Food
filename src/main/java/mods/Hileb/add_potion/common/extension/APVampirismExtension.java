package mods.Hileb.add_potion.common.extension;

import mods.Hileb.add_potion.api.event.IngredientCheckEvent;
import mods.Hileb.add_potion.api.event.PotionEffectEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = "vampirism")
public class APVampirismExtension {
    public static final Holder<Item> OBLIVION_ITEM = BuiltInRegistries.ITEM.getHolder(ResourceLocation.fromNamespaceAndPath("vampirism", "oblivion_potion")).orElse(null);
    public static final Holder<MobEffect> OBLIVION_EFFECT = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.fromNamespaceAndPath("vampirism", "oblivion")).orElse(null);

    @SubscribeEvent
    public static void canPlaceToFoodSlot(IngredientCheckEvent.Food food) {
        if (food.getStack().is(OBLIVION_ITEM)) {
            food.setIngredient(true);
        }
    }

    @SubscribeEvent
    public static void canPlaceToPotionSlot(IngredientCheckEvent.Potion food) {
        if (food.getStack().is(OBLIVION_ITEM)) {
            food.setIngredient(true);
        }
    }

    @SubscribeEvent
    public static void getPotionEffects(PotionEffectEvent event) {
        if (event.getPotion().is(OBLIVION_ITEM)) {
            event.addEffect(new MobEffectInstance(OBLIVION_EFFECT, MobEffectInstance.INFINITE_DURATION,  4));
        }
    }
}
