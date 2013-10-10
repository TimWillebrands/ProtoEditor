/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs.objects;

import com.jme3.asset.AssetKey;
import com.jme3.asset.CloneableSmartAsset;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.Savable;
import eu.proto.libs.ProtoApp;
import eu.proto.libs.lua.ProtoUserdata;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.luaj.vm2.LuaValue;

/**
 *
 * @author tim
 */
public abstract class ProtoObject extends ProtoUserdata implements Savable, Cloneable, CloneableSmartAsset {

    protected ProtoApp app;
    protected String name;
    private static final ConcurrentMap<String, Future< Class<? extends ProtoObject>>> instances = new ConcurrentHashMap<>();
    
    //private final Set<Method>

    /**
     * Do not use this constructor, instanciate this object by creating a
     * @link ProtoObjectFactory and running @link ProtoObjectFactory.newInstance.
     *
     */
    public ProtoObject() { //TODO, move MakeMethodsAvailibleToLua to class ProtoUserdata, and use that for all lua datatypes incl thisone
    }

    public static synchronized <T extends ProtoObject> T newInstance(String className, ProtoApp app) throws ClassNotFoundException {

        T instance;

        Future<Class<? extends ProtoObject>> f = instances.get(className); // Get class from synchronized map
        if (f == null) { // If this is is first time class is instanciated, callable with classname is created and added to map
            FutureTask<Class<? extends ProtoObject>> ft = new FutureTask<>(new ArgCallable(className));
            f = instances.putIfAbsent(className, ft);
            if (f == null) {
                f = ft;
                ft.run();
            }
        }
        try {
            instance = (T) f.get().newInstance();
            instance.app = app;
            instance.init();
        } catch (InstantiationException | IllegalAccessException | InterruptedException | ExecutionException ex) {
            instance = (T) LuaValue.NIL;
        }

        return instance;
    }

    public abstract void init();

    
    public abstract void setName(String name);

    public abstract String getName();

    public LuaValue getLuaValue() {

        return this;
    }

    ;

    @Override
    public abstract void write(JmeExporter ex) throws IOException;

    @Override
    public abstract void read(JmeImporter im) throws IOException;

    @Override
    public abstract void setKey(AssetKey key);

    @Override
    public abstract AssetKey getKey();

    @Override
    public abstract ProtoObject clone();

    public ProtoApp getApp() {
        return app;
    }

    public void setApp(ProtoApp app) {
        this.app = app;
    }

    private static class ArgCallable implements Callable {

        private Class<? extends ProtoObject> clazz;

        ArgCallable(String className) throws ClassNotFoundException {
            clazz = (Class<? extends ProtoObject>) Class.forName("eu.proto.libs.objects." + className);
        }

        @Override
        public Class<? extends ProtoObject> call() throws Exception {
            return clazz;
        }
    }
}
