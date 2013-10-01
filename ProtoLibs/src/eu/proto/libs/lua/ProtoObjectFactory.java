/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs.lua;

import eu.proto.libs.ProtoApp;
import eu.proto.libs.objects.Part;
import eu.proto.libs.objects.ProtoObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public class ProtoObjectFactory {
    private final ProtoApp app;
    
    @SuppressWarnings("LeakingThisInConstructor")
    public ProtoObjectFactory(ProtoApp app){
        this.app = app;
    }
    
    public <T extends ProtoObject> T newInstance(String className){
        T instance;
        try {
            instance = ProtoObject.newInstance(className,app);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ProtoObjectFactory.class.getName()).log(Level.SEVERE, null, ex);
            instance = (T) new Part();
            instance.setApp(app);
            instance.init();
        }
        
        return instance;
    }
    
}
