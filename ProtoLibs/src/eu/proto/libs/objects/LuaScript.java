package eu.proto.libs.objects;

import com.jme3.asset.AssetKey;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import eu.proto.libs.lua.ProtoGlobals;
import java.io.IOException;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public class LuaScript extends ProtoObject{
    private final static String defaultName = "Script";
    
    private String content;
    private Globals environment;
    
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
    public LuaScript(){
        this(defaultName,"",ProtoGlobals.getGlobalEnvironment());
    }
    
    /**
     * Constructor instantiates a new <code>LuaScript</code> without content and
     * a default name.
     * 
     * @param environment
     *            The environment this script will run in
     */
    public LuaScript(LuaValue environment){
        this(defaultName,"",environment);
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
    public LuaScript(String name, String content,LuaValue environment){
        this.name = name;
        this.content = content;
        this.environment = (Globals) environment;
        //System.out.println("asdasd");
    }

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
        return environment;
    }

    public void setEnvironment(LuaValue environment) {
        this.environment = (Globals) environment;
    }
    
    /**
     * Runs this script by running the content trough the environment it's 
     * loadstring function.
     */
    public void run(){
        LuaValue func = environment.loadString(content, name);
        func.invoke();
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
        return new LuaScript(this.name,this.content,this.environment);
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
    public void init() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int type() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String typename() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
