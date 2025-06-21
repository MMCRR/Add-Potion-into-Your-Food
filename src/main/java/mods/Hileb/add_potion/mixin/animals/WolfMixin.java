package mods.Hileb.add_potion.mixin.animals;

import mods.Hileb.add_potion.api.AddPotionHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Wolf.class)
public abstract class WolfMixin {
	@Inject(method = "mobInteract", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/Wolf;heal(F)V", shift = At.Shift.AFTER))
	public void applyAPEffectsTo(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		Wolf current = (Wolf)(Object)this;
		ItemStack itemStack = player.getItemInHand(hand);
		if(current.level() instanceof ServerLevel serverLevel) {
			AddPotionHelper.onFoodEaten(current, serverLevel, itemStack);
		}
	}
}
