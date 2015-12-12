package elec332.core.client;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;

/**
 * Created by Elec332 on 31-7-2015.
 */
@SideOnly(Side.CLIENT)
public class RenderHelper {

    public static final float renderUnit = 1/16f;
    private static final Tessellator mcTessellator;
    private static final ITessellator tessellator;
    private static final Minecraft mc;
    private static final RenderBlocks renderBlocks;
    private static final Map<WorldRenderer, ITessellator> worldRenderTessellators;

    public static ITessellator forWorldRenderer(WorldRenderer renderer){
        ITessellator ret = worldRenderTessellators.get(renderer);
        if (ret == null){
            ret = new ElecTessellator(renderer);
            worldRenderTessellators.put(renderer, ret);
        }
        return ret;
    }

    public static RenderBlocks getBlockRenderer(){
        return renderBlocks;
    }

    public static FontRenderer getMCFontrenderer(){
        return mc.fontRendererObj;
    }

    public static ITessellator getTessellator(){
        return tessellator;
    }

    public static Vec3 getPlayerVec(float partialTicks){
        EntityPlayer player = mc.thePlayer;
        double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * partialTicks;
        double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * partialTicks;
        double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * partialTicks;
        return new Vec3(dX, dY, dZ);
    }

    public static Vec3 getPlayerVec(){
        EntityPlayer player = mc.thePlayer;
        return new Vec3(player.posX, player.posY, player.posZ);
    }

    public static void drawLine(Vec3 from, Vec3 to, Vec3 player, float thickness){
        drawQuad(from, from.addVector(thickness, thickness, thickness), to, to.addVector(thickness, thickness, thickness));
    }

    public static void drawQuad(Vec3 v1, Vec3 v2, Vec3 v3, Vec3 v4){
        tessellator.addVertexWithUV(v1.xCoord, v1.yCoord, v1.zCoord, 0, 0);
        tessellator.addVertexWithUV(v2.xCoord, v2.yCoord, v2.zCoord, 1, 0);
        tessellator.addVertexWithUV(v3.xCoord, v3.yCoord, v3.zCoord, 1, 1);
        tessellator.addVertexWithUV(v4.xCoord, v4.yCoord, v4.zCoord, 0, 1);
    }

    public static Vec3 multiply(Vec3 original, double m){
        return new Vec3(original.xCoord * m, original.yCoord * m, original.zCoord * m);
    }

    public static void bindBlockTextures(){
        bindTexture(getBlocksResourceLocation());
    }

    public static void bindTexture(ResourceLocation rl){
        mc.renderEngine.bindTexture(rl);
    }

    public static TextureAtlasSprite checkIcon(TextureAtlasSprite icon) {
        if (icon == null)
            return getMissingTextureIcon();
        return icon;
    }

    public static TextureAtlasSprite getFluidTexture(Fluid fluid, boolean flowing) {
        if (fluid == null)
            return getMissingTextureIcon();
        return checkIcon(flowing ? getIconFrom(fluid.getFlowing()) : getIconFrom(fluid.getStill()));
    }

    public static TextureAtlasSprite getMissingTextureIcon(){
        return mc.getTextureMapBlocks().getMissingSprite();//((TextureMap) Minecraft.getMinecraft().getTextureManager().getTexture(getBlocksResourceLocation())).getAtlasSprite("missingno");
    }

    public static TextureAtlasSprite getIconFrom(ResourceLocation rl){
        return mc.getTextureMapBlocks().getAtlasSprite(rl.toString());
    }

    public static ResourceLocation getBlocksResourceLocation(){
        return TextureMap.locationBlocksTexture;
    }

    public void spawnParticle(EntityFX particle){
        mc.effectRenderer.addEffect(particle);
    }

    static {
        mcTessellator = Tessellator.getInstance();
        tessellator = new ElecTessellator(mcTessellator);
        mc = Minecraft.getMinecraft();
        renderBlocks = new RenderBlocks();
        worldRenderTessellators = Maps.newHashMap();
        worldRenderTessellators.put(mcTessellator.getWorldRenderer(), tessellator);
    }

}