/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.entity.EntityPlayerSP
 *  net.minecraft.client.gui.ScaledResolution
 *  net.minecraft.client.network.NetworkPlayerInfo
 *  net.minecraft.client.renderer.GlStateManager
 *  net.minecraft.client.renderer.Tessellator
 *  net.minecraft.client.renderer.WorldRenderer
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.client.renderer.vertex.DefaultVertexFormats
 *  net.minecraft.entity.Entity
 *  net.minecraft.entity.player.EntityPlayer
 *  net.minecraft.util.EnumChatFormatting
 *  net.minecraft.util.IChatComponent
 *  net.minecraft.util.ResourceLocation
 *  net.minecraft.util.Timer
 *  org.lwjgl.opengl.GL11
 *  wtf.tatp.meowtils.Meowtils
 *  wtf.tatp.meowtils.config.Config
 *  wtf.tatp.meowtils.event.ClientTickEvent
 *  wtf.tatp.meowtils.event.ClientTickEvent$Phase
 *  wtf.tatp.meowtils.event.RenderGameOverlayEvent
 *  wtf.tatp.meowtils.event.RenderTickEvent
 *  wtf.tatp.meowtils.event.RenderTickEvent$Phase
 *  wtf.tatp.meowtils.event.api.EventTarget
 *  wtf.tatp.meowtils.extension.Extension
 *  wtf.tatp.meowtils.font.FontRenderer
 *  wtf.tatp.meowtils.mixin.AccessorMinecraft
 *  wtf.tatp.meowtils.module.utility.Freelook
 *  wtf.tatp.meowtils.util.ScoreboardUtil
 */
package meowtils.extension;

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Timer;
import org.lwjgl.opengl.GL11;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.RenderGameOverlayEvent;
import wtf.tatp.meowtils.event.RenderTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.extension.Extension;
import wtf.tatp.meowtils.font.FontRenderer;
import wtf.tatp.meowtils.mixin.AccessorMinecraft;
import wtf.tatp.meowtils.module.utility.Freelook;
import wtf.tatp.meowtils.util.ScoreboardUtil;

