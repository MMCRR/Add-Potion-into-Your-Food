package mods.Hileb.add_potion.common.menu;

import mods.Hileb.add_potion.api.AddPotionHelper;
import mods.Hileb.add_potion.common.AddPotionCommon;
import mods.Hileb.add_potion.common.block.entity.PotionFactoryBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;

public class PotionFactoryMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;

    public PotionFactoryMenu(int containerId, Inventory inventory, Container blockEntity, ContainerLevelAccess access) {
        super(AddPotionCommon.FACTORY_MENU.get(), containerId);
        this.access = access;

        this.addSlot(new Slot(blockEntity, PotionFactoryBlockEntity.SLOT_INPUT, 13, 26) {
            @Override
            public boolean mayPlace(ItemStack p_39918_) {
                return AddPotionHelper.canPlaceToPotionSlot(p_39918_);
            }
        });
        this.addSlot(new Slot(blockEntity, PotionFactoryBlockEntity.SLOT_FOOD, 33, 26) {
            @Override
            public boolean mayPlace(ItemStack p_39927_) {
                return AddPotionHelper.canPlaceToFoodSlot(p_39927_);
            }
        });
        this.addSlot(new Slot(blockEntity, PotionFactoryBlockEntity.SLOT_GLASS_BOTTLE, 23, 45) {
            @Override
            public boolean mayPlace(ItemStack p_39936_) {
                return false;
            }
        });

        this.addSlot(new Slot(blockEntity, PotionFactoryBlockEntity.SLOT_RESULT1, 143, 57) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new Slot(blockEntity, PotionFactoryBlockEntity.SLOT_RESULT2, 143, 57 - 18) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new Slot(blockEntity, PotionFactoryBlockEntity.SLOT_RESULT3, 143, 57 - (2*18)) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }

    }


    public PotionFactoryMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, new SimpleContainer(PotionFactoryBlockEntity.SLOT_COUNT), ContainerLevelAccess.NULL);
    }

    private static final int INV_SLOT_START = PotionFactoryBlockEntity.SLOT_COUNT;
    private static final int INV_SLOT_END = 27 + PotionFactoryBlockEntity.SLOT_COUNT;
    private static final int USE_ROW_SLOT_START = 27 + PotionFactoryBlockEntity.SLOT_COUNT;
    private static final int USE_ROW_SLOT_END = 36 + PotionFactoryBlockEntity.SLOT_COUNT;


    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack ret = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack take = slot.getItem();
            ret = take.copy();
            if(index < INV_SLOT_START) {
                if(!this.moveItemStackTo(take, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if(index < INV_SLOT_END) {
                if(AddPotionHelper.canPlaceToPotionSlot(take)) {
                    if(!this.moveItemStackTo(take, 0,  2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if(AddPotionHelper.canPlaceToFoodSlot(take)) {
                    if(!this.moveItemStackTo(take, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if(!this.moveItemStackTo(take, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                if(AddPotionHelper.canPlaceToPotionSlot(take)) {
                    if(!this.moveItemStackTo(take, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if(AddPotionHelper.canPlaceToFoodSlot(take)) {
                    if(!this.moveItemStackTo(take, 1, 2, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if(!this.moveItemStackTo(take, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if(take.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            }

            slot.setChanged();
            if (take.getCount() == ret.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, take);
            this.broadcastChanges();
        }

        return ret;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, AddPotionCommon.POTION_FACTORY.get());
    }
}
