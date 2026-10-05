package meowtils.extension;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** A color strategy owns only color selection, independently of filtering and drawing. */
final class PointerColorPolicy {
    static final String FIXED="Fixed color",TEAM="Team colors",DISTANCE="Distance colors";
    interface Strategy {PointerTone choose(Context context);}
    static final class Context {
        final PointerColor fixed,team;final double distance,redAt,greenAt;
        Context(PointerColor fixed,PointerColor team,double distance,double redAt,double greenAt){this.fixed=fixed;this.team=team;this.distance=distance;this.redAt=redAt;this.greenAt=greenAt;}
    }
    private static final Map<String,Strategy> STRATEGIES;
    static {
        Map<String,Strategy> strategies=new LinkedHashMap<>();
        strategies.put(FIXED,context->PointerTone.fixed(context.fixed));
        strategies.put(TEAM,context->PointerTone.fixed(context.team==null?context.fixed:context.team));
        strategies.put(DISTANCE,context->PointerTone.distance(context.distance,context.redAt,context.greenAt));
        STRATEGIES=Collections.unmodifiableMap(strategies);
    }
    static PointerColor choose(String mode,PointerColor fixed,PointerColor team,double distance){
        return chooseTone(mode,fixed,team,distance,6,40).palette;
    }
    static PointerTone chooseTone(String mode,PointerColor fixed,PointerColor team,double distance,double redAt,double greenAt){
        Strategy strategy=STRATEGIES.get(mode);return (strategy==null?STRATEGIES.get(FIXED):strategy).choose(new Context(fixed,team,distance,redAt,greenAt));
    }
    static boolean valid(String mode){return STRATEGIES.containsKey(mode);}
}
