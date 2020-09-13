//-----------------------------------------------------------------------------
// Miny Module
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/13/2020   Add variables to track hardwage config
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

import java.util.HashMap;

//-----------------------------------------------------------------------------
public class Module
{
    public HashMap<String, Func> funcs = new HashMap<String, Func>();
    public HashMap<String, Symbol> symbols = new HashMap<String, Symbol>();

    public static String[] isr_labels = new String[16];
    public static HashMap<String, Module> modules =
                     new HashMap<String, Module>();

    public static int rom_bits = 12;
    public static int ram_bits = 12;
    public static int con_bits = 12;

    //-------------------------------------------------------------------------
    public static Symbol find_symbol(Func func, String label) throws MError
    {
        int idx = label.indexOf('.');
        if (idx < 0)
        {
            if (func.symbols.containsKey(label))
                return func.symbols.get(label);

            if (func.mod_syms.containsKey(label))
                return func.mod_syms.get(label);
        }
        else
        {
            Module mod = Module.modules.get(label.substring(0, idx));
            label = label.substring(idx + 1, label.length());
            if (mod.symbols.containsKey(label))
                return mod.symbols.get(label);
        }

        throw new MError("Label not found: " + label);
    }

    //-------------------------------------------------------------------------
    public static Func find_func(String func_name)
    {
        int idx = func_name.indexOf('.');
        Module mod = Module.modules.get(func_name.substring(0, idx));
        return mod.funcs.get(func_name);
    }

    //-------------------------------------------------------------------------
    public static void add_isr(int level, String label) throws MError
    {
        if (isr_labels[level] != null)
            throw new MError(String.format("Duplicate ISR level: %d", level));

        isr_labels[level] = label;
    }
}
