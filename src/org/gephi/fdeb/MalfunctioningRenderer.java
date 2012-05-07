/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.fdeb;

import org.gephi.preview.api.*;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */


//funny, I can't delete this file that I made for debug :) Getting Lookup runtime: 

/*
May 7, 2012 3:51:18 AM org.openide.util.lookup.MetaInfServicesLookup search
WARNING: null
java.lang.ClassNotFoundException: org.gephi.fdeb.MalfunctioningRenderer@sun.misc.Launcher$AppClassLoader@35a16869:file:/home/megaterik/programming/gsoc/fdeb/build/classes/ not a subclass of org.gephi.preview.spi.Renderer@sun.misc.Launcher$AppClassLoader@35a16869:file:/home/megaterik/programming/gsoc/fdeb/toolkit/gephi-toolkit.jar
	at org.openide.util.lookup.MetaInfServicesLookup.search(MetaInfServicesLookup.java:338)
	at org.openide.util.lookup.MetaInfServicesLookup.beforeLookup(MetaInfServicesLookup.java:155)
	at org.openide.util.lookup.MetaInfServicesLookup.beforeLookupResult(MetaInfServicesLookup.java:134)
	at org.openide.util.lookup.AbstractLookup.lookup(AbstractLookup.java:482)
	at org.openide.util.lookup.ProxyLookup$R.initResults(ProxyLookup.java:382)
	at org.openide.util.lookup.ProxyLookup$R.myBeforeLookup(ProxyLookup.java:674)
	at org.openide.util.lookup.ProxyLookup$R.computeResult(ProxyLookup.java:526)
	at org.openide.util.lookup.ProxyLookup$R.allInstances(ProxyLookup.java:497)
	at org.openide.util.Lookup.lookupAll(Lookup.java:263)
	at org.gephi.preview.PreviewControllerImpl.getRegisteredRenderers(PreviewControllerImpl.java:346)
	at org.gephi.preview.PreviewModelImpl.initManagedRenderers(PreviewModelImpl.java:120)
	at org.gephi.preview.PreviewModelImpl.<init>(PreviewModelImpl.java:98)
	at org.gephi.preview.PreviewControllerImpl.<init>(PreviewControllerImpl.java:117)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
	at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:39)
	at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:27)
	at java.lang.reflect.Constructor.newInstance(Constructor.java:513)
	at java.lang.Class.newInstance0(Class.java:355)
	at java.lang.Class.newInstance(Class.java:308)
	at org.openide.util.lookup.implspi.SharedClassObjectBridge.newInstance(SharedClassObjectBridge.java:64)
	at org.openide.util.lookup.MetaInfServicesLookup$P.getInstance(MetaInfServicesLookup.java:507)
	at org.openide.util.lookup.AbstractLookup.lookup(AbstractLookup.java:421)
	at org.openide.util.lookup.ProxyLookup.lookup(ProxyLookup.java:214)
	at org.gephi.fdeb.PrototypeRun.main(PrototypeRun.java:69)

BUILD SUCCESSFUL (total time: 1 second)
*/
@ServiceProvider(service = Renderer.class)
public class MalfunctioningRenderer implements Renderer{

    @Override
    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void preProcess(PreviewModel previewModel) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public PreviewProperty[] getProperties() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
