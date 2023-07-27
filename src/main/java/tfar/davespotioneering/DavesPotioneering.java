package tfar.davespotioneering;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import tfar.davespotioneering.block.ModCauldronInteractions;
import tfar.davespotioneering.config.ClothConfig;
import tfar.davespotioneering.init.*;
import tfar.davespotioneering.mixin.BlockEntityTypeAcces;
import tfar.davespotioneering.net.PacketHandler;

import java.util.HashSet;
import java.util.Set;

public class DavesPotioneering implements ModInitializer {
    // Directly reference a log4j logger.

    public static final String MODID = "davespotioneering";
    public static ClothConfig CONFIG;

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModItems.register();
        ModEffects.register();
        ModPotions.register();
        ModBlockEntityTypes.register();
        ModContainerTypes.register();
        ModSoundEvents.register();
        ModParticleTypes.register();

        Util.setStackSize(Items.POTION,ClothConfig.potion_stack_size);
        Util.setStackSize(Items.SPLASH_POTION,ClothConfig.splash_potion_stack_size);
        Util.setStackSize(Items.LINGERING_POTION,ClothConfig.lingering_potion_stack_size);


        Set<Block> newSet = new HashSet<>(((BlockEntityTypeAcces)BlockEntityType.LECTERN).getBlocks());
        newSet.add(ModBlocks.MAGIC_LECTERN);
        ((BlockEntityTypeAcces)BlockEntityType.LECTERN).setBlocks(newSet);


      //  UseItemCallback.EVENT.register(Events::potionCooldown);
        UseEntityCallback.EVENT.register(Events::milkCow);
        AttackEntityCallback.EVENT.register(Events::afterHit);

        AutoConfig.register(ClothConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ClothConfig.class).getConfig();


        PacketHandler.registerMessages();
        ModCauldronInteractions.bootStrap();
    }

    protected static void strongRecipe(Potion potion,Potion strong) {
        BrewingRecipeRegistry.registerPotionRecipe(potion, Items.GLOWSTONE_DUST, strong);
    }

    protected static void extendedRecipe(Potion potion,Potion extended) {
        BrewingRecipeRegistry.registerPotionRecipe(potion, Items.REDSTONE, extended);
    }

    protected static void splashRecipe(Potion potion,Potion splash) {
        BrewingRecipeRegistry.registerPotionRecipe(potion, Items.GUNPOWDER, splash);
    }

    protected static void lingerRecipe(Potion potion,Potion splash) {
        BrewingRecipeRegistry.registerPotionRecipe(potion, Items.DRAGON_BREATH, splash);
    }

    public static void addPotions() {
        strongRecipe(Potions.INVISIBILITY,ModPotions.STRONG_INVISIBILITY);

        ItemStack milkPot = new ItemStack(Items.POTION);
        PotionUtil.setPotion(milkPot,ModPotions.MILK);

        ItemStack splashMilkPot = new ItemStack(Items.SPLASH_POTION);
        PotionUtil.setPotion(splashMilkPot,ModPotions.MILK);

        ItemStack lingerMilkPot = new ItemStack(Items.LINGERING_POTION);
        PotionUtil.setPotion(lingerMilkPot,ModPotions.MILK);

     //   splashRecipe(ModPotions.MILK,splashMilkPot);

      //  lingerRecipe(ModPotions.MILK,lingerMilkPot);
    }
}
