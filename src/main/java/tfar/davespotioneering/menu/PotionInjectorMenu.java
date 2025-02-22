package tfar.davespotioneering.menu;

import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import tfar.davespotioneering.init.ModMenuTypes;
import tfar.davespotioneering.inv.PotionInjectorHandler;

public class PotionInjectorMenu extends AbstractContainerMenu {

    private final ItemStackHandler inventory;

    //client
    public PotionInjectorMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, new PotionInjectorHandler(8));
    }

    //common
    public PotionInjectorMenu(int id, Inventory playerInventory, ItemStackHandler inventory) {
        super(ModMenuTypes.ALCHEMICAL_GAUNTLET, id);
        this.inventory = inventory;
        // assertInventorySize(inventory, 5);
        // assertIntArraySize(data, 2);

        int potY = 77;

        for (int i = 0; i < 6;i++) {
            this.addSlot(new SlotItemHandler(inventory, i, 26 + 108 * (i/3), 18 + 18 * (i % 3)));
        }

        addSlot(new SlotItemHandler(inventory,6,80, 32));
        addSlot(new SlotItemHandler(inventory,7,80,55));

        int invX = 8;
        int invY = 109;

        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, invX + j * 18, invY + i * 18));
            }
        }

        for(int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, invX + k * 18, invY + 58));
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return true;
    }

    /**
     * Handle when the stack in slot {@code index} is shift-clicked. Normally this moves the stack between the player
     * inventory and the other inventory(s).
     */
    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < this.inventory.getSlots()) {
                if (!this.moveItemStackTo(itemstack1, this.inventory.getSlots(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 0, this.inventory.getSlots(), false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }

    public void handleButton(int button) {
        switch (button) {
            case 0:
                 storePotionsAndBlaze();
                 break;
            case 1:
                removePotionsAndBlaze();
                break;
        }
    }

    public static final int BLAZE_CAP = 32;

    private void storePotionsAndBlaze() {
        ItemStack gauntlet = inventory.getStackInSlot(PotionInjectorHandler.GAUNTLET);
        if (!gauntlet.isEmpty()) {
            CompoundTag newNbt = new CompoundTag();
            ListTag nbt1 = new ListTag();
            CompoundTag oldNBT = gauntlet.getOrCreateTag();

            CompoundTag info = oldNBT.getCompound("info");
            ListTag oldList = info.getList("potions", Tag.TAG_STRING);

            for (int i = 0; i < PotionInjectorHandler.GAUNTLET; i++) {

                Potion oldPotion = oldList.isEmpty() ? Potions.EMPTY : Registry.POTION.get(new ResourceLocation(oldList.get(i).getAsString()));

                if (oldPotion == Potions.EMPTY) {
                    ItemStack potionStack = inventory.getStackInSlot(i);
                    nbt1.add(StringTag.valueOf(Registry.POTION.getKey(PotionUtils.getPotion(potionStack)).toString()));
                    inventory.extractItem(i, 1, false);
                    //copy old potion over
                } else {
                    nbt1.add(oldList.get(i));
                }
            }

            newNbt.putInt("activePotionIndex", 0);
            newNbt.put("potions", nbt1);

            int presentBlaze = info.getInt("blaze");

            int blazeInsert = Math.min(BLAZE_CAP - presentBlaze,Math.min(BLAZE_CAP,inventory.getStackInSlot(PotionInjectorHandler.BLAZE).getCount()));

            newNbt.putInt("blaze",blazeInsert + presentBlaze);
            inventory.extractItem(PotionInjectorHandler.BLAZE,blazeInsert,false);
            gauntlet.getTag().put("info",newNbt);
        }
    }

    private void removePotionsAndBlaze() {
        ItemStack gauntlet = inventory.getStackInSlot(PotionInjectorHandler.GAUNTLET);
        if (!gauntlet.isEmpty()) {
            CompoundTag nbt = gauntlet.getTag().getCompound("info");
            ListTag listNBT = nbt.getList("potions", Tag.TAG_STRING);

            boolean allRemoved = true;
            for (int i = 0; i < listNBT.size(); i++) {
                Tag inbt = listNBT.get(i);

                Potion potion = Registry.POTION.get(new ResourceLocation(inbt.getAsString()));
                if (potion != Potions.EMPTY) {
                    ItemStack present = inventory.getStackInSlot(i);
                    if (present.getCount() < inventory.getSlotLimit(i)) {
                        ItemStack stack = new ItemStack(Items.LINGERING_POTION);
                        PotionUtils.setPotion(stack, potion);
                        inventory.insertItem(i, stack, false);
                        listNBT.set(i,StringTag.valueOf("minecraft:empty"));
                    } else {
                        allRemoved = false;
                    }
                }
            }
            if (allRemoved) {
                nbt.remove("potions");
            }

            int presentBlaze = inventory.getStackInSlot(PotionInjectorHandler.BLAZE).getCount();

            int maxBlazeRemove = inventory.getSlotLimit(PotionInjectorHandler.BLAZE) - presentBlaze;

            int blaze = nbt.getInt("blaze");

            int blazeRemove = Math.min(maxBlazeRemove,blaze);

            inventory.insertItem(PotionInjectorHandler.BLAZE,new ItemStack(Items.BLAZE_POWDER,blazeRemove),false);

            if (blaze > blazeRemove) {
                nbt.putInt("blaze",blaze - blazeRemove);
            } else {
                nbt.remove("blaze");
            }
        }
    }

    public SoundTy getSound(boolean inject) {
        if (inject) {
            for (int i = 0; i < 6; i++) {
                if (!inventory.getStackInSlot(i).isEmpty()) {
                    return SoundTy.BOTH;
                }
            }

            return inventory.getStackInSlot(PotionInjectorHandler.BLAZE).isEmpty() ? SoundTy.NONE : SoundTy.BLAZE;


        } else {
            ItemStack stack = inventory.getStackInSlot(PotionInjectorHandler.GAUNTLET);
            CompoundTag nbt = stack.getTag();
            if (nbt != null) {
                CompoundTag info = nbt.getCompound("info");
                ListTag listNBT = info.getList("potions", Tag.TAG_STRING);
                if (!listNBT.isEmpty()) {
                    for (Tag nb : listNBT) {
                        Potion potion = Registry.POTION.get(new ResourceLocation(nb.getAsString()));
                        if (potion != Potions.EMPTY) {
                            return SoundTy.BOTH;
                        }
                    }
                }
                if (info.getInt("blaze") > 0) {
                    return SoundTy.BLAZE;
                }
            }
            return SoundTy.NONE;
        }
    }

    public enum SoundTy {
     NONE,BLAZE,BOTH
    }
}
