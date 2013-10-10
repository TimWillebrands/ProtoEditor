/**
 * *****************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
 * 
* Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
* The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * ****************************************************************************
 */
package eu.proto.libs.lua;

import eu.proto.libs.objects.ProtoObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import static org.luaj.vm2.LuaValue.INDEX;
import static org.luaj.vm2.LuaValue.NEWINDEX;
import org.luaj.vm2.lib.ThreeArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.JavaProtoMethod;
import static org.luaj.vm2.lib.jse.JavaProtoMethod.forMethod;
import static org.luaj.vm2.lib.jse.JavaProtoMethod.forMethods;

public abstract class ProtoUserdata extends LuaTable {

    private static final Map<Class, Map<String, org.luaj.vm2.LuaFunction>> classGetters = new ConcurrentHashMap<>();
    private static final Map<Class, Map<String, org.luaj.vm2.LuaFunction>> classSetters = new ConcurrentHashMap<>();
    private static final Map<Class, Map<String, org.luaj.vm2.LuaFunction>> classMethods = new ConcurrentHashMap<>();
    
    protected Map<String, org.luaj.vm2.LuaFunction> getters;
    protected Map<String, org.luaj.vm2.LuaFunction> setters;
    protected Map<String, org.luaj.vm2.LuaFunction> methods;

    public ProtoUserdata() {
        MakeMethodsAvailibleToLua(this);
    }

    @Override
    public String tojstring() {
        return String.valueOf(this);
    }

    @Override
    public int type() {
        return TUSERDATA;
    }

    @Override
    public String typename() {
        return "userdata";
    }

    public Object userdata() {
        return this;
    }

    @Override
    public boolean isuserdata() {
        return true;
    }

    @Override
    public boolean isuserdata(Class c) {
        return c.isAssignableFrom(this.getClass());
    }

    @Override
    public Object touserdata() {
        return this;
    }

    @Override
    public Object touserdata(Class c) {
        return c.isAssignableFrom(this.getClass()) ? this : null;
    }

    @Override
    public Object optuserdata(Object defval) {
        return this;
    }

    @Override
    public Object optuserdata(Class c, Object defval) {
        if (!c.isAssignableFrom(this.getClass())) {
            typerror(c.getName());
        }
        return this;
    }

    @Override
    public Object checkuserdata() {
        return this;
    }

    @Override
    public Object checkuserdata(Class c) {
        if (c.isAssignableFrom(this.getClass())) {
            return this;
        }
        return typerror(c.getName());
    }

    /*@Override
    public LuaValue get(LuaValue key) {
        return m_metatable != null ? gettable(this, key) : NIL;
    }

    @Override
    public void set(LuaValue key, LuaValue value) {
        if (m_metatable == null || !settable(this, key, value)) {
            error("cannot set " + key + " for userdata");
        }
    }*/

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object val) {
        return val.hashCode() == this.hashCode();
    }

    ;

    @Override
    public int hashCode() {
        int hashcode = 5;
        hashcode = 97 * hashcode + Objects.hashCode(this.m_metatable);
        return hashcode;
    }

    // equality w/ metatable processing
    @Override
    public LuaValue eq(LuaValue val) {
        return eq_b(val) ? TRUE : FALSE;
    }

    @Override
    public boolean eq_b(LuaValue val) {
        if (val.raweq(this)) {
            return true;
        }
        if (m_metatable == null || !val.isuserdata()) {
            return false;
        }
        LuaValue valmt = val.getmetatable();
        return valmt != null && LuaValue.eqmtcall(this, (LuaValue) m_metatable, val, valmt);
    }

    // equality w/o metatable processing
    @Override
    public boolean raweq(LuaValue val) {
        return val.raweq(this);
    }

    public boolean raweq(ProtoUserdata val) {
        return this == val || (m_metatable == val.m_metatable);
    }

    // __eq metatag processing
    public boolean eqmt(LuaValue val) {
        return m_metatable != null && val.isuserdata() ? LuaValue.eqmtcall(this, (LuaValue) m_metatable, val, val.getmetatable()) : false;
    }

    protected synchronized static void MakeMethodsAvailibleToLua(final ProtoUserdata instance) {
        final Class thisClass = instance.getClass();

        if (classSetters.containsKey(thisClass) && classGetters.containsKey(thisClass) && classMethods.containsKey(thisClass)) {
            instance.getters = classGetters.get(thisClass);
            instance.setters = classSetters.get(thisClass);
            instance.methods = classMethods.get(thisClass);
            System.out.println("GET METHODS: " + thisClass.getSimpleName());
        } else {
            System.out.println("SET METHODS: " + thisClass.getSimpleName());
            instance.getters = new ConcurrentHashMap<>();
            instance.setters = new ConcurrentHashMap<>();
            instance.methods = new ConcurrentHashMap<>();

            Method[] methods = thisClass.getMethods();
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
                    instance.methods.put(method.getName(), forMethod(method));
                } else {
                    //System.out.println("Normal field: " + method.getName());
                    if (method.getName().substring(0, 3).equals("set")) {
                        instance.setters.put(method.getName().substring(3).toLowerCase(), forMethod(method));
                    } else {
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
                    instance.methods.put(methodName, forMethods(method));
                } else {
                    //System.out.println("Overloadable field: " + method[0].getName());
                    if (methodName.substring(0, 3).equals("set")) {
                        instance.setters.put(methodName.substring(3).toLowerCase(), forMethods(method));
                    } else {
                        instance.getters.put(methodName.substring(3).toLowerCase(), forMethods(method));
                    }
                }
            }
            classSetters.put(thisClass, instance.setters);
            classGetters.put(thisClass, instance.getters);
            classMethods.put(thisClass, instance.methods);
        }

        final LuaTable metatable = new LuaTable();
        metatable.set(INDEX, new TwoArgFunction() {
            @Override
            public LuaValue call(LuaValue instance, LuaValue key) {
                return ((ProtoUserdata) instance).getters.get(key.checkjstring().toLowerCase()).call(instance);
            }
        });
        metatable.set(NEWINDEX, new ThreeArgFunction() {
            @Override
            public LuaValue call(LuaValue instance, LuaValue key, LuaValue value) {
                org.luaj.vm2.LuaFunction m = ((ProtoUserdata) instance).setters.get(key.checkjstring().toLowerCase());
                return m.call(instance, value);
            }
        });
        metatable.set(METATABLE, valueOf("Metatable locked"));
        
        for(String methodName : instance.methods.keySet()){
            //System.out.println("Method: " + methodName);
            instance.set(methodName, instance.methods.get(methodName));
        }

        instance.setmetatable(metatable);
    }
}
