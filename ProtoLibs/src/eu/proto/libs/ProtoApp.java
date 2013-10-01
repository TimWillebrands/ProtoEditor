package eu.proto.libs;

import com.jme3.app.Application;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.state.AppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeCanvasContext;
import com.jme3.system.JmeContext.Type;
import com.jme3.util.SkyFactory;
import eu.proto.libs.lua.ProtoGlobals;
import eu.proto.libs.lua.ProtoObjectFactory;
import org.luaj.vm2.*;
import java.awt.Canvas;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public abstract class ProtoApp extends Application{

    public static final String INPUT_MAPPING_EXIT = "SIMPLEAPP_Exit";
    public static final String INPUT_MAPPING_CAMERA_POS = DebugKeysAppState.INPUT_MAPPING_CAMERA_POS;
    public static final String INPUT_MAPPING_MEMORY = DebugKeysAppState.INPUT_MAPPING_MEMORY;
    public static final String INPUT_MAPPING_HIDE_STATS = "SIMPLEAPP_HideStats";
    
    public static Material defaultMaterial;
                             
    public final int hash;
    
    protected final Node rootNode = new Node("Root Node");
    protected final Node guiNode = new Node("Gui Node");
    protected final ProtoObjectFactory protoObjectFactory = new ProtoObjectFactory(this);
    //protected final LuaValue _G = ProtoGlobals.standardGlobals(this);
    protected final LuaValue _G = ProtoGlobals.debugGlobals(this);
    
    protected BulletAppState bulletAppState;
    protected PssmShadowRenderer bsr;
    protected BitmapText fpsText;
    protected BitmapFont guiFont;
    protected FlyByCamera flyCam;
    protected boolean showSettings = true;
    
    private AppActionListener actionListener = new AppActionListener();
    
    private class AppActionListener implements ActionListener {

        @Override
        public void onAction(String name, boolean value, float tpf) {
            if (!value) {
                return;
            }
            switch (name) {
                case INPUT_MAPPING_EXIT:
                    break;
                case INPUT_MAPPING_HIDE_STATS:
                    if (stateManager.getState(StatsAppState.class) != null) {
                        stateManager.getState(StatsAppState.class).toggleStats();
                    }
                    break;
                default:
                    stop();
                    break;
            }
        }
    }

    public ProtoApp() {
        this( new StatsAppState(), new FlyCamAppState(), new DebugKeysAppState() );
    }

    public ProtoApp( AppState... initialStates ) {
        super();
        
        if (initialStates != null) {
            for (AppState a : initialStates) {
                if (a != null) {
                    stateManager.attach(a);
                }
            }
        }
        
        this.hash = this.hashCode();
    }

    @Override
    public void start() {
        // set some default settings in-case
        // settings dialog is not shown
        if (settings == null) {
            setSettings(new AppSettings(true));
        }
        
        settings.setTitle("Proto - " + settings.getTitle());
        
        setSettings(settings);
        super.start();
    }
    
    public Canvas startAndGetCanvas(){
        this.setPauseOnLostFocus(false);
        this.createCanvas();
        this.startCanvas();
        JmeCanvasContext jmeContext = (JmeCanvasContext) this.getContext();
        return jmeContext.getCanvas();
    }

    /**
     * Retrieves ObjectFactory
     * @return ProtoObjectFactory object
     *
     */
    public ProtoObjectFactory getObjectFactory() {
        return protoObjectFactory;
    }

    /**
     * Retrieves flyCam
     * @return flyCam Camera object
     *
     */
    public FlyByCamera getFlyByCamera() {
        return flyCam;
    }

    /**
     * Retrieves guiNode
     * @return guiNode Node object
     *
     */
    public Node getGuiNode() {
        return guiNode;
    }

    /**
     * Gets the PhysicsSpace from the bulletAppState of this ProtoApp
     * @return PhysicsSpace object
     *
     */
    public PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }

    /**
     * Retrieves rootNode
     * @return rootNode Node object
     *
     */
    public Node getRootNode() {
        return rootNode;
    }

    public boolean isShowSettings() {
        return showSettings;
    }

    /**
     * Toggles settings window to display at start-up
     * @param showSettings Sets true/false
     *
     */
    public void setShowSettings(boolean showSettings) {
        this.showSettings = showSettings;
    }

    /**
     *  Creates the font that will be set to the guiFont field
     *  and subsequently set as the font for the stats text.
     */
    protected BitmapFont loadGuiFont() {
        return assetManager.loadFont("Interface/Fonts/Default.fnt");
    }

    @Override
    public void initialize() {
        super.initialize();

        // Several things rely on having this
        guiFont = loadGuiFont();

        guiNode.setQueueBucket(Bucket.Gui);
        guiNode.setCullHint(CullHint.Never);
        viewPort.attachScene(rootNode);
        guiViewPort.attachScene(guiNode);

        if (inputManager != null) {
        
            // We have to special-case the FlyCamAppState because too
            // many SimpleApplication subclasses expect it to exist in
            // simpleInit().  But at least it only gets initialized if
            // the app state is added.
            if (stateManager.getState(FlyCamAppState.class) != null) {
                flyCam = new FlyByCamera(cam);
                cam.setLocation(new Vector3f(0, 25f, 12f));
                cam.lookAt(Vector3f.ZERO, new Vector3f(0, 1, 0));
                cam.setFrustumFar(256);
                stateManager.getState(FlyCamAppState.class).setCamera( flyCam ); 
            }

            if (context.getType() == Type.Display) {
                inputManager.addMapping(INPUT_MAPPING_EXIT, new KeyTrigger(KeyInput.KEY_ESCAPE));
            }

            if (stateManager.getState(StatsAppState.class) != null) {
                inputManager.addMapping(INPUT_MAPPING_HIDE_STATS, new KeyTrigger(KeyInput.KEY_F5));
                inputManager.addListener(actionListener, INPUT_MAPPING_HIDE_STATS);            
            }
            
            inputManager.addListener(actionListener, INPUT_MAPPING_EXIT);            
        }

        if (stateManager.getState(StatsAppState.class) != null) {
            // Some of the tests rely on having access to fpsText
            // for quick display.  Maybe a different way would be better.
            stateManager.getState(StatsAppState.class).setFont(guiFont);
            fpsText = stateManager.getState(StatsAppState.class).getFpsText();
        }
        
        rootNode.attachChild(SkyFactory.createSky(
            assetManager, "Textures/Sky/Bright/BrightSky.dds", false));

        
        
        cam.setFrustumPerspective(45f, (float)cam.getWidth() / cam.getHeight(), 0.01f, 1000f);
        
        defaultMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        /*TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);*/
        defaultMaterial.setColor("Color", ColorRGBA.Blue);
        
        bulletAppState = new BulletAppState();
        bulletAppState.setThreadingType(BulletAppState.ThreadingType.SEQUENTIAL);
        stateManager.attach(bulletAppState);
        
        rootNode.setShadowMode(RenderQueue.ShadowMode.Off);
        bsr = new PssmShadowRenderer(assetManager, 1024, 2);
        bsr.setDirection(new Vector3f(-1, -1, -1).normalizeLocal());
        bsr.setLambda(0.55f);
        bsr.setShadowIntensity(0.6f);
        bsr.setCompareMode(PssmShadowRenderer.CompareMode.Hardware);
        bsr.setFilterMode(PssmShadowRenderer.FilterMode.PCF4);
        viewPort.addProcessor(bsr);
        
        protoInit();
    }

    @Override
    public void update() {
        super.update(); // makes sure to execute AppTasks
        if (speed == 0 || paused) {
            return;
        }

        float tpf = timer.getTimePerFrame() * speed;

        // protoUpdate states
        stateManager.update(tpf);

        // simple protoUpdate and root node
        protoUpdate(tpf);
 
        rootNode.updateLogicalState(tpf);
        guiNode.updateLogicalState(tpf);
        
        rootNode.updateGeometricState();
        guiNode.updateGeometricState();

        // render states
        stateManager.render(renderManager);
        renderManager.render(tpf, context.isRenderable());
        simpleRender(renderManager);
        stateManager.postRender();        
    }

    public void setDisplayFps(boolean show) {
        if (stateManager.getState(StatsAppState.class) != null) {
            stateManager.getState(StatsAppState.class).setDisplayFps(show);
        }
    }

    public void setDisplayStatView(boolean show) {
        if (stateManager.getState(StatsAppState.class) != null) {
            stateManager.getState(StatsAppState.class).setDisplayStatView(show);
        }
    }

    public abstract void protoInit();

    public abstract void protoUpdate(float tpf);

    public void simpleRender(RenderManager rm) {
    }
}