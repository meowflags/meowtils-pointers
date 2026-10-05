/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.input.Keyboard
 */
package meowtils.tenacitygui.tenacity.panel.settings;

import java.awt.Color;
import meowtils.tenacitygui.tenacity.anim.Animation;
import meowtils.tenacitygui.tenacity.anim.ContinualAnimation;
import meowtils.tenacitygui.tenacity.anim.Direction;
import meowtils.tenacitygui.tenacity.anim.impl.DecelerateAnimation;
import meowtils.tenacitygui.tenacity.font.Fonts;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.ColorUtil;
import meowtils.tenacitygui.tenacity.render.RoundedUtil;
import meowtils.tenacitygui.tenacity.util.HoveringUtil;
import meowtils.tenacitygui.tenacity.util.MathUtils;
import meowtils.tenacitygui.tenacity.util.Pair;
import org.lwjgl.input.Keyboard;

public class NumberComponent
extends SettingComponent {
    private final NumAccess access;
    private final Object sourceValue;
    private final Animation hoverAnimation = new DecelerateAnimation(250, 1.0, Direction.BACKWARDS);
    private final Pair<Animation, Animation> textAnimations = Pair.of(new DecelerateAnimation(250, 1.0), new DecelerateAnimation(250, 1.0, Direction.BACKWARDS));
    private boolean dragging;
    private final ContinualAnimation animationWidth = new ContinualAnimation();
    public float clickCountAdd = 0.0f;
    private boolean selected;

    public NumberComponent(NumAccess access) {
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
        if(!ExtensionControlSupport.active(this.sourceValue))return;
        if (this.selected) {
            Keyboard.enableRepeatEvents((boolean)true);
            double increment = this.access.increment();
            switch (keyCode) {
                case 203: {
                    this.access.set(this.access.get() - increment);
                    break;
                }
                case 205: {
                    this.access.set(this.access.get() + increment);
                }
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY) {
        boolean active=ExtensionControlSupport.active(this.sourceValue);
        if(!active){this.dragging=false;this.selected=false;}
        drawControl(mouseX,mouseY);
        if(!active)ExtensionControlSupport.shade(this);
    }

    private void drawControl(int mouseX,int mouseY){
        String value = String.valueOf(MathUtils.round(this.access.get(), 2));
        value = value.contains(".") ? value.replaceAll("0*$", "").replaceAll("\\.$", "") : value;
        value+=ExtensionControlSupport.suffix(this.sourceValue);
        float sliderX = this.x + 5.0f;
        float sliderWidth = this.width - 10.0f;
        float sliderY = this.y + 13.0f;
        float sliderHeight = 3.0f;
        this.textAnimations.getFirst().setDirection(this.dragging ? Direction.BACKWARDS : Direction.FORWARDS);
        this.textAnimations.getSecond().setDirection(this.selected && !this.dragging ? Direction.FORWARDS : Direction.BACKWARDS);
        boolean hovering = HoveringUtil.isHovering(sliderX, sliderY - 2.0f, sliderWidth, sliderHeight + 4.0f, mouseX, mouseY);
        this.hoverAnimation.setDirection(hovering || this.dragging ? Direction.FORWARDS : Direction.BACKWARDS);
        float firstTextAnim = this.textAnimations.getFirst().getOutput().floatValue();
        float funnyWidth = Fonts.tenacityFont16.getStringWidth(this.access.name()) - Fonts.tenacityFont16.getStringWidth(": " + value);
        Fonts.tenacityFont16.drawString(": \u00a7l" + value, sliderX + funnyWidth + Fonts.tenacityFont16.getStringWidth(": " + value) * firstTextAnim, this.y + 2.0f, ColorUtil.applyOpacity(this.textColor, firstTextAnim));
        String text = "You can use arrow keys";
        Fonts.tenacityFont14.drawCenteredString(text, this.x + this.width / 2.0f, sliderY + sliderHeight + 4.5f, ColorUtil.applyOpacity(-1, this.textAnimations.getSecond().getOutput().floatValue() * 0.25f));
        Fonts.tenacityFont16.drawString(this.access.name(), sliderX, this.y + 2.0f, this.textColor);
        RoundedUtil.drawRound(sliderX, sliderY, sliderWidth, sliderHeight, 1.5f, ColorUtil.brighter(this.settingRectColor, 0.7f - 0.2f * this.hoverAnimation.getOutput().floatValue()));
        double currentValue = this.access.get();
        if (this.dragging) {
            float percent = Math.min(1.0f, Math.max(0.0f, ((float)mouseX - sliderX) / sliderWidth));
            double newValue = MathUtils.interpolate(this.access.min(), this.access.max(), percent);
            this.access.set(newValue);
        }
        float widthPercentage = (float)((currentValue - this.access.min()) / (this.access.max() - this.access.min()));
        this.animationWidth.animate(sliderWidth * widthPercentage, 20);
        float animatedWidth = this.animationWidth.getOutput();
        RoundedUtil.drawRound(sliderX, sliderY, animatedWidth, sliderHeight, 1.5f, (Color)this.clientColors.getSecond());
        float size = 7.0f;
        RoundedUtil.drawRound(sliderX + animatedWidth - size / 2.0f, sliderY - (size / 4.0f + 0.5f), size, size, size / 2.0f - 0.5f, this.settingRectColor);
        size = 5.0f;
        RoundedUtil.drawRound(sliderX + animatedWidth - size / 2.0f, sliderY - size / 4.0f, size, size, size / 2.0f - 0.5f, this.textColor);
        float secondTextAnim = 1.0f - this.textAnimations.getFirst().getOutput().floatValue();
        float rectWidth = Fonts.tenacityFont14.getStringWidth("\u00a7l" + value) + 4.0f;
        float rectX = Math.max(this.x, 2.0f + (sliderX + animatedWidth - size / 2.0f) - rectWidth / 2.0f);
        float rectY = sliderY + sliderHeight + 4.0f;
        float rectHeight = Fonts.tenacityFont14.getHeight() + 2;
        RoundedUtil.drawRound(rectX, rectY, rectWidth, rectHeight, 3.0f, ColorUtil.applyOpacity(this.settingRectColor.brighter(), secondTextAnim));
        Fonts.tenacityFont14.drawString("\u00a7l" + value, rectX + 2.0f, rectY + Fonts.tenacityFont14.getMiddleOfBox(rectHeight), ColorUtil.applyOpacity(this.textColor, secondTextAnim));
        this.clickCountAdd = 0.3f * secondTextAnim + 0.3f * this.textAnimations.getSecond().getOutput().floatValue();
        this.countSize = (float)(1.5 + (double)this.clickCountAdd);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button) {
        if(!ExtensionControlSupport.active(this.sourceValue))return;
        float sliderX = this.x + 5.0f;
        float sliderWidth = this.width - 10.0f;
        float sliderY = this.y + this.height / 2.0f + 2.0f;
        float sliderHeight = 3.0f;
        if (!HoveringUtil.isHovering(this.x, this.y, this.width, this.height, mouseX, mouseY)) {
            this.selected = false;
        }
        if (this.isClickable(sliderY + sliderHeight) && HoveringUtil.isHovering(sliderX, sliderY - 2.0f, sliderWidth, sliderHeight + 4.0f, mouseX, mouseY) && button == 0) {
            this.selected = true;
            this.dragging = true;
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int state) {
        if (this.dragging) {
            this.dragging = false;
        }
    }

    public static interface NumAccess {
        public String name();

        public double get();

        public void set(double var1);

        public double min();

        public double max();

        public double increment();
    }
}

