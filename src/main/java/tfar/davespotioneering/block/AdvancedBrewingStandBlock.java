package tfar.davespotioneering.block;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import tfar.davespotioneering.Util;
import tfar.davespotioneering.blockentity.AdvancedBrewingStandBlockEntity;
import tfar.davespotioneering.init.ModBlockEntityTypes;

import javax.annotation.Nullable;
import java.util.List;

public class AdvancedBrewingStandBlock extends BrewingStandBlock {
    public AdvancedBrewingStandBlock(Properties properties) {
        super(properties);
    }

    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof AdvancedBrewingStandBlockEntity) {
                player.openMenu((AdvancedBrewingStandBlockEntity)tileentity);
                player.awardStat(Stats.INTERACT_WITH_BREWINGSTAND);
            }
            return InteractionResult.CONSUME;
        }
    }

    public static final int C_LINES = 3;

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        tooltip.add(Component.translatable(getDescriptionId()+".hold_shift.desc"));
        if (Screen.hasShiftDown())
            tooltip.add(this.getShiftDescription().withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable(getDescriptionId()+".hold_ctrl.desc"));
        if (Screen.hasControlDown())
            for (int i = 0; i < C_LINES;i++) {
                tooltip.add(this.getCtrlDescriptions(i).withStyle(ChatFormatting.GRAY));
            }
    }

    public MutableComponent getShiftDescription() {
        return Component.translatable(this.getDescriptionId() + ".shift.desc");
    }

    public MutableComponent getCtrlDescription() {
        return Component.translatable(this.getDescriptionId() + ".ctrl.desc");
    }

    public MutableComponent getCtrlDescriptions(int i) {
        return Component.translatable(this.getDescriptionId() + i +".ctrl.desc");
    }

    public BlockEntity newBlockEntity(BlockPos p_152698_, BlockState p_152699_) {
        return new AdvancedBrewingStandBlockEntity(p_152698_, p_152699_);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity blockentity = pLevel.getBlockEntity(pPos);
            if (blockentity instanceof AdvancedBrewingStandBlockEntity advancedBrewingStandBlockEntity) {
                Util.dropContents(pLevel, pPos, advancedBrewingStandBlockEntity.getBrewingHandler().getStacks());
            }

            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, ModBlockEntityTypes.COMPOUND_BREWING_STAND, AdvancedBrewingStandBlockEntity::serverTick);
    }

}
