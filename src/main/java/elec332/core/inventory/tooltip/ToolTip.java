package elec332.core.inventory.tooltip;

import com.google.common.collect.Lists;
import elec332.core.client.util.GuiDraw;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Elec332 on 31-7-2015.
 */
public class ToolTip{

    public ToolTip(){
        this((ColouredString) null);
    }

    public ToolTip(ColouredString colouredString){
        this(Lists.newArrayList(colouredString));
    }

    public ToolTip(List<ColouredString> s){
        this.tooltip = s.stream().map(ColouredString::toString).collect(Collectors.toList());
    }

    public ToolTip(String s){
        this(Lists.newArrayList(s));
    }

    public ToolTip(List<String> s, Object... o){
        this.tooltip = s;
    }

    public ToolTip setWidth(int width){
        this.width = width;
        return this;
    }

    private int width = -1;
    private final List<String> tooltip;

    @SideOnly(Side.CLIENT)
    public void renderTooltip(int mouseX, int mouseY, int guiLeft, int guiTop){
        GlStateManager.pushMatrix();
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        List<String> tooltip = this.tooltip;
        if (!tooltip.isEmpty()) {
            GlStateManager.disableRescaleNormal();
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            GlStateManager.translate(mouseX, mouseY, 0);
            int k = 0;
            if (width == -1) {
                for (String colouredString : tooltip) {
                    int l = fontRenderer.getStringWidth(colouredString);
                    if (l > k) {
                        k = l;
                    }
                }
            } else {
                tooltip = Lists.newArrayList();
                for (String s : this.tooltip){
                    for (String s1 : fontRenderer.listFormattedStringToWidth(s, width)) {
                        tooltip.add(s1);
                        int l = fontRenderer.getStringWidth(s1);
                        if (l > k) {
                            k = l;
                        }
                    }
                }
            }
            int j2 = mouseX + 12;// - guiLeft;
            int k2 = mouseY - 12;// - guiTop;
            int i1 = 8;
            if (tooltip.size() > 1) {
                i1 += 2 + (tooltip.size() - 1) * 10;
            }

            if (j2 + k > guiLeft)
            {
                j2 -= 28 + k;
            }

            if (k2 + i1 + 6 > guiTop)
            {
                k2 = guiTop - i1 - 6;
            }
            int j1 = -267386864;
            GuiDraw.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j1, j1);
            GuiDraw.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j1, j1);
            GuiDraw.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j1, j1);
            GuiDraw.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j1, j1);
            GuiDraw.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j1, j1);
            int k1 = 1347420415;
            int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
            GuiDraw.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k1, l1);
            GuiDraw.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k1, l1);
            GuiDraw.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k1, k1);
            GuiDraw.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l1, l1);
            for (int i2 = 0; i2 < tooltip.size(); ++i2) {
                String s1 = tooltip.get(i2);
                fontRenderer.drawStringWithShadow(s1, j2, k2, -1);
                if (i2 == 0) {
                    k2 += 2;
                }
                k2 += 10;
            }
        }
        GlStateManager.popMatrix();
    }

    public static class ColouredString {

        public ColouredString(String s){
            this(TextFormatting.GRAY, s);
        }

        public ColouredString(TextFormatting colour, String s){
            this.string = colour+s;
        }

        private final String string;

        @Override
        public String toString() {
            return this.string;
        }

    }
}
