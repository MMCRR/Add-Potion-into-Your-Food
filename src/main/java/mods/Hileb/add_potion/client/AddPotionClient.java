package mods.Hileb.add_potion.client;

import mods.Hileb.add_potion.api.AddPotionHelper;
import mods.Hileb.add_potion.api.PotionList;
import mods.Hileb.add_potion.client.screens.PotionFactoryScreen;
import mods.Hileb.add_potion.client.screens.PotionTableScreen;
import mods.Hileb.add_potion.common.AddPotionCommon;
import mods.Hileb.add_potion.common.menu.PotionFactoryMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class AddPotionClient {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        NeoForge.EVENT_BUS.addListener(AddPotionClient::onToolTipShow);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(AddPotionCommon.TABLE_MENU.get(), PotionTableScreen::new);
        event.register(AddPotionCommon.FACTORY_MENU.get(), PotionFactoryScreen::new);
    }

    public static void onToolTipShow(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        if(AddPotionHelper.canPlaceToFoodSlot(itemStack)) {
            if(!AddPotionHelper.isEffectsHiding(itemStack)) {
                PotionList list = AddPotionHelper.getEffectsFromFood(itemStack);
                for (var effect : list) {
                    MutableComponent component = Component.translatable(effect.getDescriptionId());

                    if (effect.getAmplifier() > 0) {
                        component = Component.translatable("potion.withAmplifier", component, Component.translatable("potion.potency." + effect.getAmplifier()));
                    }

                    if (effect.getDuration() > 20) {
                        component = Component.translatable("potion.withDuration", component, MobEffectUtil.formatDuration(effect, 1.0F, event.getContext().tickRate()));
                    }
                    event.getToolTip().add(Component.translatable("add_potion.potion_type." + list.getType().getSerializedName().toLowerCase(), component.withStyle(effect.getEffect().value().getCategory().getTooltipFormatting())));
                }
            }
        }
    }

}
