package meowtils.extension;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.gui.component.ModuleComponent;
import wtf.tatp.meowtils.gui.component.subcomponents.*;
import wtf.tatp.meowtils.gui.values.*;

final class PointersControls {
    private static final double[][] HEART_OUTLINE=heartPath();
    private static wtf.tatp.meowtils.font.FontRenderer tooltipFont;
    static final float TOOLTIP_TEXT_SIZE=4.5f;
    static void nativeBackground(ModuleComponent parent,int offset,int height){
        int x=parent.parent.getX(),y=parent.parent.getY()+offset+3;
        Gui.func_73734_a(x-1,y,x+82,y+height,0xEE000000);
    }
    static final class GroupValue extends ExpandValue {
        GroupValue(String name,java.util.function.Consumer<ExpandValue> builder,PointersModule module){super(name,builder,module);}
        @Override public ExpandComponent createComponent(final ModuleComponent parent,final int y){
            return new ExpandComponent(this,parent,y){int position=y;
                @Override public void setOff(int offset){position=offset;super.setOff(offset);}
                @Override public void render(){nativeBackground(parent,position,getHeight());super.render();}
            };
        }
    }
    static final class ActionValue extends ButtonValue {
        ActionValue(String name,float scale,Runnable action){super(name,scale,action);}
        @Override public ButtonComponent createComponent(final ModuleComponent parent,final int y){
            return new ButtonComponent(this,parent,y){int position=y;
                @Override public void setOff(int offset){position=offset;super.setOff(offset);}
                @Override public void render(){nativeBackground(parent,position,12);super.render();}
            };
        }
    }
    static void shade(ModuleComponent parent,int offset){
        Gui.func_73734_a(parent.parent.getX(),parent.parent.getY()+offset+4,parent.parent.getX()+82,parent.parent.getY()+offset+16,0x99000000);
    }
    static void label(String text,float x,float y,float width,int color){
        if(Meowtils.fontRenderer==null)return;
        float size=4.5f,w=Meowtils.fontRenderer.getStringWidth(text,size);
        if(w>width)size*=width/w;
        Meowtils.fontRenderer.drawStringWithShadow(text,x,y,color,size);
    }
    static void centerLabel(String text,float x,float y,float width,int color,float size){
        if(Meowtils.fontRenderer==null)return;
        float w=Meowtils.fontRenderer.getStringWidth(text,size);if(w>width){size*=width/w;w=width;}
        Meowtils.fontRenderer.drawStringWithShadow(text,x-w/2,y,color,size);
    }
    static void arrow(float x,float y,boolean right,int color){
        circle(x,y,4.8f,0xFF424850);
        centerLabel(right?">":"<",x,y+1.5f,8,0xFFE9F1F6,5);
    }
    // A feathered edge stays round at both GUIs' scales without a stepped bitmap.
    static void circle(float x,float y,float radius,int color){
        shape(x,y,radius,color,false,true);
    }
    static void heart(float x,float y,float radius,int color,boolean filled){
        shape(x,y,radius,color,true,filled);
    }
    static void animatedHeart(float x,float y,float radius,float progress){
        progress=Math.max(0,Math.min(1,progress));
        int outline=0xFF000000|PointerTone.mix(0xEFF3F8,0xFF62B4,progress);
        int fill=(Math.round(255*progress)<<24)|0xFF62B4;
        shape(x,y,radius,outline,true,true,fill);
    }
    private static void shape(float x,float y,float radius,int color,boolean heart,boolean filled){
        shape(x,y,radius,color,heart,filled,filled?color:0);
    }
    private static void shape(float x,float y,float radius,int color,boolean heart,boolean filled,int fillColor){
        boolean texture=GL11.glIsEnabled(GL11.GL_TEXTURE_2D),blend=GL11.glIsEnabled(GL11.GL_BLEND);
        boolean alpha=GL11.glIsEnabled(GL11.GL_ALPHA_TEST),cull=GL11.glIsEnabled(GL11.GL_CULL_FACE);
        int shade=GL11.glGetInteger(GL11.GL_SHADE_MODEL);
        int src=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB),dst=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB);
        int srcAlpha=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA),dstAlpha=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
        Tessellator tess=Tessellator.func_178181_a();WorldRenderer vertices=tess.func_178180_c();
        double inner=radius-.35;
        try{
            GlStateManager.func_179090_x();GlStateManager.func_179147_l();GlStateManager.func_179118_c();GlStateManager.func_179129_p();
            GlStateManager.func_179120_a(770,771,1,0);GL11.glShadeModel(GL11.GL_SMOOTH);
            if(heart){
                if((fillColor>>>24)>0)heartFill(tess,vertices,x,y,radius,fillColor);
                outlineBand(tess,vertices,x,y,radius,-.33,color,.33,color);
                outlineBand(tess,vertices,x,y,radius,-.48,color&0xFFFFFF,-.33,color);
                outlineBand(tess,vertices,x,y,radius,.33,color,.48,color&0xFFFFFF);
            }else if(filled){
                vertices.func_181668_a(GL11.GL_TRIANGLE_FAN,DefaultVertexFormats.field_181706_f);circleVertex(vertices,x,y,color);
                for(int i=0;i<=64;i++)shapeVertex(vertices,x,y,i*Math.PI/32,inner,color,heart);
                tess.func_78381_a();
            }else{
                shapeRing(tess,vertices,x,y,radius*.58,color,inner,color,heart);
                shapeRing(tess,vertices,x,y,radius*.58-.25,color&0xFFFFFF,radius*.58,color,heart);
            }
            if(!heart)shapeRing(tess,vertices,x,y,inner,color,radius,color&0xFFFFFF,false);
        }finally{
            GL11.glShadeModel(shade);GlStateManager.func_179120_a(src,dst,srcAlpha,dstAlpha);
            if(texture)GlStateManager.func_179098_w();else GlStateManager.func_179090_x();
            if(blend)GlStateManager.func_179147_l();else GlStateManager.func_179084_k();
            if(alpha)GlStateManager.func_179141_d();else GlStateManager.func_179118_c();
            if(cull)GlStateManager.func_179089_o();else GlStateManager.func_179129_p();
            GlStateManager.func_179131_c(1,1,1,1);
        }
    }
    private static void heartFill(Tessellator tess,WorldRenderer vertices,float x,float y,float radius,int color){
        vertices.func_181668_a(GL11.GL_TRIANGLE_FAN,DefaultVertexFormats.field_181706_f);circleVertex(vertices,x,y,color);
        for(int i=0;i<=HEART_OUTLINE.length;i++)heartVertex(vertices,x,y,radius,i,-.33,color);
        tess.func_78381_a();
    }
    private static void heartVertex(WorldRenderer vertices,float x,float y,float radius,int index,double offset,int color){
        index%=HEART_OUTLINE.length;
        double[] point=HEART_OUTLINE[index],previous=HEART_OUTLINE[(index+HEART_OUTLINE.length-1)%HEART_OUTLINE.length],next=HEART_OUTLINE[(index+1)%HEART_OUTLINE.length];
        double dx=next[0]-previous[0],dy=next[1]-previous[1],length=Math.hypot(dx,dy);
        circleVertex(vertices,x+point[0]*radius-dy/length*offset,y+point[1]*radius+dx/length*offset,color);
    }
    private static double[][] heartPath(){
        double[][] curves={{0,-.35,-.45,-1.1,-1.15,-.78,-.96,-.15},{-.96,-.15,-.83,.32,-.38,.65,0,1},{0,1,.38,.65,.83,.32,.96,-.15},{.96,-.15,1.15,-.78,.45,-1.1,0,-.35}};
        double[][] path=new double[96][2];int at=0;
        for(double[] curve:curves)for(int i=0;i<24;i++){
            double t=i/24.0,u=1-t;
            path[at][0]=u*u*u*curve[0]+3*u*u*t*curve[2]+3*u*t*t*curve[4]+t*t*t*curve[6];
            path[at++][1]=u*u*u*curve[1]+3*u*u*t*curve[3]+3*u*t*t*curve[5]+t*t*t*curve[7];
        }
        return path;
    }
    private static void outlineBand(Tessellator tess,WorldRenderer vertices,float x,float y,float radius,double inner,int a,double outer,int b){
        vertices.func_181668_a(GL11.GL_TRIANGLE_STRIP,DefaultVertexFormats.field_181706_f);
        for(int i=0;i<=HEART_OUTLINE.length;i++){
            heartVertex(vertices,x,y,radius,i,inner,a);
            heartVertex(vertices,x,y,radius,i,outer,b);
        }
        tess.func_78381_a();
    }
    private static void roundedRect(float x,float y,float width,float height,float radius,int color){
        Gui.func_73734_a((int)(x+radius),(int)y,(int)(x+width-radius),(int)(y+height),color);
        Gui.func_73734_a((int)x,(int)(y+radius),(int)(x+width),(int)(y+height-radius),color);
        circle(x+radius,y+radius,radius,color);circle(x+width-radius,y+radius,radius,color);
        circle(x+radius,y+height-radius,radius,color);circle(x+width-radius,y+height-radius,radius,color);
    }
    static void tooltip(String title,String hint,float right,float top){
        if(tooltipFont==null)tooltipFont=new wtf.tatp.meowtils.font.FontRenderer(new java.awt.Font("Arial",java.awt.Font.BOLD,32));
        float width=Math.max(76,(float)Math.ceil(Math.max(tooltipFont.getStringWidth(title,TOOLTIP_TEXT_SIZE),tooltipFont.getStringWidth(hint,TOOLTIP_TEXT_SIZE)))+10);
        float x=Math.round(right-width),y=Math.round(top);
        roundedRect(x-1,y-1,width+2,21,3,0xFF36414F);
        roundedRect(x,y,width,19,2.5f,0xFF141A22);
        boolean alpha=GL11.glIsEnabled(GL11.GL_ALPHA_TEST);
        try{
            GlStateManager.func_179118_c();
            tooltipFont.drawString(title,x+5,y+7,0xFFF8FAFD,TOOLTIP_TEXT_SIZE);
            tooltipFont.drawString(hint,x+5,y+15,0xFFD6DFEA,TOOLTIP_TEXT_SIZE);
        }finally{if(alpha)GlStateManager.func_179141_d();else GlStateManager.func_179118_c();}
    }
    private static void shapeRing(Tessellator tess,WorldRenderer vertices,float x,float y,double inner,int a,double outer,int b,boolean heart){
        vertices.func_181668_a(GL11.GL_TRIANGLE_STRIP,DefaultVertexFormats.field_181706_f);
        for(int i=0;i<=64;i++){double angle=i*Math.PI/32;shapeVertex(vertices,x,y,angle,inner,a,heart);shapeVertex(vertices,x,y,angle,outer,b,heart);}tess.func_78381_a();
    }
    private static void shapeVertex(WorldRenderer vertices,float x,float y,double angle,double radius,int color,boolean heart){
        double dx=Math.cos(angle),dy=Math.sin(angle);
        if(heart){double s=Math.sin(angle);dx=s*s*s;dy=-(13*Math.cos(angle)-5*Math.cos(2*angle)-2*Math.cos(3*angle)-Math.cos(4*angle)+2.5)/16;}
        circleVertex(vertices,x+dx*radius,y+dy*radius,color);
    }
    private static void circleVertex(WorldRenderer vertices,double x,double y,int color){
        vertices.func_181662_b(x,y,0).func_181669_b((color>>16)&255,(color>>8)&255,color&255,(color>>>24)&255).func_181675_d();
    }
    static class ConditionalSlider extends SliderValue {
        final BooleanSupplier active;
        public boolean isControlActive(){return active.getAsBoolean();}
        ConditionalSlider(String name,double min,double max,double step,String suffix,String field,PointersModule owner,Class<?> type,BooleanSupplier active){super(name,min,max,step,suffix,field,owner,type);this.active=active;}
        @Override public void set(double value){if(active.getAsBoolean())super.set(value);}
        @Override public SliderComponent createComponent(final ModuleComponent parent,final int y){
            return new SliderComponent(this,parent,y){int position=y;
                @Override public void setOff(int offset){position=offset;super.setOff(offset);}
                @Override public void render(){super.render();if(!active.getAsBoolean())shade(parent,position);}
                @Override public boolean mouseClicked(int x,int y,int button){return active.getAsBoolean()&&super.mouseClicked(x,y,button);}
            };
        }
    }
    static final class DistanceThreshold extends ConditionalSlider {
        private final PointersModule module;private final boolean red;
        DistanceThreshold(PointersModule module,boolean red){
            super(red?"Red at":"Green at",red?1:2,red?127:128,1,"m",red?"distanceRedAt":"distanceGreenAt",module,Double.TYPE,()->PointerColorPolicy.DISTANCE.equals(module.colorMode));
            this.module=module;this.red=red;
        }
        @Override public void set(double value){
            if(!isControlActive()||!Double.isFinite(value))return;
            value=Math.max(red?1:2,Math.min(red?127:128,Math.round(value)));
            if(red&&value>=module.distanceGreenAt)module.distanceGreenAt=value+1;
            if(!red&&value<=module.distanceRedAt)module.distanceRedAt=value-1;
            super.set(value);
        }
    }
    static final class ColorMode extends ModeValue {
        private final PointersModule module;
        ColorMode(PointersModule module){super("Color mode",Arrays.asList(PointerColorPolicy.FIXED,PointerColorPolicy.TEAM,PointerColorPolicy.DISTANCE),"colorMode",module);this.module=module;}
        @Override public void setValue(String value){if(PointerColorPolicy.DISTANCE.equals(value))module.proximityGlow=false;super.setValue(value);}
        @Override public void setValue(int index){setValue(getModes().get(index));}
    }
    static void distanceTrack(float x,float y,float width,double position){
        int start=(int)(x-width/2),end=(int)(x+width/2);
        for(int i=start;i<end;i++)Gui.func_73734_a(i,(int)y-1,i+1,(int)y+1,0xFF000000|PointerTone.at((i-start)/(double)Math.max(1,end-start-1)).rgb);
        circle(x-width/2+(float)position*width,y,2.1f,0xFF172027);
        circle(x-width/2+(float)position*width,y,1.3f,0xFFF2F6FF);
    }
    static final class ConditionalToggle extends ToggleValue {
        final BooleanSupplier active;
        public boolean isControlActive(){return active.getAsBoolean();}
        ConditionalToggle(String name,String field,PointersModule owner,BooleanSupplier active){super(name,field,owner);this.active=active;}
        @Override public void setState(boolean value){if(active.getAsBoolean())super.setState(value);}
        @Override public ToggleComponent createComponent(final ModuleComponent parent,final int y){
            return new ToggleComponent(this,parent,y){int position=y;
                @Override public void setOff(int offset){position=offset;super.setOff(offset);}
                @Override public void render(){super.render();if(!active.getAsBoolean())shade(parent,position);}
                @Override public boolean mouseClicked(int x,int y,int button){return active.getAsBoolean()&&super.mouseClicked(x,y,button);}
            };
        }
    }
    static final class InvertedToggle extends ToggleValue {
        final PointersModule module;
        InvertedToggle(String name,String field,PointersModule module){super(name,field,module);this.module=module;}
        @Override public boolean getState(){return !module.showBots;}
        @Override public void setState(boolean hide){if(hide!=getState()){module.showBots=!hide;ConfigManager.save();}}
    }
    static final class GlowMode extends ModeValue {
        final PointersModule module;
        public boolean isControlActive(){return module.isGlowActive();}
        GlowMode(PointersModule module){super("Glow style",Arrays.asList("Continuous","Pulse Once"),"glowEffect",module);this.module=module;}
        @Override public List<String> getModes(){return Arrays.asList("Repeating pulse","Pulse Once");}
        @Override public String getValue(){String stored=super.getValue();return "Continuous".equals(stored)?"Repeating pulse":stored;}
        @Override public void setValue(String value){if(module.isGlowActive())super.setValue("Repeating pulse".equals(value)?"Continuous":value);}
        @Override public void setValue(int index){setValue(getModes().get(index));}
        @Override public ModeComponent createComponent(final ModuleComponent parent,final int y){
            return new ModeComponent(this,parent,y,getName()){
                @Override public void render(){super.render();if(!module.isGlowActive())shade(parent,offset);}
                @Override public void updateComponent(int x,int y){if(!module.isGlowActive()){expanded=false;if(expandedPart==this)expandedPart=null;}super.updateComponent(x,y);}
                @Override public boolean mouseClicked(int x,int y,int button){return module.isGlowActive()&&super.mouseClicked(x,y,button);}
            };
        }
    }
    static class NoteValue extends ButtonValue {
        NoteValue(String name){super(name,4.5f,null);}
        public String getGuiPresentation(){return "note";}
        @Override public ButtonComponent createComponent(final ModuleComponent parent,final int y){
            return new ButtonComponent(this,parent,y){int position=y;
                @Override public void setOff(int offset){position=offset;}
                @Override public void render(){nativeBackground(parent,position,12);label(getName(),parent.parent.getX()+3,parent.parent.getY()+position+10,77,0xFF9FB4C0);}
                @Override public boolean mouseClicked(int x,int y,int button){return false;}
            };
        }
    }
    static final class PreviewValue extends ButtonValue {
        final PointersModule module;
        PreviewValue(PointersModule module){super("Live preview",4.5f,null);this.module=module;}
        public String getGuiPresentation(){return "preview";}
        public float getGuiPreviewHeight(){return 84;}
        public boolean drawsOwnPreviewLabels(){return true;}
        public void renderGuiPreview(float x,float y,float width){
            module.renderColorPreview(x+4,y,width-8);
        }
        public boolean clickGuiPreview(float mouseX,float mouseY,int button,float x,float y,float width){return module.clickColorPreview(mouseX,mouseY,button,x+4,y,width-8);}
        public void renderGuiPreviewTooltip(float mouseX,float mouseY,float x,float y,float width){module.renderPreviewTooltip(mouseX,mouseY,x+4,y,width-8);}
        @Override public ButtonComponent createComponent(final ModuleComponent parent,final int y){
            return new ButtonComponent(this,parent,y){int position=y,mouseX=-1000,mouseY=-1000;
                @Override public int getHeight(){return 76;}
                @Override public void setOff(int offset){position=offset;}
                @Override public void render(){
                    int x=parent.parent.getX(),top=parent.parent.getY()+position;
                    nativeBackground(parent,position,76);
                    module.renderNativeColorPreview(x+3,top+3,75);
                    module.renderNativePreviewTooltip(mouseX,mouseY,x+3,top+3,75);
                }
                @Override public void updateComponent(int x,int y){mouseX=x;mouseY=y;super.updateComponent(x,y);}
                @Override public boolean mouseClicked(int x,int y,int button){return parent.open&&parent.parent.open&&module.clickNativeColorPreview(x,y,button,parent.parent.getX()+3,parent.parent.getY()+position+3,75);}
            };
        }
    }
}
