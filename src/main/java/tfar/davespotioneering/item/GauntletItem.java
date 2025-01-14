package tfar.davespotioneering.item;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.level.Level;
import tfar.davespotioneering.ModConfig;
import tfar.davespotioneering.init.ModSoundEvents;
import tfar.davespotioneering.menu.PotionInjectorMenu;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class GauntletItem extends SwordItem {

    public static final String ACTIVE = "active";
    public static final String ACTIVE_POTION = "activePotionIndex";
    public static final String BLAZE = "blaze";
    public static final String INFO = "info";
    public static final String COOLDOWNS = "potionCooldownMap";
    public static final String POTIONS = "potions";
    public static final int SLOTS = 6;

    public GauntletItem(Properties properties) {
        super(Tiers.NETHERITE, 4, -2.8f, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player playerIn, InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        if (playerIn.isShiftKeyDown()) {
//                PacketHandler.sendToClient(new GauntletHUDMovementGuiPacket(), (ServerPlayerEntity) playerIn);


            boolean active = stack.getOrCreateTag().getBoolean(ACTIVE);

            int blaze = getBlaze(stack);

            if (!world.isClientSide && (blaze > 0 || active)) {
                stack.getOrCreateTag().putBoolean(ACTIVE, !active);
                world.playSound(null,playerIn.getX(),playerIn.getY(),playerIn.getZ(),active ? ModSoundEvents.GAUNTLET_TURNING_OFF : ModSoundEvents.GAUNTLET_TURNING_ON, SoundSource.PLAYERS,.5f,1);
            } else {
            }
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    public MutableComponent getCtrlDescriptions(int i) {
        return Component.translatable(this.getDescriptionId() + i +".ctrl.desc");
    }

    @Override
    public int getDamage(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        double blaze = 0;
        if (tag != null) {
            CompoundTag info = tag.getCompound(INFO);
            blaze = info.getInt(BLAZE);
        }
        return PotionInjectorMenu.BLAZE_CAP - (int) blaze;
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack.getItem() != newStack.getItem();
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        CompoundTag info = stack.getOrCreateTag().getCompound(INFO);
        double blaze = info.getInt(BLAZE);
        return blaze > 0;
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ChatFormatting.GOLD.getColor();
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity victim, LivingEntity attacker) {
        if (stack.getItem() instanceof GauntletItem) {
            CompoundTag info = stack.getOrCreateTag().getCompound(INFO);
            Potion[] potions = getPotionsFromNBT(info);
            if (attacker instanceof Player) {

                boolean active = stack.getTag().getBoolean(ACTIVE);

                if (potions != null && getCooldownFromPotionByIndex(info.getInt(INFO), stack) <= 0 && info.getInt(BLAZE) > 0 && active) {
                    Potion potion = potions[0];
                    for (MobEffectInstance effectInstance : potion.getEffects()) {
                        victim.addEffect(new MobEffectInstance(effectInstance));
                    }
                    info.putInt(BLAZE, info.getInt(BLAZE) - 1);

                    if (info.getInt(BLAZE) == 0) {
                        stack.getTag().putBoolean(ACTIVE,false);
                    }

                    ListTag cooldownMap;
                    if (info.get(COOLDOWNS) instanceof ListTag) {
                        cooldownMap = (ListTag) info.get(COOLDOWNS);
                    } else {
                        cooldownMap = new ListTag();
                        cooldownMap.add(0, new IntArrayTag(new ArrayList<>()));
                        cooldownMap.add(1, new IntArrayTag(new ArrayList<>()));
                    }
                    addPotionCooldownByIndex(info.getInt(ACTIVE_POTION), ModConfig.Server.gauntlet_cooldown.get(), stack, cooldownMap);
                }
            }
        }
        return super.hurtEnemy(stack, victim, attacker);
    }

    public static final int C_LINES = 3;

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        tooltip.add(Component.translatable(getDescriptionId()+".hold_shift.desc"));
        if (Screen.hasShiftDown())
            tooltip.add(this.getShiftDescription().withStyle(ChatFormatting.GRAY));

        tooltip.add(Component.translatable(getDescriptionId()+".hold_ctrl.desc"));
        if (Screen.hasControlDown())
            for (int i = 0; i < C_LINES;i++) {
                tooltip.add(this.getCtrlDescriptions(i).withStyle(ChatFormatting.GRAY));
            }

        Tuple<List<MobEffectInstance>, List<Potion>> tuple = getEffectsFromGauntlet(stack);
        if (tuple == null) return;
        if (tuple.getA().isEmpty()) return;
        tooltip.add(Component.literal(" "));

        for (MobEffectInstance instance : tuple.getA()) {
            MutableComponent effectFormatted = Component.translatable(instance.getDescriptionId());
            effectFormatted.withStyle(instance.getEffect().getCategory().getTooltipFormatting());
            Component amplifier = Component.literal("");
            Component duration;
            Component product;
            if (instance.getAmplifier() > 0) {
                amplifier = Component.literal(String.valueOf(instance.getAmplifier()));
            }
            if (instance.getDuration() > 1) {
                duration = Component.literal(MobEffectUtil.formatDuration(instance, 1f));
                product = Component.translatable("davespotioneering.tooltip.gauntlet.withDuration", effectFormatted, amplifier, duration);
            } else {
                product = Component.translatable("davespotioneering.tooltip.gauntlet", effectFormatted, amplifier);
            }

            tooltip.add(product);

            }


    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, itemSlot, isSelected);
        if (entity instanceof Player && !entity.getCommandSenderWorld().isClientSide()) {
            modifyCooldowns(stack, (cd) -> {
                if (cd > 0) cd -= 1;
                return cd;
            });
        }
    }

    public MutableComponent getShiftDescription() {
        return Component.translatable(this.getDescriptionId() + ".shift.desc");
    }

    public MutableComponent getCtrlDescription() {
        return Component.translatable(this.getDescriptionId() + ".ctrl.desc");
    }

    @Nullable
    public static Tuple<List<MobEffectInstance>, List<Potion>> getEffectsFromGauntlet(ItemStack stack) {
        if (!stack.hasTag()) return null;
        ListTag nbts = stack.getTag().getCompound(INFO).getList(POTIONS, Tag.TAG_STRING);
        List<MobEffectInstance> effects = new ArrayList<>();
        List<Potion> potions = new ArrayList<>();
        for (Tag inbt : nbts) {
            if (inbt instanceof StringTag stringNBT) {
                Potion potion = Registry.POTION.get(new ResourceLocation(stringNBT.getAsString()));
                effects.addAll(potion.getEffects());
                potions.add(potion);
            }
        }
        return new Tuple<>(effects, potions);
    }

    public static void cycleGauntletForward(Player player) {
        if (player == null) return;
        CompoundTag info = player.getMainHandItem().getOrCreateTag().getCompound(INFO);
        ListTag nbts = info.getList(POTIONS, Tag.TAG_STRING);
        if (nbts.isEmpty()) return;
        int index = info.getInt(ACTIVE_POTION);
        index++;
        if (index > 5) {
            index = 0;
        }
        info.putInt(ACTIVE_POTION, index);
    }

    public static void cycleGauntletBackward(Player player) {
        if (player == null) return;
        CompoundTag info = player.getMainHandItem().getOrCreateTag().getCompound(INFO);
        ListTag nbts = info.getList(POTIONS, Tag.TAG_STRING);
        if (nbts.isEmpty()) return;
        int index = info.getInt(ACTIVE_POTION);
        index--;
        if (index < 0) {
            index = 5;
        }
        info.putInt(ACTIVE_POTION, index);
    }

    public static Potion[] getPotionsFromNBT(CompoundTag info) {
        ListTag nbts = info.getList(POTIONS, Tag.TAG_STRING);
        if (nbts.isEmpty()) return null;

        // get active potion
        int index = info.getInt(ACTIVE_POTION);
        index--;
        if (index < 0) {
            index = 5;
        }

        // get the potion in front of active potion
        Tag pre = nbts.get(index);
        if (pre == null) return null;

        index += 2;
        index %= 6;

        // get the potion behind of active potion
        Tag post = nbts.get(index);
        if (post == null) return null;

        Potion activePotion = Registry.POTION.get(new ResourceLocation(nbts.get(info.getInt(ACTIVE_POTION)).getAsString()));
        Potion prePotion = Registry.POTION.get(new ResourceLocation(pre.getAsString()));
        Potion postPotion = Registry.POTION.get(new ResourceLocation(post.getAsString()));

        return new Potion[]{activePotion, prePotion, postPotion};
    }

    public static ListTag addPotionCooldownByIndex(int index, int cooldown, ItemStack stack, ListTag cooldownMap) {
        CompoundTag info = stack.getOrCreateTag().getCompound(INFO);
        if (cooldownMap.get(0) instanceof IntArrayTag) {
            if (cooldownMap.get(1) instanceof IntArrayTag cooldownArray) {
                IntArrayTag indexArray = (IntArrayTag) cooldownMap.get(0);

                indexArray.add(IntTag.valueOf(index));
                cooldownArray.add(IntTag.valueOf(cooldown));

                ListTag list = new ListTag();
                list.add(0, indexArray);
                list.add(1, cooldownArray);

                info.put(COOLDOWNS, list);
                return list;
            }
        }
        return cooldownMap;
    }

    public static int getCooldownFromPotionByIndex(int indexOfPotion, ItemStack stack) {
        CompoundTag info = stack.getOrCreateTag().getCompound(INFO);
        Tag inbt = info.get(COOLDOWNS);
        if (inbt instanceof ListTag cooldownMap) {
            if (cooldownMap.get(0) instanceof IntArrayTag) {
                if (cooldownMap.get(1) instanceof IntArrayTag cooldownArray) {
                    IntArrayTag indexArray = (IntArrayTag) cooldownMap.get(0);
                    try {
                        int indexOfPotionIndex = toList(indexArray.getAsIntArray()).indexOf(indexOfPotion);
                        return toList(cooldownArray.getAsIntArray()).get(indexOfPotionIndex);
                    } catch (Exception ignore) {
                        // if the potion doesn't have cooldown an IndexOutOfBounds exception will be thrown, but it is not an actual problem, so we are just ignoring it
                    }
                }
            }
        }
        return 0;
    }

    public static void modifyCooldowns(ItemStack gauntlet, Function<Integer, Integer> modifier) {
        CompoundTag info = gauntlet.getOrCreateTag().getCompound(INFO);
        Tag inbt = info.get(COOLDOWNS);
        if (inbt instanceof ListTag map) {
            if (map.get(0) instanceof IntArrayTag indexArray && map.get(1) instanceof IntArrayTag cooldownArray) {
                if (cooldownArray.isEmpty() || indexArray.isEmpty()) return;
                if (cooldownArray.getAsIntArray().length != indexArray.getAsIntArray().length) return;
                List<Integer> cooldownList = new ArrayList<>();
                List<Integer> indexList = new ArrayList<>();
                for (int i = 0; i < cooldownArray.getAsIntArray().length; i++) {
                    int modified = modifier.apply(cooldownArray.getAsIntArray()[i]);
                    // copying over the cooldown and index to a new list, remove ones that are already expired
                    if (modified > 0) {
                        cooldownList.add(modified);
                        indexList.add(indexArray.getAsIntArray()[i]);
                    }
                }
                IntArrayTag newArray = new IntArrayTag(cooldownList);
                IntArrayTag newArrayIndex = new IntArrayTag(indexList);
                map.set(1, newArray);
                map.set(0, newArrayIndex);
            }
        }
    }

    public static List<ItemStack> getItemsFromInventory(ItemStack item, Inventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack stack : inventory.items) {
            if (stack.sameItem(item)) items.add(stack);
        }
        for (ItemStack stack : inventory.offhand) {
            if (stack.sameItem(item)) items.add(stack);
        }
        for (ItemStack stack : inventory.armor) {
            if (stack.sameItem(item)) items.add(stack);
        }
        return items;
    }

    public static List<Integer> toList(int... in) {
        List<Integer> list = new ArrayList<>();
        for (int i : in) {
            list.add(i);
        }
        return list;
    }

    public static int getBlaze(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        int blaze = 0;
        if (tag != null) {
            CompoundTag info = tag.getCompound(INFO);
            blaze = info.getInt(BLAZE);
        }
        return blaze;
    }
}
