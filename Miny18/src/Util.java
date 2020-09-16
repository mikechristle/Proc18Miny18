//-----------------------------------------------------------------------------
// Miny Utilities
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/16/2020   Change NOP ICodeId to NONE to support NOP command
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

//-----------------------------------------------------------------------------
public class Util
{
    //-------------------------------------------------------------------------
    public static void dump_icodes()
    {
        System.out.println("=============================");

        for (String mkey : Module.modules.keySet())
        {
            Module module = Module.modules.get(mkey);
            for (String fkey : module.funcs.keySet())
            {
                System.out.println("-------------------");
                Func func = module.funcs.get(fkey);
                for (int i = 0; i < func.icodes.size(); i++)
                {
                    ICode icode = func.icodes.get(i);
                    if (icode.id != ICodeId.NONE)
                    {
                        String str = icode.toString();
                        str = String.format("%3d %s", i, str);
                        System.out.println(str);
                    }
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    public static void dump_parse_tree()
    {
        System.out.println("=============================");

        for (String mkey : Module.modules.keySet())
        {
            Module module = Module.modules.get(mkey);
            for (String fkey : module.funcs.keySet())
            {
                Func func = module.funcs.get(fkey);
                System.out.println(func.toString());

                for (Node node : func.nodes)
                    dump_node(node, 1);
            }
        }
    }

    //-------------------------------------------------------------------------
    public static void dump_node(Node node, int indent)
    {
        for (int i = 0; i < indent; i++)
            System.out.print(".  ");

        System.out.println(node.toString());

        switch (node.id)
        {
            case BLOCK:
            case CALL:
            case CONST:
                for (Node sub_node : ((BlockNode)node).nodes)
                    dump_node(sub_node, indent + 1);
                break;

            case ARRAY:
            case ASSIGN:
            case COMPARE:
            case IF:
            case LOOP:
            case MATHOP:
            case RETURN:
            case TIMER:
            StmtNode st_node = (StmtNode)node;
                if (st_node.p1 != null)
                    dump_node(st_node.p1, indent + 1);
                if (st_node.p2 != null)
                    dump_node(st_node.p2, indent + 1);
                if (st_node.p3 != null)
                    dump_node(st_node.p3, indent + 1);
                break;
        }
    }

    //-------------------------------------------------------------------------
    public static void dump_symbols()
    {
        System.out.println("=============================");

        for (String mkey : Module.modules.keySet())
        {
            Module module = Module.modules.get(mkey);
            System.out.println("======== Module " + mkey + " ========");

            for (String skey : module.symbols.keySet())
            {
                Symbol symbol = module.symbols.get(skey);
                String str = String.format("%-12s %s", skey, symbol);
                System.out.println(str);
            }

            for (String fkey : module.funcs.keySet())
            {
                Func func = module.funcs.get(fkey);
                System.out.println("---- Func " + fkey);

                for (String skey : func.symbols.keySet())
                {
                    Symbol symbol = func.symbols.get(skey);
                    String str = String.format("%-12s %s", skey, symbol);
                    System.out.println(str);
                }
            }
        }

        System.out.println("======== ISR Vectors ========");
        for (int i = 0; i < 16; i++)
            if (Module.isr_labels[i] != null)
                System.out.println(String.format("%2d %s", i,
                                                 Module.isr_labels[i]));
    }
}
