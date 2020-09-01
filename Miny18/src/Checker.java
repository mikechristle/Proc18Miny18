//-----------------------------------------------------------------------------
// Miny Checker
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

import java.util.HashMap;

//-----------------------------------------------------------------------------
public class Checker
{
    private HashMap<String, Symbol> func_syms;
    private HashMap<String, Symbol> mod_syms;
    private String module_name;

    //-------------------------------------------------------------------------
    public void check() throws MError
    {
        for (String mkey : Module.modules.keySet())
        {
            Module module = Module.modules.get(mkey);
            mod_syms = module.symbols;
            module_name = mkey;

            for (String fkey : module.funcs.keySet())
            {
                Func func = module.funcs.get(fkey);
                func_syms = func.symbols;

                if (func.isr && func.parms.size() != 0)
                    throw new MError("ISR can't have parameters", func.src);
                
                for (Node node : func.nodes)
                    check_node(node);
            }
        }
    }

    //-------------------------------------------------------------------------
    private TypeId check_node(Node node) throws MError
    {
        switch (node.id)
        {
            case BREAK: return TypeId.NONE;
            case CONTINUE: return TypeId.NONE;
            case HALT: return TypeId.NONE;
            case PAUSE: return TypeId.NONE;
            case LEVEL: return TypeId.NONE;
            case RESET: return TypeId.NONE;
            case RESTART: return TypeId.NONE;

            case BCON:
                node.type = TypeId.BOOL;
                return TypeId.BOOL;
            case ICON:
                node.type = TypeId.INT;
                return TypeId.INT;
            case SCON:
                node.type = TypeId.INT;
                return TypeId.INT;

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

        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private TypeId const_node(Node node) throws MError
    {
        BlockNode bnode = (BlockNode)node;
        for (Node n : bnode.nodes)
        {
            TypeId type = check_node(n);
            if (type != TypeId.INT)
                throw new MError("Type error", node.src);
        }

        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private TypeId call_node(Node node) throws MError
    {
        BlockNode bnode = (BlockNode)node;
        String label = (String)bnode.value;
        Func func = find_func(label, node.src);

        if (func.isr)
            throw new MError("Call to ISR", node.src);

        if (func.parms.size() != bnode.nodes.size())
            throw new MError("Parameter count error", node.src);

        for (int i = 0; i < func.parms.size(); i++)
        {
            TypeId type = check_node(bnode.nodes.get(i));
            if (type != func.parms.get(i).type)
                throw new MError("Parameter type error", node.src);
        }

        node.type = func.type;
        return func.type;
    }

    //-------------------------------------------------------------------------
    private Func find_func(String label, Src src) throws MError
    {
        int idx = label.indexOf('.');
        if (idx < 0)
        {
            label = module_name + '.' + label;
            Module module = Module.modules.get(module_name);
            if (module.funcs.containsKey(label))
                return module.funcs.get(label);
        }
        else
        {
            String mod_name = label.substring(0, idx);
            if (Module.modules.containsKey(mod_name))
            {
                Module module = Module.modules.get(mod_name);
                if (module.funcs.containsKey(label))
                    return module.funcs.get(label);
            }
        }

        String msg = String.format("Function not declared: %s", label);
        throw new MError(msg, src);
    }

    //-------------------------------------------------------------------------
    private TypeId array_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        String label = (String)snode.value;
        Symbol symbol = find_symbol(label, node.src);

        TypeId type = check_node(snode.p1);
        if (type != TypeId.INT)
            throw new MError("Array index is not int", node.src);

        node.type = symbol.type;
        return TypeId.INT;
    }

    //-------------------------------------------------------------------------
    private TypeId timer_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        TypeId type = check_node(snode.p1);
        if (type != TypeId.INT)
            throw new MError("Timer value is not int", node.src);

        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private TypeId return_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        if (snode.type != TypeId.NONE)
        {
            TypeId type = check_node(snode.p1);
            if (type != snode.type)
                throw new MError("Timer value is not int", snode.src);
        }

        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private TypeId mathop_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        TypeId type1 = check_node(snode.p1);
        TypeId type2 = TypeId.NONE;
        if (snode.p2 != null)
            type2 = check_node(snode.p2);

        switch ((MathOp)snode.value)
        {
            case ADD:
            case SUB:
            case MUL:
            case DIV:
            case MOD:
            case SHR:
            case SHL:
            case BW_AND:
            case BW_OR:
            case BW_XOR:
                if (type1 != TypeId.INT || type2 != TypeId.INT)
                    throw new MError("Arithmetic type error", node.src);
                break;

            case NEG:
            case BW_NOT: 
                if (type1 != TypeId.INT)
                    throw new MError("Arithmetic type error", node.src);
                break;

            case LG_AND:
            case LG_OR:
                if (type1 != TypeId.BOOL || type2 != TypeId.BOOL)
                    throw new MError("Arithmetic type error", node.src);
                break;

            case LG_NOT:
                if (type1 != TypeId.BOOL)
                    throw new MError("Arithmetic type error", node.src);
                break;
        }

        node.type = type1;
        return type1;
    }

    //-------------------------------------------------------------------------
    private TypeId compare_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        TypeId type1 = check_node(snode.p1);
        TypeId type2 = check_node(snode.p2);

        if (type1 != type2)
            throw new MError("Comparison type error", node.src);

        node.type = TypeId.BOOL;
        return TypeId.BOOL;
    }

    //-------------------------------------------------------------------------
    private TypeId block_node(Node node) throws MError
    {
        BlockNode bnode = (BlockNode)node;
        for (Node n : bnode.nodes)
            check_node(n);

        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private TypeId loop_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        check_node(snode.p1);
        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private TypeId if_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        TypeId type1 = check_node(snode.p1);

        if (type1 != TypeId.BOOL)
            throw new MError("If expression must be boolean", node.src);

        check_node(snode.p2);
        if (snode.p3 != null)
            check_node(snode.p3);

        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private TypeId assign_node(Node node) throws MError
    {
        StmtNode snode = (StmtNode)node;
        TypeId type1 = check_node(snode.p1);
        TypeId type2 = check_node(snode.p2);

        if (type1 != type2)
            throw new MError("Type error for assignment", node.src);

        node.type = type1;
        return TypeId.NONE;
    }

    //-------------------------------------------------------------------------
    private TypeId label_node(Node node) throws MError
    {
        String label = (String)node.value;
        Symbol sym = find_symbol(label, node.src);

        if (sym.store == StoreId.PORT) node.id = NodeId.PORT;

        node.type = sym.type;
        return sym.type;
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
