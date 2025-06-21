package mods.Hileb.add_potion.api.event;

import mods.Hileb.add_potion.common.AddPotionCommon;
import net.minecraft.core.Holder;
import net.minecraft.world.item.alchemy.Potion;

import java.util.List;

/**
 * This Event is fired during ServerAboutToStartEvent to register beneficial and harmful potions added by other mods.
 * @see AddPotionCommon.EventHandler.PotionTrades
 */
public class AddVillagerTradePotionEvent extends net.neoforged.bus.api.Event {
	private final List<Holder<Potion>> beneficial;
	private final List<Holder<Potion>> harmful;

	public AddVillagerTradePotionEvent(List<Holder<Potion>> beneficial, List<Holder<Potion>> harmful) {
		this.beneficial = beneficial;
		this.harmful = harmful;
	}

	public List<Holder<Potion>> getBeneficial() {
		return this.beneficial;
	}

	public List<Holder<Potion>> getHarmful() {
		return this.harmful;
	}
}
