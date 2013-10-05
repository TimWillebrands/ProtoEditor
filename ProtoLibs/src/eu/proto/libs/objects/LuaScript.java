package eu.proto.libs.objects;

import com.jme3.asset.AssetKey;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import eu.proto.libs.DataPusher;
import java.io.IOException;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaThread;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.ThreeArgFunction;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public class LuaScript extends ProtoObject{
    private final static String defaultName = "Script";
    
    private String content;
    private LuaTable _ENV;
    LuaThread scriptCoroutine;
    
    /**
     * Used for smart asset caching
     * 
     * @see AssetKey#useSmartCache() 
     */
    protected AssetKey key;
    
    /**
     * Constructor instantiates a new <code>LuaScript</code> without content,
     * a default name and in the global environment if it exists, if the global
     * environment does not exist it will throw an illigalStateException;
     * .
     */
    /*public LuaScript(){
        this(defaultName,"",ProtoGlobals.getGlobalEnvironment());
    }*/
    
    
    @Override
    public void init() {
        this.name = defaultName;
        this.content = "print(\"Hello World\")";
        this._ENV = (LuaTable) app.getEnvironment();
    }
    
    /**
     * Constructor instantiates a new <code>LuaScript</code> without content and
     * a default name.
     * 
     * @param environment
     *            The environment this script will run in
     */
    public LuaScript(){
        /*super();
        this.name = "Script";
        this.content = "print(\"Hello World\")";
        this.environment = (LuaTable) app.getEnvironment();*/
    }
    
    /**
     * Constructor instantiates a new <code>LuaScript</code> with content parameter
     * being the code in Lua script.
     * 
     * @param name
     *            Name of this script, not unique
     * @param content
     *            Lua code that makes up the content of this script
     * @param environment
     *            The environment this script will run in
     */
    /*public LuaScript(String name, String content,LuaValue environment){
        this.name = name;
        this.content = content;
        this.environment = (Globals) environment;
        //System.out.println("asdasd");
    }*/

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public LuaValue getEnvironment() {
        return _ENV;
    }

    public void setEnvironment(LuaValue environment) {
        this._ENV = (LuaTable) environment;
    }
    
    /**
     * Runs this script by running the content trough the environment it's 
     * loadstring function.
     */
    public void run(){
        Globals roEnv = new Globals();
        LuaTable roEnvMt = new LuaTable();
        
        roEnvMt.set(INDEX, this._ENV.get("_G"));
        roEnvMt.set(NEWINDEX, new ThreeArgFunction(){
            @Override
            public LuaValue call(LuaValue arg1, LuaValue key, LuaValue value) {
                //return error("Attempt to modify read-only table");
                arg1.get("_G").set(key,value);
                return NIL;
            }
        });
        roEnvMt.set(METATABLE, LuaValue.FALSE);
        roEnv.set("this", this);
        roEnv.set("wait", new OneArgFunction(){
            @Override
            public LuaValue call(LuaValue arg) {
                if(arg.isnumber()){
                    app.addWait(scriptCoroutine, arg.checklong());
                    //return _ENV.get("coroutine").get("yield").call();
                    return scriptCoroutine.state.lua_yield(LuaValue.NIL).arg1();
                }if(arg.isnil()){
                    app.addWait(scriptCoroutine, 0);
                    return scriptCoroutine.state.lua_yield(LuaValue.NIL).arg1();
                }else{
                    return NIL;
                }
            }
        });
        roEnv.setmetatable(roEnvMt);
        
        LuaValue[] ar = {valueOf(content),valueOf(name),valueOf("t"),roEnv}; // Parameters for the load function
        Varargs func = _ENV.get("load").invoke(LuaValue.varargsOf(ar)); // Load function
        if(func.arg(1).isfunction()){
            scriptCoroutine = new LuaThread(roEnv,func.arg1()); // Create coroutine from function
            Varargs ret = scriptCoroutine.resume(NIL); // Start that function
        }else if (func.arg(1).isnil()){
            app.getSdtErr().pushData(func.arg(2).tojstring());
        }   
    }
    
    /**
     * Creates a clone of the script. 
     * 
     * @return A clone of this script. 
     * The cloned script cannot reference equal this script as it's id will differ
     * but all other values will be equal.
     */
    @Override
    public LuaScript clone(){
        LuaScript clone;
        clone = app.getObjectFactory().newInstance("LuaScript");
        clone.name = this.name;
        clone.content = this.content;
        return clone;
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule capsule = ex.getCapsule(this);
        capsule.write(name, "name", null);
        capsule.write(content, "content", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule ic = im.getCapsule(this);

        name = ic.readString("name", null);
        content = ic.readString("content", null);
    }

    @Override
    public void setKey(AssetKey key) {
        this.key = key;
    }

    @Override
    public AssetKey getKey() {
        return key;
    }

    @Override
    public int type() {
        return TUSERDATA;
    }

    @Override
    public String typename() {
        return "userdata";
    }
    
}
