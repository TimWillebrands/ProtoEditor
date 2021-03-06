/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs.objects;

import eu.proto.libs.lua.ProtoMaterial;
import com.jme3.asset.AssetKey;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import eu.proto.libs.ProtoApp;
import eu.proto.libs.lua.LuaField;
import eu.proto.libs.lua.LuaFunction;
import eu.proto.libs.objects.DataTypes.Vector3;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public class Part extends ProtoObject {
    private Box box;
    private Geometry part;
    private ProtoMaterial material;
    
    @Override
    public void init(){
        
        box = new Box(4f, 1f, 2f);

        part = new Geometry("part", box);
        part.setMaterial(ProtoApp.defaultMaterial);
        part.setShadowMode(RenderQueue.ShadowMode.CastAndReceive);
        //part.setLocalTranslation(0, 0, 0);
        part.addControl(new RigidBodyControl(0));
        part.getControl(RigidBodyControl.class).setFriction(1.6f);
        this.name = "Part";
        
        this.app.getRootNode().attachChild(part);
        this.app.getPhysicsSpace().add(part);
    }
    
    
    
    @LuaField
    public Vector3 getPosition(){
        return new Vector3(part.getLocalTranslation());
    }
    
    @LuaField
    public void setPosition(Vector3 location){
        part.setLocalTranslation(location.toVector3f());
    }
    
    @LuaField
    public Vector3 getSize(){
        return new Vector3(box.getXExtent(),box.getYExtent(),box.getZExtent());
    }
    
    @LuaField
    public void setSize(Vector3 size){
        box.updateGeometry(box.getCenter(), size.x, size.y, size.z);
    }
    
    @LuaField
    public void setSize(float f){
        box.updateGeometry(box.getCenter(), f, f, f);
    }
    
    @LuaField
    public Quaternion getRotation(){
        return part.getLocalRotation();
    }
    
    @LuaField
    public void setRotation(Quaternion rotate){
        part.setLocalRotation(rotate);
        part.updateModelBound();
        //part.updateGeometry();
    }
    
    @LuaField
    public void setRotation(Vector3 rotate){
        this.setRotation(rotate.x,rotate.y,rotate.z);
    }
    
    @LuaField
    public void setRotation(float x, float y, float z){
        this.setRotation(new Quaternion().fromAngles(x,y,z));
    }

    @LuaField
    public ProtoMaterial getMaterial() {
        return material;
    }

    @LuaField
    public void setMaterial(ProtoMaterial material) {
        this.material = material;
        this.part.setMaterial(material.getMaterial());
    }
    
    @LuaField
    public float getMass(){
        return part.getControl(RigidBodyControl.class).getMass();
    }
    
    @LuaField
    public void setMass(float mass){
        part.getControl(RigidBodyControl.class).setMass(mass);
    }
    
    @LuaField
    public float getFriction(){
        return part.getControl(RigidBodyControl.class).getFriction();
    }
    
    @LuaField
    public void setFriction(float friction){
        part.getControl(RigidBodyControl.class).setFriction(friction);
    }
    
    @LuaField
    public String getShadowMode(){
        String mode = part.getShadowMode().toString();
        return mode.equals(RenderQueue.ShadowMode.CastAndReceive.toString()) ? "Full" : mode;
    }
    
    @LuaField
    public void setShadowMode(String mode){
        switch(mode){
            case "Full" : part.setShadowMode(RenderQueue.ShadowMode.CastAndReceive); break;
            case "Cast" : part.setShadowMode(RenderQueue.ShadowMode.Cast); break;
            case "Receive" : part.setShadowMode(RenderQueue.ShadowMode.Receive); break;
            case "Inherit" : part.setShadowMode(RenderQueue.ShadowMode.Inherit); break;
            case "Off" : part.setShadowMode(RenderQueue.ShadowMode.Off);break;
            default: throw new NullPointerException("Incorrect ShadowMode type");
        }
    }

    @LuaField
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @LuaField
    @Override
    public String getName() {
        return this.name;
    }
    
    @LuaFunction
    @Override
    public String toString(){
        return this.getName();
    }
    
    @LuaFunction
    public void pointTo(Vector3 point){
        part.lookAt(point.toVector3f(), Vector3f.UNIT_Y);
    }
    
    @LuaFunction
    public void pointTo(Vector3 point,Vector3 upVector){
        part.lookAt(point.toVector3f(), upVector.toVector3f());
    }
    
    @LuaFunction
    public void rotate(Quaternion rotate){
        part.rotate(rotate);
    }
    
    @LuaFunction
    public void rotate(Vector3 rotate){
        part.rotate(rotate.x,rotate.y,rotate.z);
    }
    
    @LuaFunction
    public void rotate(float x, float y, float z){
        part.rotate(x,y,z);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setKey(AssetKey key) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public AssetKey getKey() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public Part clone(){
        Part clone;
        try {
            clone = newInstance("Part",this.app);
            clone.setFriction(this.getFriction());
            clone.setMass(this.getMass());
            clone.setPosition(this.getPosition());
            clone.setSize(this.getSize());
            clone.setRotation(this.getRotation());
            clone.setShadowMode(this.getShadowMode());
        } catch (ClassNotFoundException ex) {
            clone = (Part) Part.NIL;
            Logger.getLogger(Part.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return clone;
    }
    
}
