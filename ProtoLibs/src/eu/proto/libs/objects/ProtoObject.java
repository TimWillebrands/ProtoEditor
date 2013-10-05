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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import static org.luaj.vm2.LuaValue.INDEX;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import static org.luaj.vm2.lib.jse.JavaProtoMethod.forMethod;
import static org.luaj.vm2.lib.jse.JavaProtoMethod.forMethods;

/**
 *
 * @author tim
 */
public abstract class ProtoObject extends LuaTable implements Savable, Cloneable, CloneableSmartAsset {

    protected ProtoApp app;
    protected String name;
    //public final Map<String, LuaFunction> getters = Collections.synchronizedMap(new HashMap());
    //public final Map<String, LuaFunction> setters = Collections.synchronizedMap(new HashMap());
    public final Map<String, LuaFunction> getters = new ConcurrentHashMap<>();
    public final Map<String, LuaFunction> setters = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, Future< Class<? extends ProtoObject>>> instances = new ConcurrentHashMap<>();
    
    //private final Set<Method>

    /**
     * Do not use this constructor, instanciate this object by creating a
     * @link ProtoObjectFactory and running @link ProtoObjectFactory.newInstance.
     *
     */
    ProtoObject() {
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
        
        MakeMethodsAvailibleToLua(instance);

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
    
    private static void MakeMethodsAvailibleToLua(final ProtoObject instance){
        Method[] methods = instance.getClass().getMethods();
        Map<String, Method> normalMethods = new HashMap<>();
        Set<String> overloadedMethodsNames = new HashSet<>();
        List<Method[]> overloadedMethods = new ArrayList<>();
        List<Method> overloadedSeperateMethods = new ArrayList<>();

        for (Method method : methods) {
            if (method.isAnnotationPresent(LuaField.class) || method.isAnnotationPresent(eu.proto.libs.lua.LuaFunction.class)) {
                if (overloadedMethodsNames.contains(method.getName())) {
                    overloadedSeperateMethods.add(method);
                } else if (normalMethods.containsKey(method.getName())) {
                    overloadedSeperateMethods.add(normalMethods.remove(method.getName()));
                    overloadedSeperateMethods.add(method);
                    overloadedMethodsNames.add(method.getName());
                } else {
                    normalMethods.put(method.getName(), method);
                }
            }
        }

        Collection<Method> normals = normalMethods.values();

        for (Method method : normals) {
            if (method.isAnnotationPresent(eu.proto.libs.lua.LuaFunction.class)) {
                //System.out.println("Normal method: " + method.getName());
                instance.set(method.getName(), forMethod(method));
            } else {
                //System.out.println("Normal field: " + method.getName());
                System.out.println(method.getName().substring(3).toLowerCase());
                if(method.getName().substring(0, 3).equals("set")){
                    instance.setters.put(method.getName().substring(3).toLowerCase(), forMethod(method));
                }else{
                    instance.getters.put(method.getName().substring(3).toLowerCase(), forMethod(method));
                }
            }
        }


        for (String methodName : overloadedMethodsNames) {
            List<Method> sameMethods = new ArrayList<>();
            for (Method method : overloadedSeperateMethods) {
                if (method.getName().equals(methodName)) {
                    sameMethods.add(method);
                }
            }
            overloadedMethods.add(sameMethods.toArray(new Method[sameMethods.size()]));
        }


        for (Method[] method : overloadedMethods) {
            String methodName = method[0].getName();
            if (method[0].isAnnotationPresent(eu.proto.libs.lua.LuaFunction.class)) {
                //System.out.println("Overloadable method: " + method[0].getName());
                instance.set(methodName, forMethods(method));
            } else {
                //System.out.println("Overloadable field: " + method[0].getName());
                if(methodName.substring(0, 3).equals("set")){
                    instance.setters.put(methodName.substring(3).toLowerCase(), forMethods(method));
                }else{
                    instance.getters.put(methodName.substring(3).toLowerCase(), forMethods(method));
                }
            }
        }
        
        LuaTable metatable = new LuaTable();
        metatable.set(INDEX, new TwoArgFunction(){
            @Override
            public LuaValue call(LuaValue _, LuaValue key) {
                return instance.getters.get(key.checkjstring().toLowerCase()).call(instance);
            }
        });
        metatable.set(NEWINDEX, new ThreeArgFunction(){
            @Override
            public LuaValue call(LuaValue _, LuaValue key, LuaValue value) {
                LuaFunction m = instance.setters.get(key.checkjstring().toLowerCase());
                return m.call(instance,value);
            }
        });
        
        instance.setmetatable(metatable);
    }
    
}
