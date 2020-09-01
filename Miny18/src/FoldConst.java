//-----------------------------------------------------------------------------
// Miny Fold Constants
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.LinkedList;

//-----------------------------------------------------------------------------
public class FoldConst
{
    private HashMap<String, Symbol> func_syms;
    private HashMap<String, Symbol> mod_syms;
    private TreeMap<Character, Character> escapes = new TreeMap<>();

    //-------------------------------------------------------------------------
    public FoldConst()
    {
        escapes.put('n', '\n'); // Newline
        escapes.put('r', '\r'); // Carriage return
        escapes.put('t', '\t'); // Tab
        escapes.put('f', '\f'); // Form feed
        escapes.put('b', '\\'); // Backslash
    }

    //-------------------------------------------------------------------------
    public void fold() throws MError
    {
        for (String mkey : Module.modules.keySet())
        {
            Module module = Module.modules.get(mkey);
            mod_syms = module.symbols;

            Object[] keys = module.funcs.keySet().toArray();
            Arrays.sort(keys);

            for (Object fkey : keys)
            {
                Func func = module.funcs.get(fkey);
                func_syms = func.symbols;

                for (Node node : func.nodes)
                    fold_node(node);
            }

            module.funcs.remove(mkey);
        }
    }

    //-------------------------------------------------------------------------
    private Object fold_node(Node node) throws MError
    {
        switch (node.id)
        {
            case BREAK: return null;
            case CONTINUE: return null;
            case HALT: return null;
            case PAUSE: return null;
            case LEVEL: return null;
            case RESET: return null;

            case BCON: return node.bval();
            case ICON: return node.ival();
            case SCON: return node.sval();

            case BLOCK: return block_node(node);
            case LABEL: return label_node(node);
            case IF: return if_node(node);
            case LOOP: return loop_node(node);
            case CALL: return call_node(node);
            case CONST: return const_node(node);
            case ASSIGN: return assign_node(node);
            case COMPARE: return compare_node(node);
            case MATHOP: return mathop_node(node);
            case ARRAY: return array_node(node);
            case TIMER: return timer_node(node);
            case RETURN: return return_node(node);
        }

        return null;
    }

    //-------------------------------------------------------------------------
    private Object const_node(Node node) throws MError
    {
        BlockNode bnode = (BlockNode)node;
        String label = (String)bnode.value;
        LinkedList<Object> values = new LinkedList<>();
        Symbol symbol = find_symbol(label, node.src);

        for (Node n : bnode.nodes)
        {
            if (n.id == NodeId.SCON)
            {
                int v = 0;
                boolean hi_byte = true;
                String s = (String)(n.value);

                for (int i = 0; i < s.length(); i++)
                {
                    char c = s.charAt(i);
                    if (c == '\\')
                    {
                        i++;
                        c = s.charAt(i);
                        if (!escapes.containsKey(c))
                            throw new MError("Invalid escape sequence \\" + c);
                        c = escapes.get(c);
                    }
                    hi_byte = !hi_byte;
                    if (hi_byte)
                    {
                        v |= (int)c << 9;
                        values.add(v);
                    }
                    else
                    {
                        v = (int)c;
                    }
                }
                if (!hi_byte) values.add(v);
            }
            else
                values.add(fold_node(n));
        }

        symbol.values = values;
        symbol.count = values.size();
        return null;
    }

    //-------------------------------------------------------------------------
    private Object call_node(Node node) throws MError
    {
        BlockNode bnode = (BlockNode)node;

        for (Node n : bnode.nodes)
            fold_node(n);

        return null;
    }

