package meowtils.extension;

/** Pointer tones are independent from the original palette dots. */
enum PointerColor {
    RED("Red",'c',0xFF3655,0xFF677A), BLUE("Blue",'9',0x4080FF,0x6F9DFF),
    GREEN("Green",'a',0x2EF078,0x6CE89E), YELLOW("Yellow",'e',0xFFCB24,0xFFDE78),
    AQUA("Aqua",'b',0x26E0F5,0x26E0F5), WHITE("White",'f',0xF8FAFF,0xECF2FF),
    PINK("Pink",'d',0xFF49CE,0xF28CD5), GRAY("Gray",'7',0x747D8C,0xA0ABBE);
    final String label;final char code;final int rgb;final int dotRgb;
    PointerColor(String label,char code,int rgb,int dotRgb){this.label=label;this.code=code;this.rgb=rgb;this.dotRgb=dotRgb;}
    float red(){return (rgb>>16&255)/255f;}float green(){return (rgb>>8&255)/255f;}float blue(){return (rgb&255)/255f;}
    static PointerColor named(String name){for(PointerColor color:values())if(color.label.equalsIgnoreCase(name))return color;return AQUA;}
    static PointerColor team(String code){if(code==null||code.length()!=1)return null;char c=Character.toLowerCase(code.charAt(0));if(c=='8')c='7';for(PointerColor color:values())if(color.code==c)return color;return null;}
    static PointerColor at(int index){PointerColor[] all=values();return all[Math.floorMod(index,all.length)];}
}
