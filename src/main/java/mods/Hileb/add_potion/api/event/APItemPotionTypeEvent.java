package mods.Hileb.add_potion.api.event;

import mods.Hileb.add_potion.api.AddPotionHelper;
import mods.Hileb.add_potion.api.PotionType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;

/**
 * This Event is fired when get potion type of potion item and apply it to an effect.
 * @see AddPotionHelper#getPotionTypeOfPotionItem
 */
public class APItemPotionTypeEvent extends net.neoforged.bus.api.Event {
	private final ItemStack potionItem;

	private PotionType result = PotionType.DEFAULT;

	public APItemPotionTypeEvent(ItemStack potionItem) {
		this.potionItem = potionItem;
	}

	public ItemStack getPotionItem() {
		return this.potionItem;
	}

	public void setPotionType(PotionType result) {
		this.result = result;
	}

	public PotionType getPotionType() {
		return this.result;
	}
}
