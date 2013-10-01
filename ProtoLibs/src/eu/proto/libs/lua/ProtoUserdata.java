/**
 * *****************************************************************************
 * Copyright (c) 2009 Luaj.org. All rights reserved.
 * 
* Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
* The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
*****************************************************************************
 */
package eu.proto.libs.lua;

import java.util.Objects;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

public abstract class ProtoUserdata extends LuaValue {

    public LuaValue m_metatable = new LuaTable();

    @Override
    public String tojstring() {
        return String.valueOf(this);
    }

    @Override
    public int type() {
        return LuaValue.TUSERDATA;
    }

    @Override
    public String typename() {
        return "userdata";
    }

    public Object userdata() {
        return this;
    }

    @Override
    public boolean isuserdata() {
        return true;
    }

    @Override
    public boolean isuserdata(Class c) {
        return c.isAssignableFrom(this.getClass());
    }

    @Override
    public Object touserdata() {
        return this;
    }

    @Override
    public Object touserdata(Class c) {
        return c.isAssignableFrom(this.getClass()) ? this : null;
    }

    @Override
    public Object optuserdata(Object defval) {
        return this;
    }

    @Override
    public Object optuserdata(Class c, Object defval) {
        if (!c.isAssignableFrom(this.getClass())) {
            typerror(c.getName());
        }
        return this;
    }

    @Override
    public LuaValue getmetatable() {
        return m_metatable;
    }

    @Override
    public LuaValue setmetatable(LuaValue metatable) {
        this.m_metatable = metatable;
        return this;
    }

    @Override
    public Object checkuserdata() {
        return this;
    }

    @Override
    public Object checkuserdata(Class c) {
        if (c.isAssignableFrom(this.getClass())) {
            return this;
        }
        return typerror(c.getName());
    }

    @Override
    public LuaValue get(LuaValue key) {
        return m_metatable != null ? gettable(this, key) : NIL;
    }

    @Override
    public void set(LuaValue key, LuaValue value) {
        if (m_metatable == null || !settable(this, key, value)) {
            error("cannot set " + key + " for userdata");
        }
    }

    @Override
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    public boolean equals(Object val){
        return val.hashCode()==this.hashCode();
    };

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.m_metatable);
        return hash;
    }

    // equality w/ metatable processing
    @Override
    public LuaValue eq(LuaValue val) {
        return eq_b(val) ? TRUE : FALSE;
    }

    @Override
    public boolean eq_b(LuaValue val) {
        if (val.raweq(this)) {
            return true;
        }
        if (m_metatable == null || !val.isuserdata()) {
            return false;
        }
        LuaValue valmt = val.getmetatable();
        return valmt != null && LuaValue.eqmtcall(this, m_metatable, val, valmt);
    }

    // equality w/o metatable processing
    @Override
    public boolean raweq(LuaValue val) {
        return val.raweq(this);
    }

    public boolean raweq(ProtoUserdata val) {
        return this == val || (m_metatable == val.m_metatable);
    }

    // __eq metatag processing
    public boolean eqmt(LuaValue val) {
        return m_metatable != null && val.isuserdata() ? LuaValue.eqmtcall(this, m_metatable, val, val.getmetatable()) : false;
    }
}
