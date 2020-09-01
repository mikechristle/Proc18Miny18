//-----------------------------------------------------------------------------
// Miny ICode Generator
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

import java.util.Stack;
import java.util.LinkedList;

//-----------------------------------------------------------------------------
public class ICodeGen
{
    private LinkedList<ICode> icodes;
    private Stack<String> top_markers = new Stack<>();
    private Stack<String> bot_markers = new Stack<>();
    private String func_name;
    private int temp_idx;
    private Module module;
    private Func func;

    //-------------------------------------------------------------------------
    public void run() throws MError
    {
        for (String mkey : Module.modules.keySet())
        {
            module = Module.modules.get(mkey);

            for (String fkey : module.funcs.keySet())
            {
                func = module.funcs.get(fkey);
                func_name = fkey;
                icodes = func.icodes;
                icodes.add(new ICode(ICodeId.MARKER, func.src, fkey));
                temp_idx = 0;

                for (Node node : func.nodes)
                    gen_icode(node);

                if (icodes.getLast().id != ICodeId.RETURN)
                    icodes.add(new ICode(ICodeId.RETURN, func.src));

                top_markers.clear();
                bot_markers.clear();

                int size = func.icodes.size();
                for (int i = 0; i < func.icodes.size(); i++)
                {
                    ICode icode = func.icodes.get(i);
                    if (icode.p1 >= 0)
                    {
                        ICode ic1 = func.icodes.get(icode.p1);
                        if (ic1.id == ICodeId.REG || ic1.id == ICodeId.ROM ||
                            ic1.id == ICodeId.RAM || ic1.id == ICodeId.BIT ||
                            ic1.id == ICodeId.OUT || ic1.id == ICodeId.IN)
                            ic1.last_ref = size;
                        else
                            ic1.last_ref = i;
                    }
                    if (icode.p2 >= 0)
                    {
                        ICode ic2 = func.icodes.get(icode.p2);
                        if (ic2.id == ICodeId.REG || ic2.id == ICodeId.ROM ||
                            ic2.id == ICodeId.RAM || ic2.id == ICodeId.BIT ||
                            ic2.id == ICodeId.OUT || ic2.id == ICodeId.IN)
                            ic2.last_ref = size;
                        else
                            ic2.last_ref = i;
                    }
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    private int gen_icode(Node node) throws MError
    {
        switch (node.id)
        {
            case BREAK:    return break_node(node);
            case CONTINUE: return continue_node(node);
            case HALT:     return id_node(node, ICodeId.HALT);
            case PAUSE:    return id_node(node, ICodeId.PAUSE);
            case RESET:    return id_node(node, ICodeId.RESET);
            case RESTART:  return id_node(node, ICodeId.RESTART);

            case LEVEL: return parm_node(node, ICodeId.LEVEL);
            case BCON:  return parm_node(node, ICodeId.BCON);
            case ICON:  return parm_node(node, ICodeId.ICON);
            case SCON:  return parm_node(node, ICodeId.SCON);

            case BLOCK:   return block_node(node);
            case LABEL:   return label_node(node);
            case PORT:    return port_node(node);
            case IF:      return if_node(node);
            case LOOP:    return loop_node(node);
            case CALL:    return call_node(node);
            case ASSIGN:  return assign_node(node);
            case COMPARE: return compare_node(node);
            case MATHOP:  return mathop_node(node);
            case ARRAY:   return array_node(node);
            case RETURN:  return return_node(node);
            case TIMER:   return timer_node(node);

            default:
                throw new MError("Invalid node: " + node.id.toString(),
                                 node.src);
        }
    }

    //-------------------------------------------------------------------------
    private int parm_node(Node node, ICodeId id) throws MError
    {
        icodes.add(new ICode(id, node.src, node.value));
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int id_node(Node node, ICodeId id) throws MError
    {
        icodes.add(new ICode(id, node.src));
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int break_node(Node node) throws MError
    {
        String marker = bot_markers.peek();
        icodes.add(new ICode(ICodeId.JMP, node.src, marker));
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int continue_node(Node node) throws MError
    {
        String marker = top_markers.peek();
        icodes.add(new ICode(ICodeId.JMP, node.src, marker));
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int call_node(Node node) throws MError
    {
        BlockNode bnode = (BlockNode)node;
        String func_name = (String)bnode.value;
        Func func = Module.find_func(func_name);

        for (int i = 0; i < bnode.nodes.size(); i++)
        {
            Node n = bnode.nodes.get(i);
            int op = gen_icode(n);
            ICode icode = new ICode(ICodeId.PARM, n.src);
            Node parm = func.parms.get(i);
            icode.parm = (String)parm.value;
            icode.p1 = op;
            icodes.add(icode);
        }

        icodes.add(new ICode(ICodeId.CALL, node.src, node.value));

        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int array_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        ICode icode = new ICode(ICodeId.ARRAY, snode.src, snode.value);
        icode.p1 = gen_icode(snode.p1);
        icodes.add(icode);
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int timer_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        ICode icode = new ICode(ICodeId.TIMER, snode.src);
        icode.p1 = gen_icode(snode.p1);
        icodes.add(icode);
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int return_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        ICode icode = new ICode(ICodeId.RETURN, snode.src);
        if (snode.p1 != null) icode.p1 = gen_icode(snode.p1);
        icodes.add(icode);
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int mathop_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;

        ICodeId id = ICodeId.MATHI;
        if (snode.type == TypeId.BOOL) id = ICodeId.MATHB;

        ICode icode = new ICode(id, snode.src, snode.value);
        icode.p1 = gen_icode(snode.p1);
        if (snode.p2 != null)
            icode.p2 = gen_icode(snode.p2);
        icodes.add(icode);
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int compare_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        ICodeId id = ICodeId.COMPI;
        if (snode.p1.type == TypeId.BOOL) id = ICodeId.COMPB;
        ICode icode = new ICode(id, snode.src, snode.value);
        icode.p1 = gen_icode(snode.p1);
        icode.p2 = gen_icode(snode.p2);
        icodes.add(icode);
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int block_node(Node node) throws MError
    {
        BlockNode bnode = (BlockNode)node;

        for (Node n : bnode.nodes)
            gen_icode(n);

        return 0;
    }

    //-------------------------------------------------------------------------
    private int loop_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        String top = temp_marker();
        String bot = temp_marker();
        top_markers.push(top);
        bot_markers.push(bot);

        icodes.add(new ICode(ICodeId.MARKER, snode.src, top));
        int idx = icodes.size() - 1;

        gen_icode(snode.p1);

        icodes.add(new ICode(ICodeId.JMP, snode.src, top));
        icodes.add(new ICode(ICodeId.MARKER, snode.src, bot));

        top_markers.pop();
        bot_markers.pop();

        return 0;
    }

    //-------------------------------------------------------------------------
    private int if_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        String else_marker = temp_marker();
        String exit_marker = temp_marker();

        ICode icode = new ICode(ICodeId.JMPF, snode.src, else_marker);
        if (snode.p1.id == NodeId.MATHOP && 
            snode.p1.value == MathOp.LG_NOT)
        {
            icode.id = ICodeId.JMPT;
            StmtNode snode1 = (StmtNode)snode.p1;
            icode.p1 = gen_icode(snode1.p1);
        }
        else
        {
            icode.p1 = gen_icode(snode.p1);
        }
        icodes.add(icode);

        if (snode.p2.id == NodeId.BREAK)
        {
            icode.id = ICodeId.JMPT;
            icode.parm = bot_markers.peek();
        }
        else if (snode.p2.id == NodeId.CONTINUE) 
        {
            icode.id = ICodeId.JMPT;
            icode.parm = top_markers.peek();
        }
        else
            gen_icode(snode.p2);

        if (snode.p3 == null)
        {
            icodes.add(new ICode(ICodeId.MARKER, snode.src, else_marker));
        }
        else
        {
            icodes.add(new ICode(ICodeId.JMP, snode.src, exit_marker));
            icodes.add(new ICode(ICodeId.MARKER, snode.src, else_marker));
            gen_icode(snode.p3);
            icodes.add(new ICode(ICodeId.MARKER, snode.src, exit_marker));
        }
        return 0;
    }

    //-------------------------------------------------------------------------
    private int assign_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        int src = gen_icode(snode.p2);
        int dst = gen_icode(snode.p1);

        ICode idst = icodes.get(dst);
        if (idst.id == ICodeId.ARRAY) idst.id = ICodeId.ADDRESS;
        else if (idst.id == ICodeId.IN) idst.id = ICodeId.OUT;

        ICodeId id = ICodeId.ASSIGNI;
        if (snode.type == TypeId.BOOL) id = ICodeId.ASSIGNB;

        ICode icode = new ICode(id, snode.src);
        icode.p1 = dst;
        icode.p2 = src;
        icode.parm = snode.value;
        icodes.add(icode);

        return 0;
    }

    //-------------------------------------------------------------------------
    private int label_node(Node node) throws MError
    {
        String label = (String)node.value;
        Object value = node.value;
        Symbol sym = Module.find_symbol(func, label);

        ICodeId id; // = ICodeId.REG;
        switch (sym.store)
        {
            case BIT: id = ICodeId.BIT; break;
            case REG: id = ICodeId.REG; break;
            case RAM: id = ICodeId.RAM; break;
            case PORT: id = ICodeId.OUT; break;
            default: // ROM
                id = ICodeId.ROM;
                if (sym.count == 1)
                {
                    if (sym.type == TypeId.BOOL)
                        id = ICodeId.BCON;
                    else
                        id = ICodeId.ICON;
                    value = sym.values.get(0);
                }
                break;
        }

        icodes.add(new ICode(id, node.src, value));
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private int port_node(Node node) throws MError
    {
        ICode icode = new ICode(ICodeId.IN, node.src, node.value);
        icodes.add(icode);
        return icodes.size() - 1;
    }

    //-------------------------------------------------------------------------
    private String temp_marker()
    {
        return String.format("%s.%d", func_name, ++temp_idx);        
    }
}
