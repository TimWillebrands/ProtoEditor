/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.defaults;

import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import eu.proto.libs.ProtoApp;
import eu.proto.libs.objects.LuaScript;
import eu.proto.libs.objects.Part;

/**
 *
 * @author tim
 */
public class Base extends ProtoApp{    
    LuaScript script;
    Part part;
    Part part2;
    Part part3;
    
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
        //initFloor();

        part = (Part) _G.get("new").call("Part");
        //part.set("size", "OHAI");
        //System.out.println(part.get("size").call());
        /*part2 = (Part) _G.get("new").call("Part");
        part3 = (Part) _G.get("new").call("Part");
        //TODO only sizing works
        part.setSize(new Vector3f(10, 1, 1));
        part2.setSize(new Vector3f(1, 10, 1));
        part3.setSize(new Vector3f(1, 1, 10));
        part.setRotation(0f , 0f , 20*FastMath.DEG_TO_RAD);
        part2.setRotation(0f , 30*FastMath.DEG_TO_RAD, 0f );
        part3.setRotation(40*FastMath.DEG_TO_RAD, 0f , 0f );*/
        script = new LuaScript("ThaScript", "local part = new('Part') \n part:setSize(20)\n print(part:size())", _G);
        script.run();
    }

    @Override
    public void protoUpdate(float tpf) {
    }
}