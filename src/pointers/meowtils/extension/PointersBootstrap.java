package meowtils.extension;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;

/** Apply first-run enabled state only after Meowtils finishes loading the user's configuration. */
public final class PointersBootstrap {
    private final PointersModule module;private boolean ready;
    public PointersBootstrap(PointersModule module){this.module=module;}
    @EventTarget public void tick(ClientTickEvent event){
        if(!ready&&event.getPhase()==ClientTickEvent.Phase.POST){ready=true;module.setState(module.enabled);}
    }
}
