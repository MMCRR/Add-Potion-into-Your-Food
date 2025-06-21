package mods.Hileb.add_potion.api;

import mods.Hileb.add_potion.api.event.*;
import mods.Hileb.add_potion.common.AddPotionCommon;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nullable;
import java.util.*;


public class AddPotionHelper {

	public static void onFoodEaten(LivingEntity effectAcceptor, ServerLevel serverLevel, ItemStack food) {
		onFoodEaten(effectAcceptor, serverLevel, effectAcceptor.blockPosition(), food);
	}

	public static void onFoodEaten(@Nullable LivingEntity effectAcceptor, ServerLevel serverLevel, BlockPos pos, ItemStack food) {
		onFoodEaten(effectAcceptor, serverLevel, new Vec3(pos.getX(), pos.getY(), pos.getZ()), food);
	}

	public static void onFoodEaten(@Nullable LivingEntity effectAcceptor, ServerLevel serverLevel, Vec3 pos, ItemStack food) {
		LivingEntity owner = getOwner(serverLevel, food);
		var evt = NeoForge.EVENT_BUS.post(new APPotionAffectEvent(effectAcceptor, serverLevel, pos, food, owner));
		if (!evt.isCanceled()) {
			PotionList list = getEffectsFromFood(evt.getFood());
			if (!list.isEmpty()) {
				list.getType().eat(effectAcceptor, serverLevel, evt.getPos(), list, evt.getOwner());
			}
		}
	}

	public static boolean canPlaceToPotionSlot(ItemStack potion) {
		return NeoForge.EVENT_BUS.post(new IngredientCheckEvent.Potion(potion)).isIngredient();
	}

	public static boolean canPlaceToFoodSlot(ItemStack food) {
		return NeoForge.EVENT_BUS.post(new IngredientCheckEvent.Food(food)).isIngredient();
	}

	public static PotionList getPotionEffects(ItemStack potion) {
		return NeoForge.EVENT_BUS.post(new PotionEffectEvent(potion, new PotionList())).getEffects();
	}

	public static PotionType getPotionTypeOfPotionItem(ItemStack potionItem) {
		return NeoForge.EVENT_BUS.post(new APItemPotionTypeEvent(potionItem)).getPotionType();
	}

	@Nullable
	public static Tuple<ItemStack, Optional<ItemStack>> applyEffectsToFood(@Nullable LivingEntity owner, ItemStack potion, ItemStack food) {
		PotionList effects = getPotionEffects(potion);

		ItemStack ret = food.copy();
		ret.setCount(1);

		setEffectsShow(ret);
		ApplyEffectsToFoodEvent event = new ApplyEffectsToFoodEvent(potion, ret, effects, canPlaceToFoodSlot(food) && canPlaceToPotionSlot(potion));

		if(!NeoForge.EVENT_BUS.post(event).getSuccess()) {
			return null;
		}

		if (!effects.isEmpty()) {
			if (ret.has(AddPotionCommon.DATA_EFFECTS)) {
				//noinspection DataFlowIssue
				ret.get(AddPotionCommon.DATA_EFFECTS).addAll(effects);
			} else ret.set(AddPotionCommon.DATA_EFFECTS, effects.copy());
			//noinspection DataFlowIssue
			ret.get(AddPotionCommon.DATA_EFFECTS).setType(getPotionTypeOfPotionItem(ret));
		}

		if(owner != null) {
			ret.set(AddPotionCommon.DATA_OWNER, owner.getUUID());
		}
		return new Tuple<>(ret, Optional.ofNullable(event.getPotionRemaining()));
	}

	public static PotionList getEffectsFromFood(ItemStack food) {
		if (food.has(AddPotionCommon.DATA_EFFECTS)) {
			//noinspection DataFlowIssue
			return food.get(AddPotionCommon.DATA_EFFECTS).copy();
		}
		return new PotionList();
	}

	@Nullable
	public static LivingEntity getOwner(ServerLevel level, ItemStack food) {
		if (food.has(AddPotionCommon.DATA_OWNER)) {
            //noinspection DataFlowIssue
            Entity entity = level.getEntity(food.get(AddPotionCommon.DATA_OWNER));
			return entity instanceof LivingEntity livingEntity ? livingEntity : null;
		}
		return null;
	}

	public static boolean isEffectsHiding(ItemStack food) {
		return food.get(AddPotionCommon.DATA_SHOW) == Boolean.FALSE;
	}

	public static void setEffectsHiding(ItemStack food) {
		food.set(AddPotionCommon.DATA_SHOW, false);
	}

	public static void setEffectsShow(ItemStack food) {
		food.set(AddPotionCommon.DATA_SHOW, true);
	}

	public static void clearEffects(ItemStack food) {
		food.remove(AddPotionCommon.DATA_EFFECTS);
		food.remove(AddPotionCommon.DATA_OWNER);
		food.remove(AddPotionCommon.DATA_SHOW);
	}
}
