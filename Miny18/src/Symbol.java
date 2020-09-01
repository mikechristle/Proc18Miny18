//-----------------------------------------------------------------------------
// Miny Symbol
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
//-----------------------------------------------------------------------------
// Copyright 2020 Mike Christle
//
// Permission is hereby granted, free of charge, to any person obtaining a
// copy of this software and associated documentation files (the "Software"),
// to deal in the Software without restriction, including without limitation
// the rights to use, copy, modify, merge, publish, distribute, sublicense,
// and/or sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
// THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
// DEALINGS IN THE SOFTWARE.
//-----------------------------------------------------------------------------

import java.util.LinkedList;

enum StoreId {NONE, REG, RAM, ROM, BIT, PORT};

//-----------------------------------------------------------------------------
public class Symbol
{
    public TypeId type;
    public Object value;
    public LinkedList<Object> values;
    public int count;
    public int offset;
    public StoreId store;

    //-------------------------------------------------------------------------
    public Symbol(TypeId _type, StoreId _store)
    {
        type = _type;
        value = null;
        values = null;
        count = 1;
        offset = 0;
        store = _store;
    }

    //-------------------------------------------------------------------------
    public Symbol(TypeId _type, Object _value)
    {
        type = _type;
        value = _value;
        values = null;
        count = 1;
        offset = 0;
        store = StoreId.ROM;
    }

    //-------------------------------------------------------------------------
    public String toString()
    {
        String str = String.format("%-5s %-4s %4d", 
                                   type, store.toString(), count);

        if (offset >= 0) str += String.format(" [%d]", offset);
        if (value != null) str += String.format(" = %s", value);
        if (values != null) str += String.format(" = %s", values);

        return str;
    }
}
