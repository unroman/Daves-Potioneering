package tfar.davespotioneering.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.BiomeColors;
import tfar.davespotioneering.ModConfig;
import tfar.davespotioneering.Util;
import tfar.davespotioneering.block.ReinforcedCauldronBlock;
import tfar.davespotioneering.init.ModBlockEntityTypes;
import tfar.davespotioneering.init.ModItems;
import tfar.davespotioneering.init.ModPotions;

import javax.annotation.Nonnull;

public class ReinforcedCauldronBlockEntity extends TileEntity {

    @Nonnull protected Potion potion = Potions.EMPTY;

    public ReinforcedCauldronBlockEntity() {
        this(ModBlockEntityTypes.REINFORCED_CAULDRON);
    }

    public ReinforcedCauldronBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Nonnull
    public Potion getPotion() {
        return potion;
    }

    public void setPotion(@Nonnull Potion potion) {
        this.potion = potion;
        markDirty();
    }

    public int getColor() {
        if (!potion.getEffects().isEmpty()) {
            return PotionUtils.getPotionColor(potion);
        }
        return BiomeColors.getWaterColor(world, pos);
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        potion = Registry.POTION.getOrDefault(new ResourceLocation(nbt.getString("potion")));
        super.read(state, nbt);
    }

    @Nonnull
    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putString("potion", potion.getRegistryName().toString());
        return super.write(compound);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return write(new CompoundNBT());    // okay to send entire inventory on chunk load
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 1, getUpdateTag());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
        this.read(null, packet.getNbtCompound());
        world.notifyBlockUpdate(pos, getBlockState(), getBlockState(), 3);
    }

    public void onEntityCollision(Entity entity) {
        if (entity instanceof ItemEntity && getBlockState().get(ReinforcedCauldronBlock.DRAGONS_BREATH)) {
            ItemStack stack = ((ItemEntity) entity).getItem();

            if (stack.getItem().isIn(ModItems.BLACKLISTED)) return;

            Util.CoatingType coatingType = Util.CoatingType.getCoatingType(stack);

            BlockState blockState = getBlockState();
            int level = blockState.get(CauldronBlock.LEVEL);
            if (potion == ModPotions.MILK && PotionUtils.getPotionFromItem(stack) != Potions.EMPTY && !Util.isPotion(stack)) {
                ReinforcedCauldronBlock.removeCoating(blockState,world,pos,null,stack);
            } else if (stack.getItem() == Items.ARROW && level > 0) {
              ReinforcedCauldronBlock.handleArrowCoating(blockState,world,pos,null,stack,level);
            } else if (level == 3) {

                if (coatingType == Util.CoatingType.TOOL && !ModConfig.Server.coat_tools.get()) return;//check if tools can be coated

                if (coatingType == Util.CoatingType.FOOD && !ModConfig.Server.spike_food.get()) return;//check if food can be coated

                if (coatingType == Util.CoatingType.ANY && !ModConfig.Server.coat_anything.get() && !stack.getItem().isIn(ModItems.WHITELISTED)) return;
                //check if anything can be coated AND the item is not in a whitelist


                //burn off a layer, then schedule the rest of the ticks
                world.playSound(null,pos, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 0.8F, 1);
                ((CauldronBlock)blockState.getBlock()).setWaterLevel(world,pos,blockState,2);
                scheduleTick();
            }
        }
    }

    private void scheduleTick() {
        this.world.getPendingBlockTicks().scheduleTick(this.getPos(), this.getBlockState().getBlock(), ReinforcedCauldronBlock.brew_speed);
    }
}
