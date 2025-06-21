package mods.Hileb.add_potion.api.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

/**
 * This Event is fired when player uses a potion table to add potion to food.
 * @see mods.Hileb.add_potion.common.menu.PotionTableMenu#clickMenuButton
 */
public class APCraftEvent extends PlayerEvent implements ICancellableEvent {
	protected final ItemStack potion;
	protected final ItemStack food;
	protected ItemStack result;

	public APCraftEvent(Player player, ItemStack potion, ItemStack food, ItemStack result) {
		super(player);
		this.potion = potion;
		this.food = food;
		this.result = result;
	}

	public ItemStack getPotion() {
		return this.potion;
	}

	public ItemStack getFood() {
		return this.food;
	}

	public ItemStack getOutput() {
		return this.result;
	}

	public void setOutput(ItemStack result) {
		this.result = result;
	}
}
