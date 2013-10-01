/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs.lua;

import eu.proto.libs.ProtoApp;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.TwoArgFunction;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public class ProtoLuaLib extends TwoArgFunction {
    private final ProtoApp app;
    private final ProtoObjectFactory objectFactory;

    public ProtoLuaLib(ProtoApp app) {
        this.app = app;
        objectFactory = app.getObjectFactory();
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable protoLib = new LuaTable(0, 30);
        env.set("new", new New());
       
        return protoLib;
    }

    class New extends OneArgFunction {
        @Override
        public LuaValue call(LuaValue arg) {
            return objectFactory.newInstance(arg.checkjstring());
        }
    }
}
