package mods.Hileb.add_potion.common.block.entity;

import mods.Hileb.add_potion.api.AddPotionHelper;
import mods.Hileb.add_potion.common.AddPotionCommon;
import mods.Hileb.add_potion.common.menu.PotionFactoryMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Tuple;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;

public class PotionFactoryBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FOOD = 1;
    public static final int SLOT_RESULT1 = 2;
    public static final int SLOT_RESULT2 = 3;
    public static final int SLOT_RESULT3 = 4;
    public static final int SLOT_GLASS_BOTTLE = 5;
    public static final int SLOT_COUNT = 6;

    protected NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    public PotionFactoryBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public PotionFactoryBlockEntity(BlockPos pos, BlockState blockState) {
        super(AddPotionCommon.POTION_FACTORY_BLOCK_ENTITY.get(), pos, blockState);
    }

    @Override
    protected Component getDefaultName() {
        return AddPotionCommon.POTION_FACTORY.get().getName();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> nonNullList) {
        items = nonNullList;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack itemstack : this.items) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack itemStack) {
        return switch (index) {
            case SLOT_INPUT -> AddPotionHelper.canPlaceToPotionSlot(itemStack);
            case SLOT_FOOD -> AddPotionHelper.canPlaceToFoodSlot(itemStack);
            default -> false;
        };
    }

    @Override
    public ItemStack getItem(int index) {
        return this.items.get(index);
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(this.items, index, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.items, index);
    }

    @Override
    public void setItem(int index, ItemStack itemStack) {
        this.items.set(index, itemStack);
        this.setChanged();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }
        return this.worldPosition.closerToCenterThan(player.position(), 64.0D);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new PotionFactoryMenu(containerId, inventory, this, ContainerLevelAccess.create(inventory.player.level(), PotionFactoryBlockEntity.this.worldPosition));
    }

    private static final int[] SLOTS_FOR_UP = new int[]{SLOT_INPUT};
    private static final int[] SLOTS_FOR_DOWN = new int[]{SLOT_RESULT1, SLOT_RESULT2, SLOT_RESULT3, SLOT_GLASS_BOTTLE};
    private static final int[] SLOTS_FOR_SIDES = new int[]{SLOT_FOOD};

    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        }
        return direction == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack itemStack, @Nullable Direction direction) {
        return this.canPlaceItem(index, itemStack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack itemStack, Direction direction) {
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items, registries);
    }

    public static void serverTick(Level level, BlockPos blockPos, @SuppressWarnings("unused") BlockState blockState, PotionFactoryBlockEntity blockEntity) {
        if(!level.isClientSide) {
            if(level.hasNeighborSignal(blockPos)) {
                ItemStack potion = blockEntity.getItem(SLOT_INPUT);
                ItemStack food = blockEntity.getItem(SLOT_FOOD);
                if(!potion.isEmpty() && !food.isEmpty()) {
                    Tuple<ItemStack, Optional<ItemStack>> result = AddPotionHelper.applyEffectsToFood(null, potion, food);
                    if(result == null) {
                        return;
                    }
                    for (int i = SLOT_RESULT1; i <= SLOT_RESULT3; ++i) {
                        ItemStack slot = blockEntity.getItem(i);
                        if (slot.isEmpty()) {
                            blockEntity.items.set(i, result.getA());
                        } else if (ItemStack.isSameItemSameComponents(slot, result.getA())) {
                            blockEntity.getItem(i).grow(result.getA().getCount());
                        } else {
                            continue;
                        }
                        result.getB().ifPresentOrElse(
                                itemStack -> {
                                    if(AddPotionHelper.canPlaceToPotionSlot(itemStack)) {
                                        blockEntity.items.set(SLOT_INPUT, itemStack);
                                    } else {
                                        ItemStack slotBottle = blockEntity.getItem(SLOT_GLASS_BOTTLE);
                                        if(slotBottle.isEmpty()) {
                                            blockEntity.items.set(SLOT_GLASS_BOTTLE, itemStack);
                                        } else if (ItemStack.isSameItemSameComponents(slotBottle, itemStack)) {
                                            blockEntity.getItem(SLOT_GLASS_BOTTLE).grow(itemStack.getCount());
                                        } else {
                                            double d0 = level.random.nextDouble() * 0.7D + 0.15D;
                                            double d1 = level.random.nextDouble() * 0.7D + 2.0D / 3.0D;
                                            double d2 = level.random.nextDouble() * 0.7D + 0.15D;
                                            ItemEntity itementity = new ItemEntity(level, blockPos.getX() + d0, blockPos.getY() + d1, blockPos.getZ() + d2, itemStack);
                                            itementity.setDefaultPickUpDelay();
                                            level.addFreshEntity(itementity);
                                        }
                                        potion.shrink(1);
                                    }
                                },
                                () -> potion.shrink(1)
                        );
                        food.shrink(1);
                        blockEntity.setChanged();
                        break;
                    }
                }
            }
        }
    }
}
