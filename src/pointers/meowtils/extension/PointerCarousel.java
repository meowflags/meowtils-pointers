package meowtils.extension;

/** Continuous palette position avoids jumps even when the arrows are clicked repeatedly. */
final class PointerCarousel {
    static final long DURATION=240000000L;
    private double from,target;private long started;
    PointerCarousel(PointerColor initial){from=target=initial.ordinal();}
    double position(long now){double t=Math.min(1,Math.max(0,(now-started)/(double)DURATION));t=t*t*(3-2*t);return from+(target-from)*t;}
    void sync(PointerColor color,long now){
        if(PointerColor.at((int)Math.round(target))==color)return;
        int delta=Math.floorMod(color.ordinal()-(int)Math.round(target),PointerColor.values().length);
        if(delta>PointerColor.values().length/2)delta-=PointerColor.values().length;
        move(delta,now);
    }
    PointerColor move(int delta,long now){from=position(now);target+=delta;started=now;return PointerColor.at((int)Math.round(target));}
}
