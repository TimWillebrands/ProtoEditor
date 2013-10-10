/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs.lua;

import com.jme3.asset.AssetKey;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.material.Material;
import eu.proto.libs.objects.ProtoObject;
import java.io.IOException;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public abstract class ProtoMaterial extends ProtoObject {
    protected Material material;

    @LuaField
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @LuaField
    @Override
    public String getName() {
        return name;
    }

    public Material getMaterial() {
        return material;
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
    public ProtoObject clone() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
