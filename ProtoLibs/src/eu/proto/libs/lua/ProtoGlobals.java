/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs.lua;

import eu.proto.libs.ProtoApp;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseIoLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseOsLib;
import org.luaj.vm2.lib.jse.LuajavaLib;

/** ProtoGlobals
 *
 * @see ProtoGlobals
 */
public class ProtoGlobals {

    private static Globals globalEnvironment;

    /**
     * Create a standard set of globals for ProtoApps including all the
     * libraries.
     *
     * @return Table of globals initialized with the standard Proto libraries
     */
    public static Globals standardGlobals(ProtoApp app) {
        Globals _G = new Globals();
        _G.load(new JseBaseLib());
        _G.load(new PackageLib());
        _G.load(new Bit32Lib());
        _G.load(new TableLib());
        _G.load(new StringLib());
        _G.load(new CoroutineLib());
        _G.load(new JseMathLib());
        _G.load(new JseIoLib());
        _G.load(new JseOsLib());
        _G.load(new ProtoLuaLib(app));
        _G.load(new LuajavaLib());
        LuaC.install();
        _G.compiler = LuaC.instance;
        globalEnvironment = _G;
        
        return _G;
    }

    /**
     * Create standard globals including the {@link debug} library.
     *
     * @return Table of globals initialized with the standard JSE and debug
     * libraries
     * @see #standardGlobals()
     * @see DebugLib
     */
    public static Globals debugGlobals(ProtoApp app) {
        Globals _G = standardGlobals(app);
        _G.load(new DebugLib());
        globalEnvironment = _G;
        return _G;
    }

    /**
     * Gets the global environment if it is initialized
     *
     * @return The global environment table
     */
    public static Globals getGlobalEnvironment() {
        return globalEnvironment;
    }

    /**
     * Simple wrapper for invoking a lua function with command line arguments.
     * The supplied function is first given a new Globals object, then the
     * program is run with arguments.
     */
    public static void luaMain(LuaValue mainChunk, String[] args, ProtoApp app) {
        Globals g = standardGlobals(app);
        int n = args.length;
        LuaValue[] vargs = new LuaValue[args.length];
        for (int i = 0; i < n; ++i) {
            vargs[i] = LuaValue.valueOf(args[i]);
        }
        LuaValue arg = LuaValue.listOf(vargs);
        arg.set("n", n);
        g.set("arg", arg);
        mainChunk.initupvalue1(g);
        mainChunk.invoke(LuaValue.varargsOf(vargs));
    }
}
