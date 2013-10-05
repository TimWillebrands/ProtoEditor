/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public abstract class DataPusher{
    public abstract void newLine();
    public abstract void pushData(int b);
    public abstract void pushData(byte buf[], int off, int len);
    public abstract void pushData(String str);
    public abstract void pushData(Exception ex);
}