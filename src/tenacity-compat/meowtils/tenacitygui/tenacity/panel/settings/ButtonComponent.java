/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  wtf.tatp.meowtils.gui.values.ButtonValue
 */
package meowtils.tenacitygui.tenacity.panel.settings;

import java.awt.Color;
import java.lang.reflect.Method;
import net.minecraft.client.renderer.GlStateManager;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import wtf.tatp.meowtils.gui.values.ButtonValue;

public class ButtonComponent
extends SettingComponent {
    private final ButtonValue setting;
    private final String presentation;
    private final Method previewRenderer;
    private final Method previewClick;
    private final Method previewTooltip;
    private final boolean ownPreviewLabels;
    private final float previewHeight;
    private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
    private final Animation clickAnimation = new DecelerateAnimation(300, 1.0, Direction.BACKWARDS);

    public ButtonComponent(ButtonValue setting) {
        this.setting = setting;
        try{
            Method kind=ExtensionControlSupport.method(setting,"getGuiPresentation");
            this.presentation=kind==null?"button":String.valueOf(kind.invoke(setting));
            this.previewRenderer=ExtensionControlSupport.method(setting,"renderGuiPreview",float.class,float.class,float.class);
            this.previewClick=ExtensionControlSupport.method(setting,"clickGuiPreview",float.class,float.class,int.class,float.class,float.class,float.class);
            this.previewTooltip=ExtensionControlSupport.method(setting,"renderGuiPreviewTooltip",float.class,float.class,float.class,float.class,float.class);
            Method labels=ExtensionControlSupport.method(setting,"drawsOwnPreviewLabels");
            this.ownPreviewLabels=labels!=null&&Boolean.TRUE.equals(labels.invoke(setting));
            Method size=ExtensionControlSupport.method(setting,"getGuiPreviewHeight");
            this.previewHeight=size==null?60:((Number)size.invoke(setting)).floatValue();
            if("preview".equals(this.presentation))this.countSize=this.previewHeight/16;
        }catch(ReflectiveOperationException e){throw new IllegalStateException("Could not create extension preview",e);}
    }

    @Override
    public String getName() {
        return this.setting.getName();
    }

    @Override
    public void initGui() {
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        if("note".equals(this.presentation)){
            this.countSize=1;
            drawFitted(this.setting.getName(),this.x+5,this.y+Fonts.tenacityFont14.getMiddleOfBox(16),this.width-10,ColorUtil.applyOpacity(this.textColor,.7f));
            return;
        }
        if("preview".equals(this.presentation)){
            this.countSize=this.previewHeight/16;
            RoundedUtil.drawRound(this.x+5,this.y+1,this.width-10,this.previewHeight-3,4,ColorUtil.applyOpacity(this.settingRectColor.brighter(),this.alpha));

            if(this.previewRenderer!=null){
                try{this.previewRenderer.invoke(this.setting,this.x,this.y,this.width);}
                catch(ReflectiveOperationException e){throw new IllegalStateException("Could not render extension preview",e);}
            }
            if(!this.ownPreviewLabels){
                Fonts.tenacityFont14.drawString("Live preview",this.x+10,this.y+5,this.textColor);
                Fonts.tenacityFont14.drawCenteredString("near",this.x+this.width/2-23,this.y+48,ColorUtil.applyOpacity(this.textColor,.7f));
                Fonts.tenacityFont14.drawCenteredString("far",this.x+this.width/2+23,this.y+48,ColorUtil.applyOpacity(this.textColor,.7f));
            }
            if(this.previewTooltip!=null){
                try{this.previewTooltip.invoke(this.setting,(float)mouseX,(float)mouseY,this.x,this.y,this.width);}
                catch(ReflectiveOperationException e){throw new IllegalStateException("Could not render extension preview hint",e);}
            }
            return;
        }
        float rectHeight;
        float rectY;
        String name = this.setting.getName();
        float textWidth = Fonts.tenacityFont16.getStringWidth(name);
        float rectWidth = Math.min(this.width - 10.0f, textWidth + 12.0f);
        float rectX = this.x + this.width / 2.0f - rectWidth / 2.0f;
        boolean hovering = HoveringUtil.isHovering(rectX, rectY = this.y + this.height / 2.0f - (rectHeight = (float)(Fonts.tenacityFont16.getHeight() + 6)) / 2.0f, rectWidth, rectHeight, mouseX, mouseY);
        this.hoverAnimation.setDirection(hovering ? Direction.FORWARDS : Direction.BACKWARDS);
        if (this.clickAnimation.finished(Direction.FORWARDS)) {
            this.clickAnimation.setDirection(Direction.BACKWARDS);
        }
        Color base = ColorUtil.brighter(this.settingRectColor, 0.7f - 0.25f * this.hoverAnimation.getOutput().floatValue());
        Color accent = ColorUtil.applyOpacity((Color)this.clientColors.getSecond(), this.alpha);
        Color rectColor = ColorUtil.interpolateColorC(base, accent, 0.35f * this.hoverAnimation.getOutput().floatValue() + 0.65f * this.clickAnimation.getOutput().floatValue());
        RoundedUtil.drawRound(rectX, rectY, rectWidth, rectHeight, 4.0f, rectColor);
        Fonts.tenacityFont16.drawCenteredString(name, this.x + this.width / 2.0f, this.y + Fonts.tenacityFont16.getMiddleOfBox(this.height), this.textColor);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if("preview".equals(this.presentation)&&this.previewClick!=null&&this.isClickable(mouseY)){
            try{this.previewClick.invoke(this.setting,(float)mouseX,(float)mouseY,button,this.x,this.y,this.width);}
            catch(ReflectiveOperationException e){throw new IllegalStateException("Could not select preview color",e);}
            return;
        }
        if(!"button".equals(this.presentation))return;
        float textWidth = Fonts.tenacityFont16.getStringWidth(this.setting.getName());
        float rectWidth = Math.min(this.width - 10.0f, textWidth + 12.0f);
        float rectHeight = Fonts.tenacityFont16.getHeight() + 6;
        float rectX = this.x + this.width / 2.0f - rectWidth / 2.0f;
        float rectY = this.y + this.height / 2.0f - rectHeight / 2.0f;
        if (this.isClickable(rectY + rectHeight) && HoveringUtil.isHovering(rectX, rectY, rectWidth, rectHeight, mouseX, mouseY) && button == 0) {
            this.clickAnimation.setDirection(Direction.FORWARDS);
            this.setting.click();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
    }

    private void drawFitted(String text,float left,float top,float available,Color color){
        float scale=Math.min(1,available/Math.max(1,Fonts.tenacityFont14.getStringWidth(text)));
        GlStateManager.func_179094_E();
        try{GlStateManager.func_179109_b(left,top,0);GlStateManager.func_179152_a(scale,scale,1);Fonts.tenacityFont14.drawString(text,0,0,color);}
        finally{GlStateManager.func_179121_F();}
    }
}

