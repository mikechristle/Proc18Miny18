//-----------------------------------------------------------------------------
// Miny ICode Optimizer
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/16/2020   Add NOP command
//                      Change NOP ICodeId to NONE to support NOP command
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

import java.util.TreeMap;
import java.util.LinkedList;

//-----------------------------------------------------------------------------
public class ICodeOpt
{
    private TreeMap<String, Integer> markers = new TreeMap<>();
    private LinkedList<ICode> icodes;

    //-------------------------------------------------------------------------
    public void run()
    {
        for (String mkey : Module.modules.keySet())
        {
            Module module = Module.modules.get(mkey);
            for (String fkey : module.funcs.keySet())
            {
                Func func = module.funcs.get(fkey);
                icodes = func.icodes;
                index_markers();
                opt_markers();
            }
        }
    }

    //-------------------------------------------------------------------------
    private void index_markers()
    {
        ICode icode;
        int idx, top_idx = 0;
        String top_marker = null;

        // Find all markers
        for (idx = 1; idx < icodes.size(); idx++)
        {
            icode = icodes.get(idx);
            if (icode.id == ICodeId.MARKER)
            {
                if (top_marker == null)
                {
                    top_marker = (String)icode.parm;
                    top_idx = idx;
                    markers.put(top_marker, idx);
                }
                else
                {
                    markers.put((String)icode.parm, top_idx);
                    icode.id = ICodeId.NONE;
                }
            }
            else
                top_marker = null;
        }

        // Replace strings with icode index
        for (idx = 0; idx < icodes.size(); idx++)
        {
            icode = icodes.get(idx);
            if (icode.id == ICodeId.JMP ||
                icode.id == ICodeId.JMPF ||
                icode.id == ICodeId.JMPT)
            {
                icode.p2 = markers.get((String)icode.parm);
                icode.parm = icodes.get(icode.p2).parm;
            }
        }
    }

    //-------------------------------------------------------------------------
    private void opt_markers()
    {
        ICode icode, jcode, kcode;
        int idx, i, j;
        String str;

        // Remove code after return
        for (idx = 1; idx < icodes.size(); idx++)
        {
            icode = icodes.get(idx);
            if (icode.id == ICodeId.RETURN)
            {
                for (i = idx + 1; i < icodes.size(); i++)
                {
                    jcode = icodes.get(i);
                    if (jcode.id == ICodeId.MARKER) break;
                    if (jcode.id == ICodeId.JMP ||
                        jcode.id == ICodeId.JMPF ||
                        jcode.id == ICodeId.JMPT)
                    {
                        str = (String)jcode.parm;
                        if (markers.containsKey(str))
                            markers.remove(str);
                        icodes.get(jcode.p2).id = ICodeId.NONE;
                    }
                    jcode.id = ICodeId.NONE;
                }
            }
        }

        // Compress double jumps
        for (idx = 0; idx < icodes.size(); idx++)
        {
            icode = icodes.get(idx);
            if (icode.id == ICodeId.JMP ||
                icode.id == ICodeId.JMPF ||
                icode.id == ICodeId.JMPT)
            {
                while (true)
                {
                    i = find_next(icode.p2);
                    jcode = icodes.get(i);
                    if (jcode.id == ICodeId.JMP)
                    {
                        icode.p2 = jcode.p2;
                        icode.parm = jcode.parm;
                    }
                    else
                        break;
                }
                
            }
        }

        // Remove unused markers
        for (idx = 0; idx < icodes.size(); idx++)
        {
            icode = icodes.get(idx);
            if (icode.id == ICodeId.JMP ||
                icode.id == ICodeId.JMPF ||
                icode.id == ICodeId.JMPT)
            {
                str = (String)icode.parm;
                markers.remove(str);
            }
        }
        for (idx = 1; idx < icodes.size(); idx++)
        {
            icode = icodes.get(idx);
            if (icode.id == ICodeId.MARKER)
            {
                str = (String)icode.parm;
                if (markers.containsKey(str))
                {
                    markers.remove(str);
                    icode.id = ICodeId.NONE;
                }
            }
        }
        markers.clear();

        // Remove dead code
        for (idx = 0; idx < icodes.size(); idx++)
        {
            icode = icodes.get(idx);
            if (icode.id == ICodeId.JMP)
            {
                for (i = idx + 1; i < icodes.size(); i++)
                {
                    icode = icodes.get(i);
                    if (icode.id == ICodeId.MARKER ||
                        icode.id == ICodeId.RETURN) break;

                    icode.id = ICodeId.NONE;
                }
            }
        }

        // Remove useless jumps
        for (idx = 1; idx < icodes.size() - 1; idx++)
        {
            icode = icodes.get(idx);
            if (icode.id == ICodeId.JMP)
            {
                j = find_next(idx);
                jcode = icodes.get(j);
                if (jcode.id == ICodeId.MARKER)
                {
                    String m1 = (String)icode.parm;
                    String m2 = (String)jcode.parm;
                    if (m1.equals(m2))
                    {
                        icode.id = ICodeId.NONE;
                    }
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    private int find_next(int i)
    {
        i++;
        while (icodes.get(i).id == ICodeId.NONE) i++;
        return i;
    }
}
