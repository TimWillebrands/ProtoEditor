/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.proto.libs.lua;

/**
 * If this annotation is present at a getter/setter it will be connected in lua
 * as a metatable field that calls these methods.
 * 
 * @author Tim Willebrands <Tim.Willebrands@rave.eu>
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface LuaField {}
