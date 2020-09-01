//-----------------------------------------------------------------------------
// Miny Function
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
import java.util.HashMap;

//-----------------------------------------------------------------------------
public class Func
{
    public String label;
    public Src src;
    public boolean isr;
    public TypeId type;
    public LinkedList<ICode> icodes = new LinkedList<ICode>();
    public LinkedList<Node> parms = new LinkedList<Node>();
    public LinkedList<Node> nodes = new LinkedList<Node>();
    public HashMap<String, Symbol> symbols = new HashMap<String, Symbol>();
    public HashMap<String, Symbol> mod_syms;
    public int bit_cnt, bit_base;
    public int reg_cnt, reg_base;

    //-------------------------------------------------------------------------
    public Func(String _label, Src _src)
    {
        label = _label;
        src = _src;
        type = TypeId.NONE;
        isr = false;
    }

    //-------------------------------------------------------------------------
    public String toString()
    {
        String str = label + '(';
        boolean comma = false;
        for (Node parm : parms)
        {
            if (comma) str += ", ";
            comma = true;
            str += parm.type;
        }

        str += ')';
        str += String.format(" reg %d %d bit %d %d",
               reg_cnt, reg_base, bit_cnt, bit_base);

        if (isr) str += " ISR";

        return str;
    }
}