public class PointersModule
extends Extension {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public double radius = 40.0;
    @Config
    public float maximumSize = 0.9f;
    @Config
    public float minimumSize = 0.6f;
    @Config
    public double minimumSizeDistance = 30.0;
    @Config
    public boolean showDistance = false;
    @Config
    public boolean showDistanceUnit = true;
    @Config
    public float distanceTextSize = PointersModule.defaultDistanceTextSize();
    @Config
    public boolean hideFarPointers = true;
    @Config
    public boolean proximityGlow = true;
    @Config
    public double glowDistance = 32.0;
    @Config
    public String glowEffect = "Continuous";
    @Config
    public boolean showBots = false;
    @Config
    public boolean ignoreTeammates = true;
    @Config
    public boolean hideInCrowds = true;
    @Config
    public double hideBeyondDistance = 40.0;
    @Config
    public String colorMode=PointerColorPolicy.FIXED;
    @Config
    public String fixedColor="Aqua";
    @Config public double distanceRedAt=6;
    @Config public double distanceGreenAt=40;
    @Config public String teammateColor="";
    private final PointerTextures colorTextures=new PointerTextures();
    private final PointerCarousel colorCarousel;
    private final HeartTransition heartTransition;
    private final HeartTransition previewBadgeTransition;
    private PointerTone previewTeammateTone;
    private double previewDistanceFrom=.5,previewDistanceTarget=.5;
    private long previewDistanceStarted;
    private int visiblePlayers;
    private int hiddenPlayers;
    private static final String[] TEAM_PREFIXES = new String[]{"\u00a7c", "\u00a79", "\u00a7a", "\u00a7e", "\u00a7b", "\u00a7f", "\u00a7d", "\u00a78"};
    private static final double VISIBILITY_HYSTERESIS = 0.2;
    private static final double GLOW_ENTRY_HYSTERESIS = 0.25;
    private static final int SHRINK_TIME_MS = 180;
    private static final long ONE_SHOT_GLOW_DURATION_NANOS = 800000000L;
    private static final String GLOW_EFFECT_CONTINUOUS = "Continuous";
    private static final String GLOW_EFFECT_PULSE_ONCE = "Pulse Once";
    private int status = -1;
    private String myName = "";
    private String myTeamPrefix = "";
    private long lastStatusCheck = 0L;
    private boolean renderedOverlayThisFrame;


    private FontRenderer distanceFont;
    private boolean distanceFontFailed;
    private final Map<UUID, PointerAnimation> pointerAnimations = new HashMap<UUID, PointerAnimation>();
    private Object animationWorld;

    public PointersModule() {
        super("Pointers", "beddefender");
        PointersDefaults.apply(this);
        this.colorCarousel=new PointerCarousel(PointerColor.named(fixedColor));
        this.tooltip("Points to nearby players.\n\u00a7dCreator: @futyimoso\u00a7r");
        this.addButton(new PointersControls.PreviewValue(this));
        this.expand("Pointer Colors",group->{
            group.addMode(new PointersControls.ColorMode(this));
            group.addSlider(new PointersControls.DistanceThreshold(this,true));
            group.addSlider(new PointersControls.DistanceThreshold(this,false));
        });
        this.expand("Pointer Appearance", group -> {
            group.slider("Crosshair distance",10,70,1,"px","radius",Double.TYPE);
            group.slider("Maximum Size",.5,1.3,.05,"x","maximumSize",Float.TYPE);
            group.slider("Minimum Size",.2,1,.05,"x","minimumSize",Float.TYPE);
            group.slider("Smallest size at",6,54,1,"m","minimumSizeDistance",Double.TYPE);
        });
        this.expand("Visibility & Players", group -> {
            group.toggle("Hide distant pointers","hideFarPointers");
            group.addSlider(new PointersControls.ConditionalSlider("Hide beyond",6,128,1,"m","hideBeyondDistance",this,Double.TYPE,()->hideFarPointers));
            group.addToggle(new PointersControls.ConditionalToggle("Hide Teammates","ignoreTeammates",this,()->!hasTeammateColor()));
            group.addToggle(new PointersControls.InvertedToggle("Hide NPCs","showBots",this));
            group.toggle("Hide in Lobby","hideInCrowds");
            group.addButton(new PointersControls.NoteValue("Lobby rule: over 16 players"));
        });
        this.expand("Distance Labels", group -> {
            group.toggle("Show distance","showDistance");
            group.addSlider(new PointersControls.ConditionalSlider("Text size",1.5,8,.1,"px","distanceTextSize",this,Float.TYPE,()->showDistance));
            group.addToggle(new PointersControls.ConditionalToggle("Show m","showDistanceUnit",this,()->showDistance));
        });
        this.expand("Proximity Glow", group -> {
            group.addToggle(new PointersControls.ConditionalToggle("Enable glow","proximityGlow",this,()->!PointerColorPolicy.DISTANCE.equals(colorMode)));
            group.addMode(new PointersControls.GlowMode(this));
            group.addSlider(new PointersControls.ConditionalSlider("Glow Starts At",4,60,1,"m","glowDistance",this,Double.TYPE,()->isGlowActive()));
        });
        this.addButton(new PointersControls.ActionValue("Reset to defaults",5.0f,()->{boolean state=enabled;int bind=key;PointersDefaults.apply(this);enabled=state;key=bind;normalizeSettings();wtf.tatp.meowtils.config.ConfigManager.save();}));
        applyColorRules();
        heartTransition=new HeartTransition(isPreviewHearted());
        previewBadgeTransition=new HeartTransition(hasTeammateColor());
        previewTeammateTone=teammateTone();
    }

    @Override public void expand(String name,java.util.function.Consumer<wtf.tatp.meowtils.gui.values.ExpandValue> builder){
        this.addExpand(new PointersControls.GroupValue(name,builder,this));
    }

    public void onEnable() {
        this.clearPointerAnimations();
        EntityPlayerSP entityPlayerSP = PointersModule.mc().field_71439_g;
        if (entityPlayerSP != null) {
            this.myName = entityPlayerSP.func_70005_c_();
        }
    }

    public void onDisable() {
        this.clearPointerAnimations();
        this.releaseCrystalTexture();
    }

    public void onReset() {
        this.status = -1;
        this.myTeamPrefix = "";
        this.myName = "";
        this.renderedOverlayThisFrame = false;
        this.clearPointerAnimations();
    }

    @EventTarget
    public void onClientTick(ClientTickEvent clientTickEvent) {
        long l;
        this.normalizeSettings();
        if (!this.enabled) {
            return;
        }
        Minecraft minecraft = PointersModule.mc();
        if (minecraft.field_71439_g == null || minecraft.field_71441_e == null) {
            return;
        }
        if (clientTickEvent.getPhase() != ClientTickEvent.Phase.POST) {
            return;
        }
        String string = minecraft.field_71439_g.func_70005_c_();
        if (string != null && !string.equals(this.myName)) {
            this.myName = string;
            this.myTeamPrefix = "";
        }
        if ((l = System.currentTimeMillis()) - this.lastStatusCheck <= 1000L) {
            return;
        }
        this.lastStatusCheck = l;
        int n = this.getBedwarsStatus();
        if (n != 3 && this.status == 3) {
            this.myTeamPrefix = "";
        }
        this.status = n;
        if (this.status == 3) {
            this.refreshTeams();
        }
    }

    @EventTarget
    public void onRenderTick(RenderTickEvent renderTickEvent) {
        if (renderTickEvent.getPhase() == RenderTickEvent.Phase.PRE) {
            this.renderedOverlayThisFrame = false;
        }
    }

    @EventTarget
    public void onRenderGameOverlay(RenderGameOverlayEvent renderGameOverlayEvent) {
        this.normalizeSettings();
        if (!this.enabled) {
            return;
        }
        Minecraft minecraft = PointersModule.mc();
        if (minecraft.field_71462_r != null) {
            return;
        }
        if (minecraft.field_71439_g == null || minecraft.field_71441_e == null) {
            return;
        }
        if (this.renderedOverlayThisFrame) {
            return;
        }
        this.renderedOverlayThisFrame = true;
        this.renderPointers(PointersModule.currentPartialTicks(minecraft));
    }

    private void renderPointers(float f) {
        double d;
        Object object;
        Minecraft minecraft = PointersModule.mc();
        EntityPlayerSP entityPlayerSP = minecraft.field_71439_g;
        long l = System.nanoTime();
        if (this.animationWorld != minecraft.field_71441_e) {
            this.pointerAnimations.clear();
            this.animationWorld = minecraft.field_71441_e;
        }
        if (this.isCrowdSuppressed(minecraft)) {
            this.visiblePlayers=0;this.hiddenPlayers=0;
            this.pointerAnimations.clear();
            return;
        }
        ScaledResolution scaledResolution = new ScaledResolution(minecraft);
        double d2 = (double)scaledResolution.func_78326_a() / 2.0;
        double d3 = (double)scaledResolution.func_78328_b() / 2.0;
        double d4 = Math.min(1.0, Math.max(0.0, (double)f));
        double d5 = PointersModule.interpolate(entityPlayerSP.field_70142_S, entityPlayerSP.field_70165_t, d4);
        double d6 = PointersModule.interpolate(entityPlayerSP.field_70137_T, entityPlayerSP.field_70163_u, d4);
        double d7 = PointersModule.interpolate(entityPlayerSP.field_70136_U, entityPlayerSP.field_70161_v, d4);
        float f2 = PointersModule.currentCameraYaw(minecraft, entityPlayerSP, (float)d4);
        visiblePlayers=0;hiddenPlayers=0;
        for(PointerAnimation animation:pointerAnimations.values())animation.seenThisFrame=false;
        for(EntityPlayer target:new ArrayList<EntityPlayer>(minecraft.field_71441_e.field_73010_i)){
            if(target==entityPlayerSP||target.field_70128_L||target.func_110143_aJ()<=0)continue;
            boolean teammate=status==3&&TeamDetection.isTeammate(minecraft,entityPlayerSP,target);
            if(ignoreTeammates&&!hasTeammateColor()&&teammate){hiddenPlayers++;continue;}
            if(!showBots){
                NetworkPlayerInfo info=minecraft.func_147114_u().func_175102_a(target.func_110124_au());
                String name=target.func_70005_c_();
                if(info==null||name!=null&&(name.indexOf(167)>=0||name.length()<=2)){hiddenPlayers++;continue;}
            }
            double tx=interpolate(target.field_70142_S,target.field_70165_t,d4);
            double ty=interpolate(target.field_70137_T,target.field_70163_u,d4);
            double tz=interpolate(target.field_70136_U,target.field_70161_v,d4);
            double dx=tx-d5,dy=ty-d6,dz=tz-d7,distance=Math.sqrt(dx*dx+dy*dy+dz*dz);
            PointerAnimation animation=updatePointerAnimation(target.func_110124_au(),distance,l);
            updateGlowEntry(animation,distance,l);
            animation.lastTargetX=tx;animation.lastTargetZ=tz;animation.lastDistance=distance;
            animation.lastBaseSize=calculateDistanceScale(maximumSize,minimumSize,distance,minimumSizeDistance);
            animation.lastColor=PointerColorPolicy.chooseTone(colorMode,PointerColor.named(fixedColor),PointerColorPolicy.TEAM.equals(colorMode)?PointerColor.team(TeamDetection.teamColor(minecraft,target)):null,distance,distanceRedAt,distanceGreenAt);
            animation.lastTeammate=teammate&&hasTeammateColor();
            if(animation.lastTeammate)animation.lastColor=teammateTone();
            animation.hasPosition=true;animation.seenThisFrame=true;animation.departing=false;
            if(animation.targetVisible)visiblePlayers++;else hiddenPlayers++;
        }
        Iterator<PointerAnimation> iterator=pointerAnimations.values().iterator();
        while(iterator.hasNext()){
            PointerAnimation animation=iterator.next();animation.visibility=evaluateVisibility(animation,l);
            if(!animation.seenThisFrame){
                animation.insideGlowRange=false;animation.glowEntryNanos=-1;
                if(!animation.hasPosition){iterator.remove();continue;}
                if(!animation.departing){animation.departing=true;transitionPointerVisibility(animation,false,l);}
            }
            animation.visibility=evaluateVisibility(animation,l);
            if(animation.departing&&animation.visibility<=.002f){iterator.remove();continue;}
            if(animation.visibility>.002f&&animation.hasPosition)
                renderPointerSnapshot(animation.lastTargetX-d5,animation.lastTargetZ-d7,animation.lastDistance,animation.lastBaseSize,animation.visibility,f2,d2,d3,l,!animation.departing&&animation.targetVisible,animation.glowEntryNanos,animation.lastColor,animation.lastTeammate);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void renderPointerSnapshot(double d, double d2, double d3, float f, float f2, float f3, double d4, double d5, long l, boolean bl, long l2,PointerTone color,boolean teammate) {
        float f4 = f * f2;
        float f5 = 0.0f;
        boolean bl2 = GLOW_EFFECT_PULSE_ONCE.equals(this.glowEffect);
        if (bl && this.isGlowActive()) {
            f5 = bl2 ? PointersModule.calculateOneShotGlowStrength(d3, this.glowDistance, l2 < 0L ? -1L : l - l2) : PointersModule.calculateGlowStrength(d3, this.glowDistance, l);
        }
        float f6 = bl2 ? 0.12f : 0.03f;
        float f7 = f4 * (1.0f + f6 * f5);
        double d6 = PointersModule.calculatePointerAngle(d, d2, f3);
        double d7 = 180.0 - d6 * 57.29577951308232;
        double d8 = d4 + this.radius * Math.sin(d6);
        double d9 = d5 + this.radius * Math.cos(d6);
        boolean bl3 = false;
        GlStateManager.func_179094_E();
        try {
            GlStateManager.func_179109_b((float)((float)d8), (float)((float)d9), (float)0.0f);
            GlStateManager.func_179114_b((float)((float)d7), (float)0.0f, (float)0.0f, (float)1.0f);
            GlStateManager.func_179152_a((float)f7, (float)f7, (float)1.0f);
            bl3 = this.drawCrystalCursor(f5,color,1);
        }
        finally {
            GlStateManager.func_179131_c((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            GlStateManager.func_179098_w();
            GlStateManager.func_179084_k();
            GlStateManager.func_179121_F();
        }
        if (bl3 && this.showDistance) {
            this.drawDistanceLabel(d3, (float)d8, (float)d9, f4, f2);
        }
        if(bl3&&teammate)drawTeammateBadge((float)d8,(float)d9,f7,(float)d7,f2);
    }

    /** The attachment rotates with the crystal; the heart is drawn in screen axes. */
    static void drawTeammateBadge(float x,float y,float scale,float rotation,float alpha){
        if(alpha<=.001f||scale<=.001f)return;
        double angle=Math.toRadians(rotation),cos=Math.cos(angle),sin=Math.sin(angle);
        float anchorX=x+(float)(7*cos-4.5*sin)*scale;
        float anchorY=y+(float)(7*sin+4.5*cos)*scale;
        PointersControls.heart(anchorX,anchorY,2.1f*scale,(Math.round(255*Math.max(0,Math.min(1,alpha)))<<24)|0xFF62B4,true);
    }

    static double calculatePointerAngle(double d, double d2, float f) {
        double d3 = Math.toRadians(f);
        double d4 = -d * Math.sin(d3) + d2 * Math.cos(d3);
        double d5 = -d * Math.cos(d3) - d2 * Math.sin(d3);
        return Math.atan2(d5, -d4);
    }

    static float calculateDistanceScale(float f, float f2, double d, double d2) {
        float f3 = Math.max(f, f2);
        float f4 = Math.min(f, f2);
        if (d2 <= 0.0) {
            return d <= 0.0 ? f3 : f4;
        }
        double d3 = Math.min(1.0, Math.max(0.0, d / d2));
        double d4 = PointersModule.smoothstep(d3);
        return (float)((double)f3 + ((double)f4 - (double)f3) * d4);
    }

    static double smoothstep(double d) {
        double d2 = Math.min(1.0, Math.max(0.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    static float calculateGlowStrength(double d, double d2, long l) {
        if (d2 <= 0.0 || d > d2) {
            return 0.0f;
        }
        double d3 = PointersModule.smoothstep(1.0 - Math.min(1.0, Math.max(0.0, d / d2)));
        double d4 = (double)(l % 650000000L) / 6.5E8 * 2.0 * Math.PI;
        double d5 = 0.5 + 0.5 * Math.sin(d4);
        double d6 = 0.05 + 0.95 * d5 + 0.1 * d3 * (1.0 - d5);
        return (float)Math.min(1.0, d6);
    }

    static float calculateOneShotGlowStrength(double d, double d2, long l) {
        if (d2 <= 0.0 || d > d2 || l < 0L || l >= 800000000L) {
            return 0.0f;
        }
        double d3 = (double)l / 8.0E8;
        double d4 = 1.0 - Math.abs(d3 * 2.0 - 1.0);
        return (float)PointersModule.smoothstep(d4);
    }

    static boolean calculateVisibilityTarget(boolean bl, boolean bl2, double d, double d2) {
        if (!bl) {
            return true;
        }
        if (bl2) {
            return d <= d2 + 0.2;
        }
        return d < d2 - 0.2;
    }

    static float calculateAnimatedVisibility(float f, float f2, long l, long l2) {
        if (l2 <= 0L || f == f2 || l >= l2) {
            return f2;
        }
        if (l <= 0L) {
            return f;
        }
        double d = PointersModule.smoothstep((double)l / (double)l2);
        return (float)((double)f + ((double)f2 - (double)f) * d);
    }

    static long fixedTransitionDurationNanos() {
        return 180000000L;
    }

    private static double interpolate(double d, double d2, double d3) {
        return d + (d2 - d) * d3;
    }

    private static float interpolateRotation(float f, float f2, float f3) {
        float f4;
        for (f4 = f2 - f; f4 < -180.0f; f4 += 360.0f) {
        }
        while (f4 >= 180.0f) {
            f4 -= 360.0f;
        }
        return f + f4 * f3;
    }

    private static float currentPartialTicks(Minecraft minecraft) {
        try {
            Timer timer = ((AccessorMinecraft)minecraft).getTimer();
            if (timer != null) {
                return Math.min(1.0f, Math.max(0.0f, timer.field_74281_c));
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return 1.0f;
    }

    private static float currentCameraYaw(Minecraft minecraft, EntityPlayerSP entityPlayerSP, float f) {
        if (Freelook.isActive()) {
            return Freelook.getYaw();
        }
        Entity entity = minecraft.func_175606_aa();
        if (entity == null) {
            entity = entityPlayerSP;
        }
        float f2 = PointersModule.interpolateRotation(entity.field_70126_B, entity.field_70177_z, f);
        if (minecraft.field_71474_y != null && minecraft.field_71474_y.field_74320_O == 2) {
            f2 += 180.0f;
        }
        return f2;
    }

    private void normalizeSettings() {
        if(!PointerColorPolicy.valid(colorMode))colorMode=PointerColorPolicy.FIXED;
        fixedColor=PointerColor.named(fixedColor).label;
        if(teammateTone()==null)teammateColor="";
        applyColorRules();
        distanceRedAt=Double.isFinite(distanceRedAt)?Math.max(1,Math.min(127,distanceRedAt)):6;
        distanceGreenAt=Double.isFinite(distanceGreenAt)?Math.max(distanceRedAt+1,Math.min(128,distanceGreenAt)):Math.max(40,distanceRedAt+1);
        this.radius = Math.min(70.0, Math.max(10.0, this.radius));
        this.maximumSize = Math.min(1.3f, Math.max(0.5f, this.maximumSize));
        this.minimumSize = Math.min(1.0f, Math.max(0.2f, this.minimumSize));
        this.minimumSizeDistance = Math.min(54.0, Math.max(6.0, this.minimumSizeDistance));
        this.distanceTextSize = PointersModule.normalizeDistanceTextSize(this.distanceTextSize);
        this.hideBeyondDistance = Math.min(128.0, Math.max(6.0, this.hideBeyondDistance));
        this.glowDistance = Math.min(60.0, Math.max(4.0, this.glowDistance));
        if (!GLOW_EFFECT_CONTINUOUS.equals(this.glowEffect) && !GLOW_EFFECT_PULSE_ONCE.equals(this.glowEffect)) {
            this.glowEffect = GLOW_EFFECT_CONTINUOUS;
        }
    }

    private PointerAnimation updatePointerAnimation(UUID uUID, double d, long l) {
        boolean bl;
        PointerAnimation pointerAnimation = this.pointerAnimations.get(uUID);
        if (pointerAnimation == null) {
            bl = !this.hideFarPointers || d <= this.hideBeyondDistance;
            pointerAnimation = new PointerAnimation(bl ? 1.0f : 0.0f, bl, l);
            this.pointerAnimations.put(uUID, pointerAnimation);
        }
        pointerAnimation.visibility = PointersModule.evaluateVisibility(pointerAnimation, l);
        bl = PointersModule.calculateVisibilityTarget(this.hideFarPointers, pointerAnimation.targetVisible, d, this.hideBeyondDistance);
        this.transitionPointerVisibility(pointerAnimation, bl, l);
        pointerAnimation.visibility = PointersModule.evaluateVisibility(pointerAnimation, l);
        return pointerAnimation;
    }

    private void updateGlowEntry(PointerAnimation pointerAnimation, double d, long l) {
        boolean bl = this.proximityGlow && GLOW_EFFECT_PULSE_ONCE.equals(this.glowEffect);
        boolean bl2 = pointerAnimation.insideGlowRange;
        boolean bl3 = PointersModule.nextGlowRangeState(bl2, bl, d, this.glowDistance);
        if (bl3 && !bl2) {
            pointerAnimation.glowEntryNanos = l;
        } else if (!bl3) {
            pointerAnimation.glowEntryNanos = -1L;
        }
        pointerAnimation.insideGlowRange = bl3;
    }

    static boolean nextGlowRangeState(boolean bl, boolean bl2, double d, double d2) {
        if (!bl2 || d2 <= 0.0) {
            return false;
        }
        if (d <= d2) {
            return true;
        }
        if (d > d2 + 0.25) {
            return false;
        }
        return bl;
    }

    private void transitionPointerVisibility(PointerAnimation pointerAnimation, boolean bl, long l) {
        if (bl == pointerAnimation.targetVisible) {
            return;
        }
        pointerAnimation.visibility = PointersModule.evaluateVisibility(pointerAnimation, l);
        pointerAnimation.fromVisibility = pointerAnimation.visibility;
        pointerAnimation.toVisibility = bl ? 1.0f : 0.0f;
        pointerAnimation.targetVisible = bl;
        pointerAnimation.startNanos = l;
        pointerAnimation.durationNanos = PointersModule.fixedTransitionDurationNanos();
    }

    private static float evaluateVisibility(PointerAnimation pointerAnimation, long l) {
        if (pointerAnimation.durationNanos <= 0L || pointerAnimation.fromVisibility == pointerAnimation.toVisibility) {
            return pointerAnimation.toVisibility;
        }
        long l2 = l - pointerAnimation.startNanos;
        if (l2 >= pointerAnimation.durationNanos) {
            pointerAnimation.fromVisibility = pointerAnimation.toVisibility;
            pointerAnimation.durationNanos = 0L;
            return pointerAnimation.toVisibility;
        }
        return PointersModule.calculateAnimatedVisibility(pointerAnimation.fromVisibility, pointerAnimation.toVisibility, l2, pointerAnimation.durationNanos);
    }

    private void clearPointerAnimations() {
        this.pointerAnimations.clear();
        this.animationWorld = null;
    }

    private boolean isCrowdSuppressed(Minecraft minecraft) {
        if (minecraft.func_147114_u() == null) {
            return false;
        }
        Collection<NetworkPlayerInfo> collection = minecraft.func_147114_u().func_175106_d();
        return PointersModule.shouldSuppressForPlayerCount(this.hideInCrowds, collection == null ? 0 : collection.size());
    }

    static boolean shouldSuppressForPlayerCount(boolean bl, int n) {
        return bl && n > 16;
    }

    private void drawDistanceLabel(double d, float f, float f2, float f3, float f4) {
        FontRenderer fontRenderer = this.getDistanceFont();
        if (fontRenderer == null || f4 <= 0.08f) {
            return;
        }
        String string = PointersModule.formatDistanceLabel(d, this.showDistanceUnit);
        float f5 = this.distanceTextSize * f4;
        float f6 = fontRenderer.getStringWidth(string, f5);
        float f7 = f - f6 * 0.5f;
        float f8 = f2 + 9.0f * f3 + 1.5f * f4;
        fontRenderer.drawScaledStringWithLightShadow(string, f7, f8, -1, f5);
    }

    static String formatDistanceLabel(double d, boolean bl) {
        return Integer.toString((int)Math.round(d)) + (bl ? "m" : "");
    }

    static float normalizeDistanceTextSize(float f) {
        return Math.min(8.0f, Math.max(1.5f, f));
    }

    static float defaultDistanceTextSize() {
        return 2.2f;
    }

    private FontRenderer getDistanceFont() {
        if (this.distanceFont != null || this.distanceFontFailed) {
            return this.distanceFont != null ? this.distanceFont : Meowtils.fontRenderer;
        }
        try (InputStream inputStream = PointersModule.class.getResourceAsStream("/meowtils/extension/assets/Nexa-Heavy.ttf");){
            if (inputStream == null) {
                throw new IllegalStateException("Missing Nexa-Heavy.ttf");
            }
            Font font = Font.createFont(0, inputStream).deriveFont(0, 30.0f);
            this.distanceFont = new FontRenderer(font);
        }
        catch (Exception exception) {
            this.distanceFontFailed = true;
            exception.printStackTrace();
        }
        return this.distanceFont != null ? this.distanceFont : Meowtils.fontRenderer;
    }

    private boolean drawCrystalCursor(float f) {
        return drawCrystalCursor(f,PointerColor.named(fixedColor),1);
    }

    private boolean drawCrystalCursor(float f,PointerColor color,float alpha) {
        ResourceLocation resourceLocation = this.colorTextures.get(mc(),color);
        if (resourceLocation == null) {
            return false;
        }
        GlStateManager.func_179098_w();
        GlStateManager.func_179147_l();
        PointersModule.mc().func_110434_K().func_110577_a(resourceLocation);
        GlStateManager.func_179120_a((int)770, (int)771, (int)1, (int)0);
        GlStateManager.func_179131_c(1,1,1,alpha);
        this.drawCrystalQuad(1.0f);
        // Additive glow must not brighten gray back into the white variant.
        if(color==PointerColor.GRAY)f*=.38f;
        if (f > 0.001f) {
            try {
                GlStateManager.func_179120_a((int)770, (int)1, (int)1, (int)771);
                setGlowTint(color,0x14D1FF,.58f*f*alpha);
                this.drawCrystalQuad(1.3f);
                setGlowTint(color,0x4DFFFF,.34f*f*alpha);
                this.drawCrystalQuad(1.14f);
                GlStateManager.func_179131_c(1,1,1,.3f*f*alpha);
                this.drawCrystalQuad(0.88f);
            }
            finally {
                GlStateManager.func_179120_a((int)770, (int)771, (int)1, (int)0);
                GlStateManager.func_179131_c((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
            }
        }
        return true;
    }

    private boolean drawCrystalCursor(float glow,PointerTone tone,float alpha){
        if(PointerColorPolicy.DISTANCE.equals(colorMode))glow=0;
        if(tone.palette!=null)return drawCrystalCursor(glow,tone.palette,alpha);
        ResourceLocation first=colorTextures.get(mc(),tone.firstRgb),second=tone.blend>0?colorTextures.get(mc(),tone.secondRgb):first;
        if(first==null||second==null)return false;
        GlStateManager.func_179098_w();GlStateManager.func_179147_l();
        try{
            drawDistanceQuad(1,tone,first,second,0xFFFFFF,alpha,false);
            if(glow>.001f){
                drawDistanceQuad(1.3f,tone,first,second,PointerTextures.recolorPixel(0x14D1FF,tone.rgb,false),.58f*glow*alpha,true);
                drawDistanceQuad(1.14f,tone,first,second,PointerTextures.recolorPixel(0x4DFFFF,tone.rgb,false),.34f*glow*alpha,true);
                drawDistanceQuad(.88f,tone,first,second,0xFFFFFF,.3f*glow*alpha,true);
            }
            return true;
        }finally{GlStateManager.func_179120_a(770,771,1,0);GlStateManager.func_179131_c(1,1,1,1);}
    }

    private void drawDistanceQuad(float scale,PointerTone tone,ResourceLocation first,ResourceLocation second,int tint,float alpha,boolean additive){
        float r=(tint>>16&255)/255f,g=(tint>>8&255)/255f,b=(tint&255)/255f;
        GlStateManager.func_179120_a(770,additive?1:771,1,additive?771:0);
        mc().func_110434_K().func_110577_a(first);
        float weight=1-tone.blend;
        // Scale RGB, not opacity: the first draw occludes the background once,
        // and the second adds its weighted hue with identical artwork/alpha.
        GlStateManager.func_179131_c(r*weight,g*weight,b*weight,alpha);drawCrystalQuad(scale);
        if(tone.blend>0){
            GlStateManager.func_179120_a(770,1,1,771);mc().func_110434_K().func_110577_a(second);
            GlStateManager.func_179131_c(r*tone.blend,g*tone.blend,b*tone.blend,alpha);drawCrystalQuad(scale);
        }
    }

    private static void setGlowTint(PointerColor color,int source,float alpha){
        if(color==PointerColor.AQUA){
            if(source==0x14D1FF)GlStateManager.func_179131_c(.08f,.82f,1,alpha);
            else GlStateManager.func_179131_c(.3f,1,1,alpha);
        }else{
            int rgb=PointerTextures.recolorPixel(source,color);
            GlStateManager.func_179131_c((rgb>>16&255)/255f,(rgb>>8&255)/255f,(rgb&255)/255f,alpha);
        }
    }

    private void drawCrystalQuad(float f) {
        float f2 = 0.212f;
        float f3 = 0.13f;
        float f4 = 0.788f;
        float f5 = 0.702f;
        float f6 = 8.0f * f;
        Tessellator tessellator = Tessellator.func_178181_a();
        WorldRenderer worldRenderer = tessellator.func_178180_c();
        worldRenderer.func_181668_a(7, DefaultVertexFormats.field_181707_g);
        worldRenderer.func_181662_b((double)(-f6), (double)f6, 0.0).func_181673_a((double)f2, (double)f5).func_181675_d();
        worldRenderer.func_181662_b((double)f6, (double)f6, 0.0).func_181673_a((double)f4, (double)f5).func_181675_d();
        worldRenderer.func_181662_b((double)f6, (double)(-f6), 0.0).func_181673_a((double)f4, (double)f3).func_181675_d();
        worldRenderer.func_181662_b((double)(-f6), (double)(-f6), 0.0).func_181673_a((double)f2, (double)f3).func_181675_d();
        tessellator.func_78381_a();
    }

    private void releaseCrystalTexture() {
        this.colorTextures.release(mc());

    }

    private String findTeamColorPrefix(String string) {
        if (string == null) {
            return "";
        }
        int n = string.length();
        int n2 = 0;
        while (n2 + 1 < n && string.charAt(n2) == '\u00a7') {
            String string2 = string.substring(n2, n2 + 2);
            for (String string3 : TEAM_PREFIXES) {
                if (!string2.equals(string3)) continue;
                return string2;
            }
            n2 += 2;
        }
        return "";
    }

    private void refreshTeams() {
        Minecraft minecraft = PointersModule.mc();
        if (this.status != 3 || minecraft.field_71439_g.field_71075_bZ.field_75101_c) {
            this.myTeamPrefix = "";
            return;
        }
        this.myTeamPrefix = "";
        Collection<NetworkPlayerInfo> collection = minecraft.func_147114_u().func_175106_d();
        for (NetworkPlayerInfo networkPlayerInfo : collection) {
            IChatComponent iChatComponent;
            if (networkPlayerInfo.func_178845_a() == null || !networkPlayerInfo.func_178845_a().getName().equals(this.myName) || (iChatComponent = networkPlayerInfo.func_178854_k()) == null) continue;
            String string = iChatComponent.func_150254_d();
            String string2 = this.findTeamColorPrefix(string);
            if (string2.length() > 0) {
                this.myTeamPrefix = string2;
            }
            return;
        }
    }

    private int getBedwarsStatus() {
        return TeamDetection.isBedwarsGame(ScoreboardUtil.getSidebarTitle(),ScoreboardUtil.getSidebarLines())?3:-1;
    }

    public void renderPreview(float x,float y){
        PointerColor selected=PointerColor.named(fixedColor);
        PointerTone teammate=teammateTone();
        if(teammate!=null)previewTeammateTone=teammate;
        float badge=previewBadgeTransition.sample(teammate!=null,System.nanoTime());
        PointerTone far=badge>.001f&&previewTeammateTone!=null?previewTeammateTone:previewTone(PointerColorPolicy.TEAM.equals(colorMode)?PointerColor.at(selected.ordinal()+1):selected,minimumSizeDistance);
        renderPreviewSamples(x,y,previewTone(selected,5),far,badge);
    }

    private PointerTone previewTone(PointerColor color,double distance){return PointerColorPolicy.DISTANCE.equals(colorMode)?PointerTone.distance(distance,distanceRedAt,distanceGreenAt):PointerTone.fixed(color);}

    private void renderPreviewSamples(float x,float y,PointerTone nearColor,PointerTone farColor,float badge){
        long now=System.nanoTime();
        float close=calculateDistanceScale(maximumSize,minimumSize,5,minimumSizeDistance);
        float far=calculateDistanceScale(maximumSize,minimumSize,minimumSizeDistance,minimumSizeDistance);
        float glow=isGlowActive()?(GLOW_EFFECT_PULSE_ONCE.equals(glowEffect)?calculateOneShotGlowStrength(5,glowDistance,now%1600000000L):calculateGlowStrength(5,glowDistance,now)):0;
        net.minecraft.client.gui.Gui.func_73734_a((int)x-4,(int)y,(int)x+5,(int)y+1,0xFF849BA6);
        net.minecraft.client.gui.Gui.func_73734_a((int)x,(int)y-4,(int)x+1,(int)y+5,0xFF849BA6);
        drawPreviewPointer(x-18,y,close,glow,nearColor,1);
        drawPreviewPointer(x+18,y,far,0,farColor,1);
        drawTeammateBadge(x+18,y,far,0,badge);
        if(showDistance){drawDistanceLabel(5,x-18,y,close,1);drawDistanceLabel(minimumSizeDistance,x+18,y,far,1);}
    }

    private void drawPreviewPointer(float x,float y,float scale,float glow,PointerColor color,float alpha){
        drawPreviewPointer(x,y,scale,glow,PointerTone.fixed(color),alpha);
    }

    private void drawPreviewPointer(float x,float y,float scale,float glow,PointerTone color,float alpha){
        GlStateManager.func_179094_E();
        try{GlStateManager.func_179109_b(x,y,0);GlStateManager.func_179152_a(scale,scale,1);drawCrystalCursor(glow,color,alpha);}
        finally{GlStateManager.func_179131_c(1,1,1,1);GlStateManager.func_179098_w();GlStateManager.func_179084_k();GlStateManager.func_179121_F();}
    }

    public void renderColorPreview(float left,float top,float width){
        renderColorPreview(left,top,width,false);
    }

    public void renderNativeColorPreview(float left,float top,float width){
        renderColorPreview(left,top,width,true);
    }

    private void renderColorPreview(float left,float top,float width,boolean compact){
        long now=System.nanoTime();PointerColor selected=PointerColor.named(fixedColor);colorCarousel.sync(selected,now);
        float heroY=top+(compact?20:24),heroScale=compact?1.3f:1.65f;
        PointersControls.animatedHeart(left+width-9,top+8.5f,3.3f,heartTransition.sample(isPreviewHearted(),now));
        double position=colorCarousel.position(now);int first=(int)Math.floor(position);float fraction=(float)(position-first);
        float glow=isGlowActive()?(GLOW_EFFECT_PULSE_ONCE.equals(glowEffect)?calculateOneShotGlowStrength(5,glowDistance,now%1600000000L):calculateGlowStrength(5,glowDistance,now)):0;
        if(PointerColorPolicy.DISTANCE.equals(colorMode)){
            double sample=previewDistancePosition(now),distance=distanceRedAt+sample*(distanceGreenAt-distanceRedAt);
            PointerTone tone=PointerTone.at(sample);
            glow=isGlowActive()?(GLOW_EFFECT_PULSE_ONCE.equals(glowEffect)?calculateOneShotGlowStrength(distance,glowDistance,now%1600000000L):calculateGlowStrength(distance,glowDistance,now)):0;
            drawPreviewPointer(left+width/2,heroY,heroScale,glow,tone,1);
            PointersControls.arrow(left+9,heroY,false,0);PointersControls.arrow(left+width-9,heroY,true,0);
            PointersControls.centerLabel(String.format(java.util.Locale.ROOT,"%.0f m",distance),left+width/2,top+(compact?39:47),width-12,0xFF000000|tone.rgb,5.5f);
            PointersControls.distanceTrack(left+width/2,top+(compact?45:53),38,sample);
            renderPreview(left+width/2,top+(compact?57:67));return;
        }
        if(fraction<.001f)drawPreviewPointer(left+width/2,heroY,heroScale,glow,PointerColor.at(first),1);
        else{
            drawPreviewPointer(left+width/2-fraction*8,heroY,heroScale,glow,PointerColor.at(first),1-fraction);
            drawPreviewPointer(left+width/2+(1-fraction)*8,heroY,heroScale,glow,PointerColor.at(first+1),fraction);
        }
        PointersControls.arrow(left+9,heroY,false,selected.rgb);
        PointersControls.arrow(left+width-9,heroY,true,selected.rgb);
        PointersControls.centerLabel(selected.label,left+width/2,top+(compact?39:47),width-12,0xFF000000|selected.dotRgb,5.5f);
        for(PointerColor color:PointerColor.values()){
            float dotX=left+width/2+(color.ordinal()-3.5f)*5;
            if(color.label.equals(teammateColor))PointersControls.circle(dotX,top+(compact?45:53),2.2f,0xFFFF62B4);
            PointersControls.circle(dotX,top+(compact?45:53),color==selected?1.65f:1.25f,(color==selected?0xFF000000:0x88000000)|color.dotRgb);
        }
        renderPreview(left+width/2,top+(compact?57:67));
    }

    public boolean clickColorPreview(float mouseX,float mouseY,int button,float left,float top,float width){
        return clickColorPreview(mouseX,mouseY,button,left,top,width,24);
    }

    public boolean clickNativeColorPreview(float mouseX,float mouseY,int button,float left,float top,float width){
        return clickColorPreview(mouseX,mouseY,button,left,top,width,20);
    }

    private boolean clickColorPreview(float mouseX,float mouseY,int button,float left,float top,float width,float heroY){
        if(button==0&&heartHovered(mouseX,mouseY,left,top,width,heroY)){togglePreviewHeart();return true;}
        if(button!=0||mouseY<top+heroY-6||mouseY>top+heroY+6)return false;
        int direction=Math.abs(mouseX-(left+9))<=6?-1:Math.abs(mouseX-(left+width-9))<=6?1:0;
        if(direction==0)return false;
        if(PointerColorPolicy.DISTANCE.equals(colorMode)){
            long now=System.nanoTime();previewDistanceFrom=previewDistancePosition(now);previewDistanceTarget=Math.max(0,Math.min(1,previewDistanceTarget+direction*.1));previewDistanceStarted=now;return true;
        }
        long now=System.nanoTime();colorCarousel.sync(PointerColor.named(fixedColor),now);fixedColor=colorCarousel.move(direction,now).label;
        wtf.tatp.meowtils.config.ConfigManager.save();return true;
    }

    private double previewDistancePosition(long now){
        double t=Math.max(0,Math.min(1,(now-previewDistanceStarted)/(double)PointerCarousel.DURATION));t=t*t*(3-2*t);
        return previewDistanceFrom+(previewDistanceTarget-previewDistanceFrom)*t;
    }

    boolean isGlowActive(){return proximityGlow&&!PointerColorPolicy.DISTANCE.equals(colorMode);}
    void applyColorRules(){if(PointerColorPolicy.DISTANCE.equals(colorMode))proximityGlow=false;if(hasTeammateColor())ignoreTeammates=false;}
    boolean hasTeammateColor(){return teammateTone()!=null;}
    PointerTone teammateTone(){
        if(teammateColor==null||teammateColor.isEmpty())return null;
        if(teammateColor.startsWith("gradient:"))try{double p=Double.parseDouble(teammateColor.substring(9));return Double.isFinite(p)&&p>=0&&p<=1?PointerTone.at(p):null;}catch(NumberFormatException invalid){return null;}
        for(PointerColor color:PointerColor.values())if(color.label.equals(teammateColor))return PointerTone.fixed(color);
        return null;
    }
    private String previewHeartChoice(){return PointerColorPolicy.DISTANCE.equals(colorMode)?"gradient:"+(Math.round(previewDistanceTarget*1000)/1000.0):PointerColor.named(fixedColor).label;}
    boolean isPreviewHearted(){return previewHeartChoice().equals(teammateColor);}
    void togglePreviewHeart(){
        PointerTone previous=teammateTone();
        teammateColor=isPreviewHearted()?"":previewHeartChoice();applyColorRules();
        long now=System.nanoTime();heartTransition.select(isPreviewHearted(),now);
        PointerTone selected=teammateTone();
        if(selected!=null)previewTeammateTone=selected;else if(previous!=null)previewTeammateTone=previous;
        previewBadgeTransition.select(selected!=null,now);
        wtf.tatp.meowtils.config.ConfigManager.save();
    }
    private static boolean heartHovered(float mx,float my,float left,float top,float width,float heroY){return Math.abs(mx-(left+width-9))<=5&&Math.abs(my-(top+8.5f))<=5;}
    public void renderPreviewTooltip(float mx,float my,float left,float top,float width){
        renderPreviewTooltip(mx,my,left,top,width,24);
    }
    public void renderNativePreviewTooltip(float mx,float my,float left,float top,float width){
        renderPreviewTooltip(mx,my,left,top,width,20);
    }
    private void renderPreviewTooltip(float mx,float my,float left,float top,float width,float heroY){
        if(!heartHovered(mx,my,left,top,width,heroY))return;
        boolean selected=isPreviewHearted();
        String title=selected?"Teammate color selected":"Use this color for teammates";
        String hint=selected?"Click again to clear":"Heart badge marks teammates";
        PointersControls.tooltip(title,hint,left+width,top+17);
    }

    private static Minecraft mc() {
        return Minecraft.func_71410_x();
    }

    private static final class PointerAnimation {
        private float fromVisibility;
        private float toVisibility;
        private float visibility;
        private float lastBaseSize;
        private PointerTone lastColor=PointerTone.fixed(PointerColor.AQUA);
        private boolean lastTeammate;
        private long startNanos;
        private long durationNanos;
        private double lastTargetX;
        private double lastTargetZ;
        private double lastDistance;
        private boolean targetVisible;
        private boolean hasPosition;
        private boolean seenThisFrame;
        private boolean departing;
        private boolean insideGlowRange;
        private long glowEntryNanos = -1L;

        private PointerAnimation(float f, boolean bl, long l) {
            this.fromVisibility = f;
            this.toVisibility = f;
            this.visibility = f;
            this.startNanos = l;
            this.durationNanos = 0L;
            this.targetVisible = bl;
        }
    }
}

