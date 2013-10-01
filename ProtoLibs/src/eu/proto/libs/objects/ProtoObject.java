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
import eu.proto.libs.lua.LuaField;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import static org.luaj.vm2.lib.jse.JavaProtoMethod.forMethod;

/**
 *
 * @author tim
 */
public abstract class ProtoObject extends LuaTable implements Savable, Cloneable, CloneableSmartAsset{

    protected ProtoApp app;
    protected String name;
    
    private static final ConcurrentMap<String, Future< Class<? extends ProtoObject>>> instances = new ConcurrentHashMap<>();

    /**Do not use this constructor, instanciate this object by creating a 
     * (@see ProtoObjectFactory) and running (@see ProtoObjectFactory.newInstance).
     * 
     */
    ProtoObject() {}

    public static synchronized <T extends ProtoObject> T newInstance(String className,ProtoApp app) throws ClassNotFoundException{

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
        
        LuaTable mt = new LuaTable();
        
        Method[] methods = instance.getClass().getMethods();
        
        for (Method method : methods){
            if (method.isAnnotationPresent(LuaField.class)){ // If method is supposed to become a field in lua
                /*mt.set(INDEX, new TwoArgFunction(){
                    @Override
                    public LuaValue call(LuaValue table, LuaValue key) {
                        System.out.println(key);
                        return LuaValue.valueOf("OHAI");
                    }
                });*/
                /*mt.set(NEWINDEX, new ThreeArgFunction(){ 
                    @Override
                    public LuaValue call(LuaValue table, LuaValue key, LuaValue value) {
                        System.out.println(key + " | " + value);
                        return LuaValue.valueOf("OHAI");
                    }
                });*/
            }else if(method.isAnnotationPresent(eu.proto.libs.lua.LuaFunction.class)){ // If method will remain a method in lua
                System.out.println(method.getName());
                instance.set(valueOf(method.getName()), forMethod(method));
            }
        }
        
        //instance.set("size", "AOISHDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
        
        instance.setmetatable(mt);
        
        return instance;
    }

    public abstract void init();

    public abstract void setName(String name);

    public abstract String getName();

    public LuaValue getLuaValue(){
        
        return this;
    };

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
    
    private static class ArgCallable implements Callable{
        private Class<? extends ProtoObject> clazz;

        ArgCallable(String className) throws ClassNotFoundException{
            clazz = (Class<? extends ProtoObject>) Class.forName("eu.proto.libs.objects." + className);
        }
        
        @Override
        public Class<? extends ProtoObject> call() throws Exception {
            return clazz;
        }
    }
    
    /**
     *
     * @param <T>
     */
    protected class Setter<T extends ProtoObject> extends LuaFunction{
        private final T object;
        private final Method method;

        @Override
	public LuaValue call(LuaValue arg){
            System.out.println("ADIGIAUSDHOIASHDOIASHDOIASHD" + arg.checkint());
            LuaValue returned;
            try {
                returned = CoerceJavaToLua.coerce(method.invoke(object, CoerceLuaToJava.coerce(arg,float.class)));
            } catch (    IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                returned = LuaValue.NIL;
                Logger.getLogger(ProtoObject.class.getName()).log(Level.SEVERE, null, ex);
            }
            return returned;
        }
        
        @Override
	public Varargs invoke(Varargs args) {
            System.out.println("VarargsVarargsVarargsVarargsVarargsVarargs");
		switch(args.narg()) {
		case 0: return error("No function parameters");
		case 1: return call(args.arg1());
		case 2: return call(args.arg1());
		case 3: return call(args.arg1());
		default: return call(args.arg1());
		}
	}
	
	public Setter(T object,Method method) {
            this.object = object;
            this.method = method;
	}
        
    }
    
    /**
     *
     * @param <T>
     */
    protected class Getter<T extends ProtoObject> extends LuaFunction{
        private final T object;
        private final Method method;

        @Override
	public LuaValue call(){
            LuaValue returned;
            try {
                returned = CoerceJavaToLua.coerce(method.invoke(object));
            } catch (    IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                returned = LuaValue.NIL;
                Logger.getLogger(ProtoObject.class.getName()).log(Level.SEVERE, null, ex);
            }
            return returned;
        }
        
        @Override
	public Varargs invoke(Varargs args) {
            System.out.println("VarargsVarargsVarargsVarargsVarargsVarargs");
		switch(args.narg()) {
		case 0: return call();
		case 1: return call();
		case 2: return call();
		case 3: return call();
		default: return call();
		}
	}
	
	public Getter(T object,Method method) {
            this.object = object;
            this.method = method;
	}
        
    }
}
