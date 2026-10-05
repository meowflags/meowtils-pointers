/*
 * Decompiled with CFR 0.152.
 */
package meowtils.tenacitygui.tenacity.panel.settings;

import java.awt.Color;
import net.minecraft.client.renderer.GlStateManager;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;

public class BooleanComponent
extends SettingComponent {
    private final BoolAccess access;
    private final Object sourceValue;
    private final Animation toggleAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
    private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);

    public BooleanComponent(BoolAccess access) {
        this.access = access;
        this.sourceValue=ExtensionControlSupport.underlyingValue(access);
    }

    @Override
    public String getName() {
        return this.access.name();
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        drawControl(mouseX,mouseY);
        if(!ExtensionControlSupport.active(this.sourceValue))ExtensionControlSupport.shade(this);
    }

    private void drawControl(int mouseX,int mouseY){
        this.toggleAnimation.setDirection(this.access.get() ? Direction.FORWARDS : Direction.BACKWARDS);
        RenderUtil.resetColor();
        float textScale="Hide Teammates".equals(this.access.name())?1:Math.min(1,(this.width-34)/Math.max(1,Fonts.tenacityFont16.getStringWidth(this.access.name())));
        GlStateManager.func_179094_E();
        try{
            GlStateManager.func_179109_b(this.x+5,this.y+Fonts.tenacityFont16.getMiddleOfBox(this.height),0);
            GlStateManager.func_179152_a(textScale,textScale,1);
            Fonts.tenacityFont16.drawString(this.access.name(),0,0,ColorUtil.applyOpacity(this.textColor,0.5f+0.5f*this.toggleAnimation.getOutput().floatValue()));
        }finally{GlStateManager.func_179121_F();}
        float switchWidth = 17.0f;
        float switchHeight = 7.0f;
        float booleanX = this.x + this.width - (switchWidth + 5.5f);
        float booleanY = this.y + this.height / 2.0f - switchHeight / 2.0f;
        boolean hovering = HoveringUtil.isHovering(booleanX - 2.0f, booleanY - 2.0f, switchWidth + 4.0f, switchHeight + 4.0f, mouseX, mouseY);
        this.hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
        Color accentCircle = ColorUtil.applyOpacity((Color)this.clientColors.getSecond(), this.alpha);
        Color rectColor = ColorUtil.interpolateColorC(this.settingRectColor.brighter().brighter(), accentCircle, this.toggleAnimation.getOutput().floatValue());
        rectColor = ColorUtil.interpolateColorC(rectColor, ColorUtil.brighter(rectColor, 0.8f), this.hoverAnimation.getOutput().floatValue());
        RenderUtil.resetColor();
        RoundedUtil.drawRound(booleanX, booleanY, switchWidth, switchHeight, 3.0f, rectColor);
        RenderUtil.resetColor();
        RoundedUtil.drawRound(this.x + this.width - (switchWidth + 4.0f) + (switchWidth - 8.0f) * this.toggleAnimation.getOutput().floatValue(), this.y + Fonts.tenacityFont16.getMiddleOfBox(this.height) + 0.5f, 5.0f, 5.0f, 2.0f, this.textColor);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if(!ExtensionControlSupport.active(this.sourceValue))return;
        float switchWidth = 17.0f;
        float switchHeight = 7.0f;
        float booleanX = this.x + this.width - (switchWidth + 5.5f);
        float booleanY = this.y + this.height / 2.0f - switchHeight / 2.0f;
        boolean hovering = HoveringUtil.isHovering(booleanX - 2.0f, booleanY - 2.0f, switchWidth + 4.0f, switchHeight + 4.0f, mouseX, mouseY);
        if (this.isClickable(booleanY + switchHeight) && hovering && button == 0) {
            this.access.toggle();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    public static interface BoolAccess {
        public String name();

        public boolean get();

        public void toggle();
    }
}

