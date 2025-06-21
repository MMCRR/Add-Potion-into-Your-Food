package mods.Hileb.add_potion.api.event;

import mods.Hileb.add_potion.api.PotionList;
import mods.Hileb.add_potion.api.PotionType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.Potion;

import java.util.Collection;

/**
 * This Event is fired to indicating the effects of a potion item.
 * @see IngredientCheckEvent.Potion
 */
public class PotionEffectEvent extends net.neoforged.bus.api.Event {
	protected final ItemStack potion;
	protected final PotionList effectMap;

	public PotionEffectEvent(ItemStack potion, PotionList effects) {
		this.potion = potion;
		this.effectMap = effects;
	}

	public ItemStack getPotion() {
		return this.potion;
	}

	public void addEffect(PotionType type) {
		effectMap.type = type;
	}

	public void addEffect(Potion potion) {
		effectMap.addAll(potion.getEffects());
	}

	public void addEffect(Collection<MobEffectInstance> effectInstances){
		this.effectMap.addAll(effectInstances);
	}

	public void addEffect(MobEffectInstance effect) {
		effectMap.add(effect);
	}

	public void removeEffect(MobEffect effect) {
		effectMap.removeIf((o)->o.getEffect().equals(effect));
	}

	public PotionList getEffects() {
		return this.effectMap;
	}

	public void setType(PotionType type) {
		this.effectMap.setType(type);
	}
}
