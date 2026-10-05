package meowtils.tenacitygui.tenacity.panel.settings;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import meowtils.tenacitygui.tenacity.panel.SettingComponent;
import meowtils.tenacitygui.tenacity.render.RenderUtil;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;

/** Optional presentation hooks, shared without linking separate extension class loaders. */
final class ExtensionControlSupport {
    private ExtensionControlSupport(){}
    static Method method(Object value,String name,Class<?>... parameters){
        if(value==null)return null;
        try{Method method=value.getClass().getMethod(name,parameters);method.setAccessible(true);return method;}
        catch(NoSuchMethodException e){return null;}
    }
    static Object underlyingValue(Object access){
        for(Class<?> type=access.getClass();type!=null;type=type.getSuperclass()){
            for(Field field:type.getDeclaredFields()){
                if(Modifier.isStatic(field.getModifiers()))continue;
                if(!SliderValue.class.isAssignableFrom(field.getType())&&!ToggleValue.class.isAssignableFrom(field.getType()))continue;
                try{field.setAccessible(true);return field.get(access);}
                catch(ReflectiveOperationException e){throw new IllegalStateException("Could not inspect extension control",e);}
            }
        }
        return null;
    }
    static boolean active(Object value){
        Method method=method(value,"isControlActive");
        if(method==null)return true;
        try{return Boolean.TRUE.equals(method.invoke(value));}
        catch(ReflectiveOperationException e){throw new IllegalStateException("Could not read extension control state",e);}
    }
    static void shade(SettingComponent component){
        RenderUtil.drawRect2(component.x,component.y,component.width,16*component.countSize,new Color(0,0,0,Math.round(135*component.alpha)).getRGB());
    }
    static String suffix(Object value){
        if(!(value instanceof SliderValue))return "";
        String unit=((SliderValue)value).getValueType();
        return unit==null||unit.isEmpty()?"":" "+unit;
    }
}
