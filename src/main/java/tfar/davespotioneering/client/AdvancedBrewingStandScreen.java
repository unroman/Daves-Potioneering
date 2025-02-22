package tfar.davespotioneering.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import tfar.davespotioneering.DavesPotioneering;
import tfar.davespotioneering.blockentity.AdvancedBrewingStandBlockEntity;
import tfar.davespotioneering.menu.AdvancedBrewingStandMenu;

public class AdvancedBrewingStandScreen extends AbstractContainerScreen<AdvancedBrewingStandMenu> {

    private static final ResourceLocation BREWING_STAND_GUI_TEXTURES = new ResourceLocation(DavesPotioneering.MODID,"textures/gui/compound_brewing_stand.png");
    private static final int[] BUBBLELENGTHS = new int[]{29, 24, 20, 16, 11, 6, 0};

    public AdvancedBrewingStandScreen(AdvancedBrewingStandMenu p_i51097_1_, Inventory p_i51097_2_, Component p_i51097_3_) {
        super(p_i51097_1_, p_i51097_2_, p_i51097_3_);
        imageHeight += 26;
        this.inventoryLabelY += 28;
    }

    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderTooltip(matrixStack, mouseX, mouseY);
    }

    protected void renderBg(PoseStack matrixStack, float partialTicks, int x, int y) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BREWING_STAND_GUI_TEXTURES);
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.blit(matrixStack, i, j, 0, 0, this.imageWidth, this.imageHeight);
        int fuel = this.menu.getFuel();
        int l = Mth.clamp((18 * fuel + 20 - 1) / 20, 0, 18);

        int y1 = 42;

        if (l > 0) {
            this.blit(matrixStack, i + 60, j + 28 + y1, 176, 29, l, 4);
        }


        int brewTime = this.menu.getBrewTime();
        if (brewTime > 0) {
            int length = (int)(28.0F * (1.0F - (float)brewTime / AdvancedBrewingStandBlockEntity.TIME));
            if (length > 0) {
                this.blit(matrixStack, i + 97, j + y1, 176, 0, 9, length);
            }

            length = BUBBLELENGTHS[brewTime / 2 % 7];
            if (length > 0) {
                this.blit(matrixStack, i + 63, j + y1 + 27 - length, 185, 29 - length, 12, length);
            }
        }

    }
}
