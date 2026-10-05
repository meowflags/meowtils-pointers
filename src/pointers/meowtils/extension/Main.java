/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  wtf.tatp.meowtils.extension.Extension
 *  wtf.tatp.meowtils.gui.Module
 */
package meowtils.extension;

import meowtils.extension.PointersModule;
import wtf.tatp.meowtils.extension.Extension;
import wtf.tatp.meowtils.gui.Module;

public class Main {
    public static void init() {
        PointersModule module=new PointersModule();
        Extension.registerModule(module);
        Extension.registerEvent(new PointersBootstrap(module));
    }
}

