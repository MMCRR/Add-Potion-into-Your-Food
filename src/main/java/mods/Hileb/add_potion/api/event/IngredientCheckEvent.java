package mods.Hileb.add_potion.api.event;

import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;

/**
 * This Event is fired to indicating if an item is food or potion. Fired in Forge bus (MinecraftForge.EVENT_BUS), and is NOT cancelable.
 */
public abstract class IngredientCheckEvent extends net.neoforged.bus.api.Event {
	protected final ItemStack stack;

	protected boolean ingredient;

	protected IngredientCheckEvent(ItemStack stack) {
		this.stack = stack;
		this.ingredient = ingredient;
	}

	public ItemStack getStack() {
		return this.stack;
	}

	public boolean isIngredient() {
		return this.ingredient;
	}

	/**
	 * @param ingredient	Set this item to ingredient (food / potion) or not.
	 */
	public void setIngredient(boolean ingredient) {
		this.ingredient = ingredient;
	}

	public void appendIngredientChecker(Predicate<ItemStack> stackPredicate) {
		this.ingredient = ingredient || stackPredicate.test(this.stack);
	}

	public static class Food extends IngredientCheckEvent {
		public Food(ItemStack stack) {
			super(stack);
		}
	}

	/**
	 * Remember to subscribe PotionEffectEvent.
	 * @see PotionEffectEvent
	 */
	public static class Potion extends IngredientCheckEvent {
		public Potion(ItemStack stack) {
			super(stack);
		}
	}
}
