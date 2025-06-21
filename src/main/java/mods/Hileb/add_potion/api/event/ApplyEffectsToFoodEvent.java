package mods.Hileb.add_potion.api.event;

import mods.Hileb.add_potion.api.PotionList;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This Event is fired when applying effects to food.
 * @see mods.Hileb.add_potion.api.AddPotionHelper#applyEffectsToFood
 */
public class ApplyEffectsToFoodEvent extends net.neoforged.bus.api.Event {
	protected final ItemStack potion;
	protected final ItemStack food;

	protected boolean success;

	@Nullable
	protected ItemStack potionRemaining;

	protected final PotionList effects;

	public ApplyEffectsToFoodEvent(ItemStack potion, ItemStack food, PotionList effects, boolean success) {
		this.potion = potion;
		this.food = food;
		this.effects = effects;
		this.potionRemaining = null;
		this.success = success;
	}

	public ItemStack getPotion() {
		return this.potion;
	}

	public ItemStack getFood() {
		return this.food;
	}

	public boolean contains(MobEffect effect) {
		return this.effects.stream().anyMatch(instance -> instance.getEffect().value().equals(effect));
	}

	public boolean contains(MobEffect effect, int amplifier) {
		return this.effects.stream().anyMatch(instance -> instance.getEffect().value().equals(effect) && instance.getAmplifier() == amplifier);
	}

	public List<MobEffectInstance> getEffects() {
		return this.effects;
	}

	@Nullable
	public ItemStack getPotionRemaining() {
		return this.potionRemaining;
	}

	public void setPotionRemaining(@Nullable ItemStack potionRemaining) {
		this.potionRemaining = potionRemaining;
	}

	public boolean getSuccess() {
		return this.success;
	}

	public void setSuccess() {
		this.success = true;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
