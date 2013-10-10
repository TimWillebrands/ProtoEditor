/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs.lua;

import eu.proto.libs.ProtoApp;
import eu.proto.libs.objects.DataTypes.Vector3;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.VarArgFunction;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public class ProtoLuaLib extends TwoArgFunction {
    private final ProtoApp app;
    private final ProtoObjectFactory objectFactory;
    
    private final Print print = new Print();

    public ProtoLuaLib(ProtoApp app) {
        this.app = app;
        this.objectFactory = app.getObjectFactory();
    }

    @Override
    public LuaValue call(LuaValue modname, LuaValue env) {
        LuaTable New = new LuaTable();
        LuaTable NewMetaTable = new LuaTable();
        
        NewMetaTable.set(LuaValue.CALL, new NewInstance());
        
        New.set("_G", new LuaTable());
        New.set("Vector3", Vector3.newInstance());
        
        New.setmetatable(NewMetaTable);
        env.set("new", New);
        env.set("print", print);
	env.get("package").get("loaded").set("new", New);
       
        return New;
    }

    class NewInstance extends TwoArgFunction {
        @Override
        public LuaValue call(LuaValue table,LuaValue arg) {
            return objectFactory.newInstance(arg.checkjstring());
        }
    }
    
    private class Print extends VarArgFunction {
        @Override
        public Varargs invoke(Varargs args) {
            System.out.println(args);
            System.out.println(app.getSdtOut());
            if(app.getSdtOut() != null){
                //LuaValue tostring = env.get("tostring");
                for (int i = 1, n = args.narg(); i <= n; i++) {
                    if (i > 1) {
                        //env.STDOUT.write('\t');
                        app.getSdtOut().pushData('\t');
                    }
                    //LuaString s = tostring.call(args.arg(i)).strvalue();
                    //env.STDOUT.write(s.m_bytes, s.m_offset, s.m_length);
                    //app.getSdtOut().pushData(s.m_bytes, s.m_offset, s.m_length);
                    app.getSdtOut().pushData(args.arg(i).tojstring());
                }
                //env.STDOUT.println();
                app.getSdtOut().newLine();
            }else{
                System.out.println("No output pusher availible");
            }
            return NONE;
        }
    }
}
