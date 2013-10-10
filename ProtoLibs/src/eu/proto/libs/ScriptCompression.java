/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 * @author Some guy from Stack Overflow
 */
public class ScriptCompression {

    public static String compress(String str) throws IOException{
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Deflater compresser = new Deflater(Deflater.BEST_COMPRESSION, true);
        try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(stream, compresser)) {
            deflaterOutputStream.write(str.getBytes());
        }
        byte[] output = stream.toByteArray();
        return String.valueOf(output);
    }

    public static String decompress(String str) throws IOException {
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        Inflater decompresser = new Inflater(true);
        try (InflaterOutputStream inflaterOutputStream = new InflaterOutputStream(stream2, decompresser)) {
            inflaterOutputStream.write(str.getBytes());
        }
        byte[] output = stream2.toByteArray();
        
        return String.valueOf(output);
    }
}
