package tfar.davespotioneering.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import tfar.davespotioneering.blockentity.ReinforcedCauldronBlockEntity;
import tfar.davespotioneering.init.ModBlocks;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Predicate;

import static tfar.davespotioneering.block.LayeredReinforcedCauldronBlock.DRAGONS_BREATH;

/**
 * Copied from @CauldronInteraction
 */
public class ModCauldronInteractions {

    public static final Map<Item,CauldronInteraction> WATER = CauldronInteraction.newInteractionMap();

     public static final Map<Item, CauldronInteraction> EMPTY = CauldronInteraction.newInteractionMap();
    static final Map<Item, CauldronInteraction> LAVA = CauldronInteraction.newInteractionMap();
    static final Map<Item, CauldronInteraction> POWDER_SNOW = CauldronInteraction.newInteractionMap();
    static final CauldronInteraction FILL_WATER = (state, level, pos, player, p_175687_, p_175688_) -> {
        InteractionResult interactionResult = CauldronInteraction.emptyBucket(level, pos, player, p_175687_, p_175688_, ModBlocks.REINFORCED_WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY);
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof ReinforcedCauldronBlockEntity reinforcedCauldronBlock) {
            reinforcedCauldronBlock.setPotion(Potions.WATER);
        }
        return interactionResult;
    };
    static final CauldronInteraction FILL_LAVA = (p_175676_, p_175677_, p_175678_, p_175679_, p_175680_, p_175681_) -> CauldronInteraction.emptyBucket(p_175677_, p_175678_, p_175679_, p_175680_, p_175681_, Blocks.LAVA_CAULDRON.defaultBlockState(), SoundEvents.BUCKET_EMPTY_LAVA);
    static final CauldronInteraction FILL_POWDER_SNOW = (p_175669_, p_175670_, p_175671_, p_175672_, p_175673_, p_175674_) -> CauldronInteraction.emptyBucket(p_175670_, p_175671_, p_175672_, p_175673_, p_175674_, Blocks.POWDER_SNOW_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3), SoundEvents.BUCKET_EMPTY_POWDER_SNOW);

    public static void bootStrap() {
        addDefaultInteractions(EMPTY);
        EMPTY.put(Items.POTION, (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide) {
                Item item = stack.getItem();
                player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.setBlockAndUpdate(pos, ModBlocks.REINFORCED_WATER_CAULDRON.defaultBlockState());

                BlockEntity blockEntity = level.getBlockEntity(pos);

                if (blockEntity instanceof ReinforcedCauldronBlockEntity reinforced) {
                    reinforced.setPotion(PotionUtils.getPotion(stack));
                }

                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        addDefaultInteractions(WATER);
        WATER.put(Items.BUCKET, (p_175725_, p_175726_, p_175727_, p_175728_, p_175729_, p_175730_) -> fillBucket(p_175725_, p_175726_, p_175727_, p_175728_, p_175729_, p_175730_, new ItemStack(Items.WATER_BUCKET), (p_175660_) -> p_175660_.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL));
        WATER.put(Items.GLASS_BOTTLE, (state, level, pos, player, hand, stack) -> {
            if (!level.isClientSide) {
                Item item = stack.getItem();

                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ReinforcedCauldronBlockEntity reinforced) {
                    Potion potion = reinforced.getPotion();
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, PotionUtils.setPotion(new ItemStack(Items.POTION), potion)));
                    if (potion != Potions.WATER)
                        LayeredReinforcedCauldronBlock.lowerFillLevel0(state, level, pos);
                }

                player.awardStat(Stats.USE_CAULDRON);
                player.awardStat(Stats.ITEM_USED.get(item));
                level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                level.gameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }

            return InteractionResult.sidedSuccess(level.isClientSide);
        });
        WATER.put(Items.POTION, (state, level, pos, player, hand, stack) -> {
            if (state.getValue(LayeredCauldronBlock.LEVEL) != 3) {
                if (!level.isClientSide) {
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                    player.awardStat(Stats.USE_CAULDRON);
                    player.awardStat(Stats.ITEM_USED.get(stack.getItem()));

                    BlockEntity blockEntity = level.getBlockEntity(pos);
                    if (blockEntity instanceof ReinforcedCauldronBlockEntity reinforced) {

                        if (reinforced.getPotion() != PotionUtils.getPotion(stack)) {
                            LayeredReinforcedCauldronBlock.boom(level,pos);
                        } else {
                            level.setBlockAndUpdate(pos, state.cycle(LayeredCauldronBlock.LEVEL));
                            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                            level.gameEvent(null, GameEvent.FLUID_PLACE, pos);
                        }
                    }
                }

                return InteractionResult.sidedSuccess(level.isClientSide);
            } else {
                return InteractionResult.PASS;
            }
        });
        WATER.put(Items.LEATHER_BOOTS, CauldronInteraction.DYED_ITEM);
        WATER.put(Items.LEATHER_LEGGINGS, CauldronInteraction.DYED_ITEM);
        WATER.put(Items.LEATHER_CHESTPLATE, CauldronInteraction.DYED_ITEM);
        WATER.put(Items.LEATHER_HELMET, CauldronInteraction.DYED_ITEM);
        WATER.put(Items.LEATHER_HORSE_ARMOR, CauldronInteraction.DYED_ITEM);
        WATER.put(Items.WHITE_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.GRAY_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.BLACK_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.BLUE_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.BROWN_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.CYAN_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.GREEN_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.LIGHT_BLUE_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.LIGHT_GRAY_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.LIME_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.MAGENTA_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.ORANGE_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.PINK_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.PURPLE_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.RED_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.YELLOW_BANNER, CauldronInteraction.BANNER);
        WATER.put(Items.WHITE_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.GRAY_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.BLACK_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.BLUE_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.BROWN_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.CYAN_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.GREEN_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.LIGHT_BLUE_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.LIGHT_GRAY_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.LIME_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.MAGENTA_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.ORANGE_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.PINK_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.PURPLE_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.RED_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);
        WATER.put(Items.YELLOW_SHULKER_BOX, CauldronInteraction.SHULKER_BOX);

        //custom//
        WATER.put(Items.DRAGON_BREATH,ModCauldronInteractions::dragonsBreath);
        WATER.put(Items.ARROW,ModCauldronInteractions::arrowCoating);

        for (Item item : Registry.ITEM) {
            if (item instanceof TieredItem) {
                WATER.put(item,ModCauldronInteractions::weaponCoating);
            } else if (item.isEdible()) {
                WATER.put(item,ModCauldronInteractions::spikedFood);
            }
        }
        //end//

        LAVA.put(Items.BUCKET, (p_175697_, p_175698_, p_175699_, p_175700_, p_175701_, p_175702_) -> fillBucket(p_175697_, p_175698_, p_175699_, p_175700_, p_175701_, p_175702_, new ItemStack(Items.LAVA_BUCKET), (p_175651_) -> true, SoundEvents.BUCKET_FILL_LAVA));
        addDefaultInteractions(LAVA);
        POWDER_SNOW.put(Items.BUCKET, (p_175690_, p_175691_, p_175692_, p_175693_, p_175694_, p_175695_) -> fillBucket(p_175690_, p_175691_, p_175692_, p_175693_, p_175694_, p_175695_, new ItemStack(Items.POWDER_SNOW_BUCKET), (p_175627_) -> p_175627_.getValue(LayeredCauldronBlock.LEVEL) == 3, SoundEvents.BUCKET_FILL_POWDER_SNOW));
        addDefaultInteractions(POWDER_SNOW);
    }

    static void addDefaultInteractions(Map<Item, CauldronInteraction> p_175648_) {
    //    p_175648_.put(Items.LAVA_BUCKET, FILL_LAVA);
        p_175648_.put(Items.WATER_BUCKET, FILL_WATER);
 //       p_175648_.put(Items.POWDER_SNOW_BUCKET, FILL_POWDER_SNOW);
    }

    static InteractionResult fillBucket(BlockState p_175636_, Level p_175637_, BlockPos p_175638_, Player p_175639_, InteractionHand p_175640_, ItemStack p_175641_, ItemStack p_175642_, Predicate<BlockState> p_175643_, SoundEvent p_175644_) {
        if (!p_175643_.test(p_175636_)) {
            return InteractionResult.PASS;
        } else {
            if (!p_175637_.isClientSide) {
                Item item = p_175641_.getItem();
                p_175639_.setItemInHand(p_175640_, ItemUtils.createFilledResult(p_175641_, p_175639_, p_175642_));
                p_175639_.awardStat(Stats.USE_CAULDRON);
                p_175639_.awardStat(Stats.ITEM_USED.get(item));
                p_175637_.setBlockAndUpdate(p_175638_, ModBlocks.REINFORCED_CAULDRON.defaultBlockState());//patch
                p_175637_.playSound(null, p_175638_, p_175644_, SoundSource.BLOCKS, 1.0F, 1.0F);
                p_175637_.gameEvent(null, GameEvent.FLUID_PICKUP, p_175638_);
            }

            return InteractionResult.sidedSuccess(p_175637_.isClientSide);
        }
    }

    @Nonnull
    static InteractionResult dragonsBreath(BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_175715_, ItemStack stack) {
        if (!level.isClientSide) {
            if (!player.getAbilities().instabuild) {
                player.awardStat(Stats.USE_CAULDRON);
                stack.shrink(1);

                ItemStack stack1 = new ItemStack(Items.GLASS_BOTTLE);

                if (!player.getInventory().add(stack1)) {
                    player.drop(stack1, false);
                } else {
                    //     ((ServerPlayer) player).refreshContainer(player.inventoryMenu);
                }
            }
            level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.setBlockAndUpdate(pos,state.setValue(DRAGONS_BREATH,true));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nonnull
    static InteractionResult arrowCoating(BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_175715_, ItemStack stack) {
        LayeredReinforcedCauldronBlock.handleArrowCoating(state,level,pos,player,p_175715_,stack);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nonnull
    static InteractionResult spikedFood(BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_175715_, ItemStack stack) {
        LayeredReinforcedCauldronBlock.handleFoodSpiking(state,level,pos,player,p_175715_,stack);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nonnull
    static InteractionResult weaponCoating(BlockState state, Level level, BlockPos pos, Player player, InteractionHand p_175715_, ItemStack stack) {
        LayeredReinforcedCauldronBlock.handleWeaponCoating(state,level,pos,player,p_175715_,stack);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
