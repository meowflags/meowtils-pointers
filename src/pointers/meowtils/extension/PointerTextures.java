package meowtils.extension;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.EnumMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** Lazily derive vivid tones from the original shape, preserving every alpha value. */
final class PointerTextures {
    private final Map<PointerColor,ResourceLocation> textures=new EnumMap<>(PointerColor.class);
    private final Map<Integer,ResourceLocation> distanceTextures=new java.util.HashMap<>();
    private BufferedImage source;private boolean failed;
    private void loadSource()throws Exception{
        if(source!=null)return;
        try(InputStream input=PointersModule.class.getResourceAsStream("/meowtils/extension/assets/crystal_cursor.png")){
            if(input==null)throw new IllegalStateException("Missing pointer artwork");source=ImageIO.read(input);if(source==null)throw new IllegalStateException("Invalid pointer artwork");
        }
    }
    ResourceLocation get(Minecraft mc,PointerColor color){
        ResourceLocation found=textures.get(color);if(found!=null)return found;if(failed)return null;
        try{
            loadSource();
            DynamicTexture texture=new DynamicTexture(recolor(source,color));
            found=mc.func_110434_K().func_110578_a("meowtils_pointer_"+color.label.toLowerCase(java.util.Locale.ROOT),texture);
            mc.func_110434_K().func_110577_a(found);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
            textures.put(color,found);return found;
        }catch(Exception e){failed=true;e.printStackTrace();return null;}
    }
    ResourceLocation get(Minecraft mc,int rgb){
        for(PointerColor color:PointerColor.values())if(color.rgb==rgb)return get(mc,color);
        if(rgb!=PointerTone.ORANGE&&rgb!=PointerTone.DISTANCE_RED&&rgb!=PointerTone.DISTANCE_GREEN)throw new IllegalArgumentException("Unexpected crystal anchor");
        ResourceLocation found=distanceTextures.get(rgb);if(found!=null)return found;if(failed)return null;
        try{
            loadSource();
            BufferedImage image=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
            for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)image.setRGB(x,y,recolorPixel(source.getRGB(x,y),rgb,false));
            DynamicTexture texture=new DynamicTexture(image);
            found=mc.func_110434_K().func_110578_a("meowtils_pointer_distance_"+Integer.toHexString(rgb),texture);
            mc.func_110434_K().func_110577_a(found);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MIN_FILTER,GL11.GL_LINEAR);GL11.glTexParameteri(GL11.GL_TEXTURE_2D,GL11.GL_TEXTURE_MAG_FILTER,GL11.GL_LINEAR);
            distanceTextures.put(rgb,found);return found;
        }catch(Exception e){failed=true;e.printStackTrace();return null;}
    }
    static BufferedImage recolor(BufferedImage source,PointerColor color){
        if(color==PointerColor.AQUA)return source;
        BufferedImage result=new BufferedImage(source.getWidth(),source.getHeight(),BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<source.getHeight();y++)for(int x=0;x<source.getWidth();x++)result.setRGB(x,y,recolorPixel(source.getRGB(x,y),color));
        return result;
    }
    static int recolorPixel(int pixel,PointerColor color){
        if(color==PointerColor.AQUA)return pixel;
        return recolorPixel(pixel,color.rgb,color==PointerColor.GRAY);
    }
    static int recolorPixel(int pixel,int rgb,boolean gray){
        int r=pixel>>16&255,g=pixel>>8&255,b=pixel&255;
        int neutral=Math.min(r,Math.min(g,b)),peak=Math.max(r,Math.max(g,b)),chroma=peak-neutral;
        // Keep pure white crystal highlights in colored variants, but stop pale edges
        // from diluting their hue. Gray needs a darker core as well as a gray halo.
        float base=gray?neutral*.28f:neutral*(chroma==0?1:.4f);
        float colored=peak-base;
        int red=Math.round(base+colored*(rgb>>16&255)/255f),green=Math.round(base+colored*(rgb>>8&255)/255f),blue=Math.round(base+colored*(rgb&255)/255f);
        return (pixel&0xFF000000)|(red<<16)|(green<<8)|blue;
    }
    void release(Minecraft mc){for(ResourceLocation texture:textures.values())mc.func_110434_K().func_147645_c(texture);for(ResourceLocation texture:distanceTextures.values())mc.func_110434_K().func_147645_c(texture);textures.clear();distanceTextures.clear();failed=false;}
    int loadedCount(){return textures.size()+distanceTextures.size();}
}
