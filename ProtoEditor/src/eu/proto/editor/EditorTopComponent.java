/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.editor;

import eu.proto.defaults.Base;
import eu.proto.libs.DataPusher;
import eu.proto.libs.ProtoApp;
import eu.proto.libs.objects.LuaScript;
import eu.proto.luaeditor.LuaEditorTopComponent;
import java.awt.Canvas;
import java.nio.ByteBuffer;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//eu.proto.editor//Editor//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "EditorTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Window", id = "eu.proto.editor.EditorTopComponent")
@ActionReference(path = "Menu/Window" /*, position = 333 */)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_EditorAction",
        preferredID = "EditorTopComponent")
@Messages({
    "CTL_EditorAction=Editor",
    "CTL_EditorTopComponent=Editor Window",
    "HINT_EditorTopComponent=This is a Editor window"
})
public final class EditorTopComponent extends TopComponent {
    final ProtoApp app;
    final Canvas canvas;
    final InputOutput io;

    public EditorTopComponent() {
        initComponents();
        setName(Bundle.CTL_EditorTopComponent());
        setToolTipText(Bundle.HINT_EditorTopComponent());
        
        
        //app = new TestBrickTower();
        app = new Base();
        
        io = IOProvider.getDefault().getIO(app.getClass().getSimpleName(), true);
        
        app.setSdtOut(new OutputDataPusher(io.getOut()));
        app.setSdtErr(new OutputDataPusher(io.getErr()){
            @Override public void pushData(String str){
                super.pushData(str.replace("[string ", "[script: "));
                super.newLine();
            }
        });
        
        final LuaScript script = app.getObjectFactory().newInstance("LuaScript");
        final String ls = System.lineSeparator();
        
        final String scriptContent = new StringBuilder()
                .append("print(getmetatable(new).__call)"+ls)
                .append("print(new.Vector3)"+ls)
                .append("local part = new(\"Part\")"+ls)
                .append("local vec = new.Vector3(2,4,6)"+ls)
                .append("part.size = vec"+ls+ls)
                
                .append("local i = 1"+ls)
                .append("local add = 1"+ls)
                .append("while true do"+ls)
                .append("\twait(500)"+ls)
                .append("\tif i>29 then add=-1 elseif i==0 then add=1 end"+ls)
                .append("\tprint(\"sdfgdfg\")"+ls)
                .append("\ti = i+add"+ls)
                .append("end"+ls)
                .toString();
        
        script.setName("DaScript");
        script.setContent(scriptContent);
        
        canvas = app.startAndGetCanvas();
        
        final int tabPos = this.getTabPosition()+2;

        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run() {
                LuaEditorTopComponent scriptEditor = new LuaEditorTopComponent(script);
                scriptEditor.openAtTabPosition(tabPos);
            }
        });
        
        app.runScripts();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        protoAppContainer = new javax.swing.JPanel();

        javax.swing.GroupLayout protoAppContainerLayout = new javax.swing.GroupLayout(protoAppContainer);
        protoAppContainer.setLayout(protoAppContainerLayout);
        protoAppContainerLayout.setHorizontalGroup(
            protoAppContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        protoAppContainerLayout.setVerticalGroup(
            protoAppContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(protoAppContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(protoAppContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel protoAppContainer;
    // End of variables declaration//GEN-END:variables
    @Override
    public void componentOpened() {
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(canvas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(canvas, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }

    @Override
    public void componentClosed() {
        app.stop();
        io.closeInputOutput();
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
    
    private class OutputDataPusher extends DataPusher {
        final OutputWriter out;
        OutputDataPusher(final OutputWriter out){this.out = out;}
        
        @Override public void newLine() {out.println();}
        @Override public void pushData(int b) {out.write(b);}
        @Override public void pushData(byte[] buf, int off, int len) {out.write(ByteBuffer.wrap(buf).asCharBuffer().array(), off, len);}
        @Override public void pushData(String str) {out.write(str);}
        @Override public void pushData(Exception ex) {out.write(ex.getLocalizedMessage());}
    }
}
