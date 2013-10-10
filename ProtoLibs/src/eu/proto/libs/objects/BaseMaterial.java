/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs.objects;

import com.jme3.asset.TextureKey;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.texture.Texture;
import eu.proto.libs.lua.ProtoMaterial;
import eu.proto.libs.objects.DataTypes.Color4;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public class BaseMaterial extends ProtoMaterial{
    private String tex = "";
    Color4 color = new Color4(ColorRGBA.randomColor());

    @Override
    public void init() {
        this.material = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
        this.material.setColor("Color", color.toColorRGBA());
    }
    
    public void setColor(Color4 color){
        this.material.setColor("Color", color.toColorRGBA());
    }
    
    public Color4 getColor(){
        return this.color;
    }
    
    public void setTexture(String tex){
        this.tex= tex;
        TextureKey key = new TextureKey(tex); //"Textures/Terrain/BrickWall/BrickWall.jpg"
        key.setGenerateMips(true);
        Texture texture = app.getAssetManager().loadTexture(key);
        this.material.setTexture("ColorMap", texture);
    }
    
    public String getTexture(){
        return this.tex;
    }
}
