package meowtils.extension;

/** Reversing a fade starts at the displayed value, keeping rapid clicks continuous. */
final class HeartTransition {
    static final long DURATION=200000000L;
    private double from,target;private long started;
    HeartTransition(boolean selected){from=target=selected?1:0;}
    double value(long now){
        double t=Math.max(0,Math.min(1,(now-started)/(double)DURATION));
        double eased=t*t*t*(t*(t*6-15)+10);
        return from+(target-from)*eased;
    }
    void select(boolean selected,long now){
        double next=selected?1:0;if(next==target)return;
        from=value(now);target=next;started=now;
    }
    float sample(boolean selected,long now){select(selected,now);return (float)value(now);}
}
