package mods.Hileb.add_potion.client.screens;

import mods.Hileb.add_potion.common.menu.PotionFactoryMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PotionFactoryScreen extends AbstractContainerScreen<PotionFactoryMenu> {


	private static final ResourceLocation BG_LOCATION = ResourceLocation.withDefaultNamespace("textures/gui/container/loom.png");


	public PotionFactoryScreen(PotionFactoryMenu menu, Inventory inventory, Component component) {
		super(menu, inventory, component);
	}

	@Override
	public void render(GuiGraphics transform, int x, int y, float partialTicks) {
		super.render(transform, x, y, partialTicks);
		this.renderTooltip(transform, x, y);
	}

	@Override
	protected void renderBg(GuiGraphics transform, float partialTicks, int x, int y) {
		transform.blit(BG_LOCATION, this.leftPos, this.topPos, 0, 0, 256, 256);
	}
}
