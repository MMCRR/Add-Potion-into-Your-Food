package mods.Hileb.add_potion.api.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;

import javax.annotation.Nullable;

public class APPotionAffectEvent extends LivingEvent implements ICancellableEvent {
	protected final ItemStack food;
	protected final ServerLevel serverLevel;
	protected Vec3 pos;
	@Nullable
	protected LivingEntity owner;

	public APPotionAffectEvent(@Nullable LivingEntity effectAcceptor, ServerLevel serverLevel, Vec3 pos, ItemStack food, @Nullable LivingEntity owner) {
		super(effectAcceptor);
		this.serverLevel = serverLevel;
		this.pos = pos;
		this.food = food;
		this.owner = owner;
	}

	public ItemStack getFood() {
		return this.food;
	}

	public Vec3 getPos() {
		return pos;
	}

	public void setPos(Vec3 pos) {
		this.pos = pos;
	}

	public ServerLevel getLevel() {
		return serverLevel;
	}

	public void setOwner(@Nullable LivingEntity owner) {
		this.owner = owner;
	}

	@Nullable
	public LivingEntity getOwner() {
		return owner;
	}
}