    //-------------------------------------------------------------------------
    private Object array_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        fold_node(snode.p1);
        return null;
    }

    //-------------------------------------------------------------------------
    private Object timer_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        fold_node(snode.p1);
        return null;
    }

    //-------------------------------------------------------------------------
    private Object return_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        if (snode.type != TypeId.NONE) fold_node(snode.p1);
        return null;
    }

    //-------------------------------------------------------------------------
    private Object mathop_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;

        if (snode.p1.id == NodeId.BCON ||
            snode.p1.id == NodeId.ICON)
        {
            switch ((MathOp)snode.value)
            {
                case ADD:
                case MUL:
                case LG_AND:
                case LG_OR:
                case BW_AND:
                case BW_OR:
                case BW_XOR:
                    Node temp = snode.p1;
                    snode.p1 = snode.p2;
                    snode.p2 = temp;
                    break;
            }
        }

        Object value1 = fold_node(snode.p1);
        Object value2 = null;
        if (snode.p2 != null)
            value2 = fold_node(snode.p2);

        if (value1 == null) return null;
        if (snode.p1 != null && value2 == null) return null;

        Object value = null;
        switch (snode.type)
        {
            case BOOL:
                switch ((MathOp)snode.value)
                {
                    case LG_AND: 
                        value = (boolean)value1 && (boolean)value2;
                        break;
                    case LG_OR:
                        value = (boolean)value1 || (boolean)value2;
                        break;
                    case LG_NOT:
                        value = !(boolean)value1;
                        break;
                }
                node.id = NodeId.BCON;
                node.value = value;
                return value;

            case INT:
                switch ((MathOp)snode.value)
                {
                    case ADD: value = (int)value1 + (int)value2; break;
                    case SUB: value = (int)value1 - (int)value2; break;
                    case MUL: value = (int)value1 * (int)value2; break;
                    case DIV: value = (int)value1 / (int)value2; break;
                    case MOD: value = (int)value1 % (int)value2; break;
                    case SHR: value = (int)value1 >> (int)value2; break;
                    case SHL: value = (int)value1 << (int)value2; break;
                    case NEG: value = -(int)value1; break;

                    case BW_AND: value = (int)value1 & (int)value2; break;
                    case BW_OR:  value = (int)value1 | (int)value2; break;
                    case BW_XOR: value = (int)value1 ^ (int)value2; break;
                    case BW_NOT: value = ~(int)value1; break;
                }
                node.id = NodeId.ICON;
                node.value = value;
                return value;
        }

        return null;
    }

    //-------------------------------------------------------------------------
    private Object compare_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        Object value1 = fold_node(snode.p1);
        Object value2 = fold_node(snode.p2);
        MathOp op = (MathOp)snode.value;

        // If const or LHS, then swap
        if (value1 != null && value2 == null)
        {
            Node temp = snode.p1;
            snode.p1 = snode.p2;
            snode.p2 = temp;
            Object o = value1;
            value1 = value2;
            value2 = o;

            switch ((MathOp)snode.value)
            {
                case GT: snode.value = MathOp.LT; break;
                case LT: snode.value = MathOp.GT; break;
                case GE: snode.value = MathOp.LE; break;
                case LE: snode.value = MathOp.GE; break;
            }
        }

        boolean bval = false;
        switch (snode.p1.type)
        {
            case BOOL:
                if (value1 != null && value2 != null)
                {
                    switch (op)
                    {
                        case EQ: 
                            bval = (boolean)value1 == (boolean)value2;
                            break;
                        case NE:
                            bval = (boolean)value1 != (boolean)value2;
                            break;
                    }
                }
                else if (snode.p2.id == NodeId.BCON)
                {
                    if (op == MathOp.EQ &&  (boolean)value2 ||
                        op == MathOp.NE && !(boolean)value2)
                    {
                        if (snode.p1.id == NodeId.LABEL ||
                            snode.p1.id == NodeId.PORT)
                        {
                            snode.id = snode.p1.id;
                            snode.value = snode.p1.value;
                            snode.type = TypeId.BOOL;
                        }
                        else
                        {
                            StmtNode sn = (StmtNode)snode.p1;
                            snode.id = sn.id;
                            snode.value = sn.value;
                            snode.type = sn.type;
                            snode.p3 = sn.p3;
                            snode.p2 = sn.p2;
                            snode.p1 = sn.p1;
                        }
                    }
                    else
                    {
                        snode.id = NodeId.MATHOP;
                        snode.value = MathOp.LG_NOT;
                        snode.type = TypeId.BOOL;
                        snode.p3 = null;
                        snode.p2 = null;
                    }
                    return null;
                }
                else return null;
                break;

            case INT:
                if (value1 == null || value2 == null) return null;
                switch (op)
                {
                    case EQ: bval = (int)value1 == (int)value2; break;
                    case NE: bval = (int)value1 != (int)value2; break;
                    case GT: bval = (int)value1 >  (int)value2; break;
                    case LT: bval = (int)value1 <  (int)value2; break;
                    case GE: bval = (int)value1 >= (int)value2; break;
                    case LE: bval = (int)value1 <= (int)value2; break;
                }
                break;

            default:
                return null;
        }

        node.id = NodeId.BCON;
        node.value = bval;
        return bval;
    }

    //-------------------------------------------------------------------------
    private Object block_node(Node node) throws MError
    {
        BlockNode bnode = (BlockNode)node;
        for (Node n : bnode.nodes)
            fold_node(n);

        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private Object loop_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        fold_node(snode.p1);
        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private Object if_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        fold_node(snode.p1);
        fold_node(snode.p2);
        if (snode.p3 != null)
            fold_node(snode.p3);

        return null;
    }

    //-------------------------------------------------------------------------
    private Object assign_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        fold_node(snode.p1);
        fold_node(snode.p2);

        if ((snode.p1.id == NodeId.LABEL || snode.p1.id == NodeId.PORT) &&
            (snode.p2.id == NodeId.MATHOP))
            {
                StmtNode sp2 = (StmtNode)snode.p2;
                if (snode.p1.id == sp2.p1.id)
                {
                    String s1 = (String)snode.p1.value;
                    String s2 = (String)sp2.p1.value;
                    if (s1.equals(s2))
                    {
                        snode.value = snode.p2.value;
                        if (sp2.p2 == null)
                            snode.p2 = sp2.p1;
                        else
                            snode.p2 = sp2.p2;
                    }
                }
            }

        return null;
    }

    //-------------------------------------------------------------------------
    private Object label_node(Node node) throws MError
    {
        String label = (String)node.value;
        Symbol sym = find_symbol(label, node.src);

        if (sym.store == StoreId.ROM && sym.count == 1 && sym.values != null)
            return sym.values.get(0);

        return null;
    }

    //-------------------------------------------------------------------------
    private Symbol find_symbol(String label, Src src) throws MError
    {
        int idx = label.indexOf('.');

        if (idx < 0)
        {
            if (func_syms.containsKey(label))
                return func_syms.get(label);

            if (mod_syms.containsKey(label))
                return mod_syms.get(label);
        }
        else
        {
            String mod_name = label.substring(0, idx);
            label = label.substring(idx + 1, label.length());
            if (Module.modules.containsKey(mod_name))
            {
                Module module = Module.modules.get(mod_name);
                if (module.symbols.containsKey(label))
                    return module.symbols.get(label);
            }
        }

        String msg = String.format("Label not declared: %s", label);
        throw new MError(msg, src);
    }
}
