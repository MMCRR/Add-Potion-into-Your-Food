package mods.Hileb.add_potion.common.extension;

import mods.Hileb.add_potion.api.AddPotionHelper;
import mods.Hileb.add_potion.api.PotionType;
import mods.Hileb.add_potion.api.event.APItemPotionTypeEvent;
import mods.Hileb.add_potion.api.event.ApplyEffectsToFoodEvent;
import mods.Hileb.add_potion.api.event.IngredientCheckEvent;
import mods.Hileb.add_potion.api.event.PotionEffectEvent;
import mods.Hileb.add_potion.common.AddPotionCommon;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@EventBusSubscriber
public class APMinecraftExtension {

    static List<TagKey<Item>> FOODS = getFoods();

    @SuppressWarnings("unchecked")
    private static List<TagKey<Item>> getFoods() {
        List<TagKey<Item>> list = new ArrayList<>();
        for (Field field : ItemTags.class.getFields()) {
            if (field.getName().endsWith("_FOOD") && (field.getModifiers() & (Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC)) != 0) {
                try {
                    list.add((TagKey<Item>) field.get(null));
                } catch (IllegalAccessException ignore) {
                }
            }
        }
        return list;
    }

    @SubscribeEvent
    public static void onApplyPotionToFood(ApplyEffectsToFoodEvent event) {
        ItemStack potion = event.getPotion();
        if(potion.is(Items.GOLD_INGOT)) {
            AddPotionHelper.setEffectsHiding(event.getFood());
            event.setSuccess();
        } else if(potion.is(Items.MILK_BUCKET)) {
            AddPotionHelper.clearEffects(event.getFood());
            event.setPotionRemaining(new ItemStack(Items.BUCKET, potion.getCount()));
            event.setSuccess();
        } else if(potion.is(Items.POTION)) {
            event.setPotionRemaining(new ItemStack(Items.GLASS_BOTTLE, potion.getCount()));
        }
    }

    @SubscribeEvent
    public static void canPlaceToPotionSlot(IngredientCheckEvent.Potion evt) {
        evt.appendIngredientChecker(APMinecraftExtension::canPlaceToPotionSlot);
    }

    public static boolean canPlaceToPotionSlot(ItemStack potion) {
        return potion.has(DataComponents.POTION_CONTENTS) || potion.is(Items.GOLD_INGOT) || potion.is(Items.MILK_BUCKET);
    }

    @SubscribeEvent
    public static void canPlaceToFoodSlot(IngredientCheckEvent.Food evt) {
        evt.appendIngredientChecker(APMinecraftExtension::canPlaceToFoodSlot);
    }

    public static boolean canPlaceToFoodSlot(ItemStack food) {
        return food.has(DataComponents.FOOD) || food.has(DataComponents.POTION_CONTENTS) || food.is(Items.MILK_BUCKET) ||
                food.is(Items.CRIMSON_FUNGUS) || getFoods().stream().anyMatch(food::is);
    }

    static Random random = new Random();

    @SubscribeEvent
    public static void getPotionEffects(PotionEffectEvent event) {
        if (event.getPotion().has(DataComponents.POTION_CONTENTS)) {
            PotionContents contents = event.getPotion().get(DataComponents.POTION_CONTENTS);
            for (var effect : contents.getAllEffects()) {
                event.addEffect(effect);
            }
            event.setType(AddPotionHelper.getPotionTypeOfPotionItem(event.getPotion()));
        }
        if (event.getPotion().has(DataComponents.FOOD)) {
            FoodProperties properties = event.getPotion().get(DataComponents.FOOD);
            //noinspection DataFlowIssue
            for(FoodProperties.PossibleEffect possibleEffect : properties.effects()) {
                if (random.nextFloat(1.0f) < possibleEffect.probability()) {
                    event.addEffect(possibleEffect.effectSupplier().get());
                }
            }
            event.setType(PotionType.DEFAULT);
        }
        if (event.getPotion().has(AddPotionCommon.DATA_EFFECTS)) {
            var effects = event.getPotion().get(AddPotionCommon.DATA_EFFECTS);
            event.addEffect(effects);
            event.setType(effects.getType());
        }
    }

    @SubscribeEvent
    public static void getPotionTypeOfPotionItem(APItemPotionTypeEvent evt) {
        switch (evt.getPotionItem().getItem()) {
            case SplashPotionItem splashPotionItem -> evt.setPotionType(PotionType.SPLASH);
            case LingeringPotionItem lingeringPotionItem -> evt.setPotionType(PotionType.LINGERING);
            default -> {
            }
        }
    }
}
