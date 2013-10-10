/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.defaults;

import eu.proto.libs.ProtoApp;
import eu.proto.libs.objects.LuaScript;

/**
 *
 * @author tim
 */
public class Base extends ProtoApp{    
    private final String ls = System.lineSeparator();
    
    LuaScript script;
    String script2Content;

    
    @Override
    public void protoInit() {
        
        String scriptContent = new StringBuilder()
                //.append("a = 'assdasd'"+ls)
                .append("print(getmetatable(new).__call)"+ls)
                .append("print(new.Vector3)"+ls)
                .append("local part = new('Part')"+ls)
                .append("local vec = new.Vector3(2,4,6)"+ls)
                //.append("print(vec.divideLocal)"+ls)
                //.append("print(vec:divideLocal(2))"+ls)
                .append("part.size = vec"+ls)
                
                .append("local i = 1"+ls)
                .append("local add = 1"+ls)
                .append("while true do"+ls)
                .append("wait(500)"+ls)
                .append("if i>29 then add=-1 elseif i==0 then add=1 end"+ls)
                //.append("print(vec:divideLocal(2))"+ls)
                .append("pint('sdfgdfg')"+ls)
                .append("i = i+add"+ls)
                .append("end"+ls)
                .toString();
        
        script = protoObjectFactory.newInstance("LuaScript");
        script.setName("DaScript");
        script.setContent(scriptContent);
        //script.run();
    }
}