/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs;

/**
 *
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
public interface Script {
    public abstract String getName();
    public abstract String getContent();
    public abstract void run();
}
