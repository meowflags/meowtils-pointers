package meowtils.extension;

/** A bounded pair of crystal textures can represent any distance hue continuously. */
final class PointerTone {
    static final int ORANGE=0xFF821F;
    static final int DISTANCE_RED=0xFF202B,DISTANCE_GREEN=0x33CC5A;
    static final int[] DISTANCE_STOPS={DISTANCE_RED,ORANGE,PointerColor.YELLOW.rgb,DISTANCE_GREEN};
    private static final PointerTone[] FIXED;
    static {PointerColor[] colors=PointerColor.values();FIXED=new PointerTone[colors.length];for(PointerColor color:colors)FIXED[color.ordinal()]=new PointerTone(color,color.rgb,color.rgb,0);}
    final PointerColor palette;
    final int firstRgb,secondRgb,rgb;
    final float blend;
    private PointerTone(PointerColor palette,int first,int second,float blend){this.palette=palette;this.firstRgb=first;this.secondRgb=second;this.blend=blend;this.rgb=mix(first,second,blend);}
    static PointerTone fixed(PointerColor color){return FIXED[color.ordinal()];}
    static PointerTone distance(double distance,double redAt,double greenAt){
        return at(position(distance,redAt,greenAt));
    }
    static double position(double distance,double redAt,double greenAt){
        if(!Double.isFinite(redAt))redAt=6;
        if(!Double.isFinite(greenAt)||greenAt<=redAt)greenAt=redAt+1;
        if(Double.isNaN(distance))return 1;
        return Math.max(0,Math.min(1,(distance-redAt)/(greenAt-redAt)));
    }
    static PointerTone at(double position){
        double p=Double.isFinite(position)?Math.max(0,Math.min(1,position)):1;
        double segment=p*(DISTANCE_STOPS.length-1);int first=(int)segment;
        int second=Math.min(DISTANCE_STOPS.length-1,first+1);
        return new PointerTone(null,DISTANCE_STOPS[first],DISTANCE_STOPS[second],(float)(segment-first));
    }
    static int mix(int a,int b,float amount){
        int r=Math.round((a>>16&255)*(1-amount)+(b>>16&255)*amount);
        int g=Math.round((a>>8&255)*(1-amount)+(b>>8&255)*amount);
        int blue=Math.round((a&255)*(1-amount)+(b&255)*amount);
        return r<<16|g<<8|blue;
    }
}
