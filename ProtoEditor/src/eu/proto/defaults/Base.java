/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.defaults;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
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
    
    public void initFloor() {
        Box floorBox = new Box(Vector3f.ZERO, 10f, 0.1f, 5f);
        floorBox.scaleTextureCoordinates(new Vector2f(3, 6));

        Geometry floor = new Geometry("floor", floorBox);
        floor.setMaterial(defaultMaterial);
        floor.setShadowMode(RenderQueue.ShadowMode.Receive);
        floor.setLocalTranslation(0, 0, 0);
        floor.addControl(new RigidBodyControl(0));
        this.rootNode.attachChild(floor);
        this.getPhysicsSpace().add(floor);
    }

    
    @Override
    public void protoInit() {
        String scriptContent = new StringBuilder()
                //.append("a = 'assdasd'"+ls)
                .append("local part = new('Part')"+ls)
                .append("local vec = new.Vector3(2,4,6)"+ls)
                .append("part.size = vec"+ls)
                
                .append("local i = 1"+ls)
                .append("local add = 1"+ls)
                .append("print(\"AAAAAHHHHHH!!!!!!!!!!!!!!!!!!!!!!!!\")"+ls)
                .append("while true do"+ls)
                .append("wait(100)"+ls)
                .append("if i>9 then add=-1 elseif i==0 then add=1 end"+ls)
                //.append("local vec = part.size:add(new.Vector3(0,i,0))"+ls)
                .append("local vec2 = new.Vector3(0,-i/2,0)"+ls)
                .append("part.size = vec"+ls)
                .append("part.position = vec2"+ls)
                .append("print(i)"+ls)
                .append("i = i+add"+ls)
                .append("end"+ls)
                .toString();
        
        String scriptContent2 = new StringBuilder()
                //.append("a = 'assdasd'"+ls)
                .append("locqqal part = new('Part')"+ls)
                .append("local vec = new.Vector3(2,4,6)"+ls)
                .append("part.size = vec"+ls)
                
                .append("local i = 1"+ls)
                .append("local add = 1"+ls)
                .append("print(\"AAAAAHHHHHH!!!!!!!!!!!!!!!!!!!!!!!!\")"+ls)
                .append("while true do"+ls)
                .toString();
        
        script = protoObjectFactory.newInstance("LuaScript");
        script.setName("DaScript");
        script.setContent(scriptContent);
        script.run();
        
        LuaScript script2 = protoObjectFactory.newInstance("LuaScript");
        script2.setName("DaScript2");
        script2.setContent(scriptContent2);
        script2.run();
    }
}