package tfar.davespotioneering.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.CauldronBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.BiomeColors;
import tfar.davespotioneering.block.ReinforcedCauldronBlock;
import tfar.davespotioneering.init.ModBlockEntityTypes;

import javax.annotation.Nonnull;

public class ReinforcedCauldronBlockEntity extends TileEntity {

    protected Potion potion = Potions.EMPTY;

    public ReinforcedCauldronBlockEntity() {
        this(ModBlockEntityTypes.REINFORCED_CAULDRON);
    }

    public ReinforcedCauldronBlockEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    public Potion getPotion() {
        return potion;
    }

    public void setPotion(Potion potion) {
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
        if (entity instanceof ItemEntity) {
            ItemStack stack = ((ItemEntity)entity).getItem();
            BlockPos blockpos = this.getPos();
            BlockState blockState = getBlockState();
            if (blockState.get(CauldronBlock.LEVEL) == 3) {
                ReinforcedCauldronBlock.handleCoating(blockState,world,blockpos,null,stack);
            }
        }
    }
}
