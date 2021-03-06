package elec332.core.inventory.widget;

import com.google.common.collect.Lists;
import elec332.core.client.util.GuiDraw;
import elec332.core.inventory.window.Window;
import elec332.core.util.NBTHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

/**
 * Created by Elec332 on 21-8-2015.
 */
@SuppressWarnings("unused")
public class WidgetButton extends Widget {

    public WidgetButton(int x, int y, int width, int height){
        this(x, y, 0, 0, width, height);
    }

    public WidgetButton(int x, int y, int width, int height, IButtonEventListener... events) {
        this(x, y, 0, 0, width, height, events);
    }

    public WidgetButton(int x, int y, int u, int v, int width, int height) {
        super(x, y, u, v, width, height);
        this.buttonEvents = Lists.newArrayList();
        this.active = true;
    }

    public WidgetButton(int x, int y, int u, int v, int width, int height, IButtonEventListener... events){
        this(x, y, u, v, width, height);
        Collections.addAll(buttonEvents, events);
    }

    protected static final ResourceLocation buttonTextures = new ResourceLocation("textures/gui/widgets.png");
    private List<IButtonEventListener> buttonEvents;
    private boolean active;
    protected String displayString;

    public WidgetButton setDisplayString(String s){
        this.displayString = s;
        return this;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public final boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOver(mouseX, mouseY) && active){
            onButtonClicked();
            sendNBTChangesToServer(new NBTHelper().addToTag(1, "id").serializeNBT());
            return true;
        }
        return false;
    }

    @Override
    public void readNBTChangesFromPacketServerSide(NBTTagCompound tagCompound) {
        if (tagCompound.getInteger("id") == 1){
            onButtonClicked();
        }
    }

    public void onButtonClicked(){
        sendButtonEvents();
    }

    @Override
    public void draw(Window gui, int guiX, int guiY, int mouseX, int mouseY) {
        if (!isHidden()) {
            GlStateManager.pushMatrix();
            FontRenderer fontrenderer = Minecraft.getMinecraft().fontRendererObj;
            bindTexture(buttonTextures);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            boolean hovering = isMouseOver(mouseX, mouseY);
            int k = this.getHoverState(hovering);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            //Left half
            GuiDraw.drawTexturedModalRect(guiX + this.x, guiY + this.y, 0, 46 + k * 20, this.width / 2, this.height / 2);
            GuiDraw.drawTexturedModalRect(guiX + this.x, guiY + this.y + this.height / 2, 0, 46 + k * 20 + 20 - this.height / 2, this.width / 2, this.height / 2);
            //Right half
            GuiDraw.drawTexturedModalRect(guiX + this.x + this.width / 2, guiY + this.y, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height / 2);
            GuiDraw.drawTexturedModalRect(guiX + this.x + this.width / 2, guiY + this.y + this.height / 2, 200 - this.width / 2, 46 + k * 20 + 20 - this.height / 2, this.width / 2, this.height / 2);
            //Vanilla code
            //gui.drawTexturedModalRect(guiX + this.x, guiY + this.y, 0, 46 + k * 20, this.width / 2, this.height);
            //gui.drawTexturedModalRect(guiX + this.x + this.width / 2, guiY + this.y, 200 - this.width / 2, 46 + k * 20, this.width / 2, this.height);
            int l = 14737632;
            if (!active) {
                l = 10526880;
            } else if (hovering) {
                l = 16777120;
            }
            GuiDraw.drawCenteredString(fontrenderer, this.displayString, guiX + this.x + this.width / 2, guiY + this.y + (this.height - 8) / 2, l);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.popMatrix();
        }
    }

    protected int getHoverState(boolean hovering) {
        byte b = 1;
        if (!active) {
            b = 0;
        } else if (hovering) {
            b = 2;
        }
        return b;
    }

    public void sendButtonEvents(){
        for (IButtonEventListener event : buttonEvents)
            event.onButtonClicked(this);
    }

    public WidgetButton addButtonEvent(IButtonEventListener event){
        if (!buttonEvents.contains(event))
            buttonEvents.add(event);
        return this;
    }

    public void removeButtonEvent(IButtonEventListener event){
        buttonEvents.remove(event);
    }

    public void clearEvents(){
        buttonEvents.clear();
    }

    public static interface IButtonEventListener {

        public void onButtonClicked(WidgetButton button);

    }
}
