//-------------------------------------------------------------------
// Asm Code Generator
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/14/2020   Add hardware config command
// 1.2.0   09/16/2020   Add NOP command
//                      Change NOP ICodeId to NONE to support NOP command
// 1.2.0   10/12/2020   Fix bug in boolean register allocation
//-------------------------------------------------------------------
// Copyright 2020 Mike Christle
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//-------------------------------------------------------------------

import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;
import java.util.LinkedList;

//-------------------------------------------------------------------
public class AsmGen18
{
    private LinkedList<ICode> icodes;
    private TreeSet<String> func_names = new TreeSet<>();
    private TreeSet<String> module_names = new TreeSet<>();
    private LinkedList<CallTree> to_do = new LinkedList<>();
    private LinkedList<Func> funcs_to_do = new LinkedList<>();

    private AsmGen18Offsets asm_offsets = new AsmGen18Offsets();
    private FileWriter ofp;
    private String func_name;
    private Func func;
    private int reg_base, bit_base;

    //---------------------------------------------------------------
    public void run(String name) throws MError
    {
        try
        {
            ofp = new FileWriter(name + ".asm");

            write_consts();
            write_isr_vectors(name + ".main");

            CallTree call_tree = CallTree.build(name + ".main");

            to_do.add(call_tree);
            while (to_do.size() > 0)
            {
                CallTree tree_node = to_do.remove();
                int idx = tree_node.name.indexOf('.');
                String module_name = tree_node.name.substring(0, idx);
                Module module = Module.modules.get(module_name);

                if (module_names.add(module_name))
                    asm_offsets.scan_symbols(module.symbols);

                if (func_names.add(tree_node.name))
                {
                    func = get_func(tree_node.name);
                    funcs_to_do.add(func);

                    if (tree_node.tree != null)
                        for (CallTree tree : tree_node.tree)
                            to_do.add(tree);
                }
            }

            bit_base = asm_offsets.bit_offset;
            reg_base = asm_offsets.reg_offset;

            // Add ISR funcs to list
            for (int i = 1; i < 16; i++)
            {
                String label = Module.isr_labels[i];
                if (label != null)
                    funcs_to_do.add(get_func(label));
            }

            // Get func offsets
            for (Func fn : funcs_to_do)
            {
                asm_offsets.bit_offset = 0;
                asm_offsets.reg_offset = 0;
                asm_offsets.scan_func(fn);
            }

            // Set ISR base offsets
            for (int i = 1; i < 16; i++)
            {
                String label = Module.isr_labels[i];
                if (label != null)
                {
                    func = get_func(label);
                    func.bit_base = bit_base;
                    func.reg_base = reg_base;
                    bit_base += func.bit_cnt;
                    reg_base += func.reg_cnt;
                }
            }

            // Set func base offsets
            set_base_offsets(call_tree);

            // Update symbol tables
            for (Func fn : funcs_to_do)
                update_symbols(fn);

            // Set func base offsets
            for (Func fn : funcs_to_do)
                update_icodes(fn);

            // Gen code
            for (Func fn : funcs_to_do)
                gen_code(fn);

            ofp.close();

            // CallTree.dump_tree(call_tree, 0);
        }
        catch (IOException e)
        {
            throw new MError("Error writing output file");
        }
    }

    //---------------------------------------------------------------
    private void write_isr_vectors(String name) throws IOException
    {
        ofp.write("    ORG     0\n");
        for (int i = 0; i < 16; i++)
        {
            String label = Module.isr_labels[i];
            if (label != null)
                emit1("JMP", label);
            else
                emit1("JMP", name);
        }        
        ofp.write("\n");
    }

    //---------------------------------------------------------------
    private void update_icodes(Func func) throws MError
    {
        for (ICode icode : func.icodes)
        {
            if (icode.offset < 0)
            {
                switch (icode.id)
                {
                    case BCON:
                    case BIT:
                    case MATHB:
                    case COMPI:
                        icode.offset = func.bit_base - icode.offset - 1;
                        break;

                    case CALL:
                        Func call_func = Module.find_func((String)icode.parm);
                        if (call_func.type != TypeId.NONE)
                            icode.offset = call_func.symbols.get("return").offset;
                        break;

                    case ASSIGNB:
                        ICode ic1 = func.icodes.get(icode.p1);
                        ICode ic2 = func.icodes.get(icode.p2);
                        if (ic2.id == ICodeId.COMPB || 
                            ic2.id == ICodeId.COMPI)
                        {
                            ic2.offset = ic1.offset;
                        }
                        break;

                    default:
                        icode.offset = func.reg_base - icode.offset - 1;
                        break;
                }
            }
        }
    }

    //---------------------------------------------------------------
    private void update_symbols(Func func)
    {
        for (String skey : func.symbols.keySet())
        {
            Symbol sym = func.symbols.get(skey);
            switch (sym.type)
            {
                case INT:
                case RAMP:
                case ROMP:
                    sym.offset = func.reg_base - sym.offset - 1;
                    break;

                case BOOL:
                    sym.offset = func.bit_base - sym.offset - 1;
                    break;
            }
        }
    }

    //---------------------------------------------------------------
    private void set_base_offsets(CallTree tree_node)
    {
        Func func = Module.find_func(tree_node.name);
        if (func.bit_base < bit_base) func.bit_base = bit_base;
        if (func.reg_base < reg_base) func.reg_base = reg_base;

        bit_base += func.bit_cnt;
        reg_base += func.reg_cnt;

        if (tree_node.tree != null)
            for (CallTree tree : tree_node.tree)
                set_base_offsets(tree);

        bit_base -= func.bit_cnt;
        reg_base -= func.reg_cnt;
    }

    //---------------------------------------------------------------
    private void write_consts() throws IOException, MError
    {
        int rom_offset = 0;

        ofp.write(String.format("    CONFIG  %d %d %d\n",
                  Module.rom_bits, Module.ram_bits, Module.con_bits));

        ofp.write("    ORG     0\n");
        for (String mkey : Module.modules.keySet())
        {
            Module mod = Module.modules.get(mkey);
            for (String skey : mod.symbols.keySet())
            {
                Symbol sym = mod.symbols.get(skey);
                if (sym.store == StoreId.ROM && sym.count > 1)
                {
                    sym.offset = rom_offset;
                    rom_offset += sym.count;
                    ofp.write("    DC      ");
                    for (Object value : sym.values)
                        ofp.write(String.format("%d ", value));
                    ofp.write('\n');
                }
            }
        }
        ofp.write('\n');

        if (rom_offset > (1 << Module.con_bits))
            throw new MError("Constants ROM too small");
    }

    //---------------------------------------------------------------
    private void gen_code(Func fn) throws IOException, MError
    {
        ICode ic1, ic2;
        Symbol sym;
        String str;
        int ival;
        MathOp mop;
        float rval;

        func = fn;

        ofp.write(";----------------------\n");
        ofp.write("; " + func.label + '\n');
        ofp.write(";----------------------\n");

        for (int idx = 0; idx < func.icodes.size(); idx++)
        {
            ICode icode = func.icodes.get(idx);
            switch (icode.id)
            {
                case BCON:   // id > Value
                    if (icode.offset > 0)
                    {
                        if ((boolean)icode.parm)
                            emit1("BSET", icode.offset);
                        else
                            emit1("BCLR", icode.offset);
                    }
                    break;

                case ICON:   // id > Value
                case SCON:   // id > Value
                    if (icode.offset > 0)
                    {
                        emit2i("MOV", icode.offset, (int)icode.parm);
                    }
                    break;

                case RAM:    // id > Label
                case ROM:    // id > Label
                case REG:    // id > Label
                    break;

                case IN:     // id > Label
                    sym = Module.find_symbol(func, (String)icode.parm);
                    if (sym.type != TypeId.BOOL)
                        emit2("IN", icode.offset, sym.offset);
                    break;

                case MARKER: // id > Marker
                    ofp.write((String)icode.parm + '\n');
                    break;

                case HALT:    emit0("HALT\n"); break;
                case PAUSE:   emit0("PAUSE\n"); break;
                case NOP:     emit0("NOP\n"); break;
                case RESET:   emit0("RESET\n"); break;
                case RESTART: emit0("RESTART\n"); break;

                case JMPT:   // id > Marker, Expr
                    ic1 = func.icodes.get(icode.p1);
                    str = (String)icode.parm;
                    jmpt_icode(icode, ic1, str);
                    break;

                case JMPF:   // id > Marker, Expr
                    ic1 = func.icodes.get(icode.p1);
                    str = (String)icode.parm;
                    jmpf_icode(icode, ic1, str);
                    break;

                case JMP:    // id > Marker
                    str = (String)icode.parm;
                    emit1("JMP", str);
                    break;

                case CALL:   // id > FuncLabel
                    emit1("CALL", (String)icode.parm);
                    break;

                case PARM:
                    ic1 = func.icodes.get(icode.p1);
                    parm_icode(icode, ic1);
                    break;

                case LEVEL:  // id > Value
                    emit1("LEVEL", String.format("#%d\n", ((int)icode.parm)));
                    break;

                case RETURN: // id, Expr
                    if (icode.p1 >= 0)
                    {
                        ic1 = func.icodes.get(icode.p1);
                        sym = Module.find_symbol(func, "return");
                        return_icode(icode, ic1, sym);
                    }
                    if (func.isr) emit0("RTI");
                    else          emit0("RTS");
                    ofp.write('\n');
                    break;

                case TIMER: // id, Expr
                    ic1 = func.icodes.get(icode.p1);
                    if (ic1.id == ICodeId.ICON)
                        emit1("TIMER", String.format("#%d", (int)ic1.parm));
                    else
                        emit1("TIMER", ic1.offset);
                    break;

                case COMPB:// id > MathOp, Op1, Op2
                    ic1 = func.icodes.get(icode.p1);
                    ic2 = func.icodes.get(icode.p2);
                    compb_icode(icode, ic1, ic2);
                    break;

                case COMPI:// id > MathOp, Op1, Op2
                    ic1 = func.icodes.get(icode.p1);
                    ic2 = func.icodes.get(icode.p2);
                    comp_icode(icode, ic1, ic2);
                    break;

                case ASSIGNB: // Id, Dst, Src
                    ic1 = func.icodes.get(icode.p1);
                    ic2 = func.icodes.get(icode.p2);
                    assign_bool_icode(icode, ic1, ic2);
                    break;

                case ASSIGNI: // Id, Dst, Src
                    ic1 = func.icodes.get(icode.p1);
                    ic2 = func.icodes.get(icode.p2);
                    switch (ic1.id)
                    {
                        case REG:
                            assign_label_icode(icode, ic1, ic2);
                            break;
                        case ADDRESS:
                            assign_array_icode(icode, ic1, ic2);
                            break;
                        case OUT:
                            assign_port_icode(icode, ic1, ic2);
                            break;
                    }
                    break;

                case MATHB:   // id > MathOp, Op1, Op2
                    ic1 = func.icodes.get(icode.p1);
                    if (icode.p2 >= 0)
                        ic2 = func.icodes.get(icode.p2);
                    else
                        ic2 = null;
                    mathb_icode(icode, ic1, ic2);
                    break;

                case MATHI:   // id > MathOp, Op1, Op2
                    ic1 = func.icodes.get(icode.p1);
                    if (icode.p2 >= 0)
                        ic2 = func.icodes.get(icode.p2);
                    else
                        ic2 = null;
                    mathi_icode(icode, ic1, ic2);
                    break;

                case ADDRESS:// id > Label, Expr
                    ic1 = func.icodes.get(icode.p1);
                    address_icode(icode, ic1, false);
                    break;

                case ARRAY:  // id > Label, Expr
                    ic1 = func.icodes.get(icode.p1);
                    address_icode(icode, ic1, true);
                    break;

                case NONE: break;
            }
        }
    }

    //---------------------------------------------------------------
    private void return_icode(ICode icode, ICode ic1, Symbol sym) throws IOException
    {
        switch (ic1.id)
        {
            case ICON:
            case SCON:
                emit2i("MOV", sym.offset, (int)ic1.parm);
                break;

            case BCON:
                if ((int)ic1.parm == 0)
                    emit1("BCLR", sym.offset);
                else
                    emit1("BSET", sym.offset);
                break;

            default:
                emit2("MOV", sym.offset, ic1.offset);
                break;
        }
    }

    //---------------------------------------------------------------
    private void compb_icode(ICode icode, ICode ic1, ICode ic2) throws IOException
    {
        emit2("BCMP", ic1.offset, ic2.offset);

        if (icode.offset > 0)
        {
            String str = 'S' + ((MathOp)icode.parm).toString();
            emit1(str, String.format("%d", icode.offset));
        }
    }

    //---------------------------------------------------------------
    private void comp_icode(ICode icode, ICode ic1, ICode ic2) throws IOException
    {
        String str;
        int ival;
        String op = "CMP";

        if (ic2.offset == 0)
            emit2i(op, ic1.offset, (int)ic2.parm);
        else
            emit2(op, ic1.offset, ic2.offset);

        if (icode.offset > 0)
        {
            str = 'S' + ((MathOp)icode.parm).toString();
            emit1(str, String.format("%d", icode.offset));
        }
    }

    //---------------------------------------------------------------
    private void parm_icode(ICode icode, ICode ic1) throws IOException, MError
    {
        String label = (String)icode.parm;
        String [] parts = label.split("\\.");
        Module mod = Module.modules.get(parts[0]);
        Func func = mod.funcs.get(parts[0] + '.' + parts[1]);
        Symbol sym = func.symbols.get(parts[2]);

        switch (ic1.id)
        {
            case BCON:
                if ((boolean)ic1.parm)
                    emit1("BSET", sym.offset);
                else
                    emit1("BCLR", sym.offset);
                break;

            case ICON:
                emit2i("MOV", sym.offset, (int)ic1.parm);
                break;

            case BIT:
            case COMPB:
            case COMPI:
                emit2("BMOV", sym.offset, ic1.offset);
                break;

            case RAM:
            case ROM:
                emit2i("MOV", sym.offset, ic1.offset);
                break;

            default:
                emit2("MOV", sym.offset, ic1.offset);
                break;
        }
    }

    //---------------------------------------------------------------
    private void jmpt_icode(ICode icode, ICode ic1, String marker)
        throws IOException
    {
        if (ic1.id == ICodeId.COMPB ||
            ic1.id == ICodeId.COMPI)
        {
            String str;
            switch ((MathOp)ic1.parm)
            {
                case EQ: str = "JEQ"; break;
                case NE: str = "JNE"; break;
                case GT: str = "JGT"; break;
                case GE: str = "JGE"; break;
                case LT: str = "JLT"; break;
                default: str = "JLE"; break;
            }
            emit1(str, marker);
        }
        else // ICodeId.LABEL || ICodeId.MATHB
        {
            emit2("BNOT", 0, ic1.offset);
            emit1("JEQ", marker);
        }
        ofp.write('\n');
    }

    //---------------------------------------------------------------
    private void jmpf_icode(ICode icode, ICode ic1, String marker)
        throws IOException
    {
        if (ic1.id == ICodeId.COMPB ||
            ic1.id == ICodeId.COMPI)
        {
            String str;
            switch ((MathOp)ic1.parm)
            {
                case EQ: str = "JNE"; break;
                case NE: str = "JEQ"; break;
                case GT: str = "JLE"; break;
                case GE: str = "JLT"; break;
                case LT: str = "JGE"; break;
                default: str = "JGT"; break;
            }
            emit1(str, marker);
        }
        else // ICodeId.LABEL || ICodeId.MATHB
        {
            emit2("BNOT", 0, ic1.offset);
            emit1("JNE", marker);
        }
        ofp.write('\n');
    }

    //---------------------------------------------------------------
    private void address_icode(ICode icode, ICode ic1, boolean load)
        throws IOException, MError
    {
        String str;
        String label = (String)icode.parm;

        Symbol sym = Module.find_symbol(func, label);

        if (ic1.id == ICodeId.REG)
            emit2("MOV", icode.offset, ic1.offset);

        else if (ic1.id == ICodeId.ICON)
            emit2i("MOV", icode.offset, (int)ic1.parm);

        switch (sym.store)
        {
            case REG:
                emit2("ADD", icode.offset, sym.offset);
                break;

            case RAM:
            case ROM:
                emit2i("ADD", icode.offset, sym.offset);
                break;
        }

        if (load)
        {
            str = String.format("%d, (%d)", icode.offset, icode.offset);
            if (sym.type == TypeId.ROMP)
                emit1("LDC", str);
            else
                emit1("LDR", str);
        }
    }

    //---------------------------------------------------------------
    private void assign_label_icode(ICode icode, ICode ic1, ICode ic2) throws IOException
    {
        int ival;
        String str, op;
        MathOp mop = (MathOp)icode.parm;

        switch(mop)
        {
            case PAS:    op = "MOV"; break;
            case BW_AND: op = "AND"; break;
            case BW_OR:  op = "OR"; break;
            case BW_XOR: op = "XOR"; break;
            case BW_NOT: op = "INV"; break;
            default:     op = mop.toString(); break;
        }

        switch (ic2.id)
        {
            case REG:
            case IN:
            case MATHI:
            case ARRAY:
            case COMPI:
            case CALL:
                emit2(op, ic1.offset, ic2.offset);
                break;

            case ADDRESS:
                str = String.format("%d, (%d)", ic1.offset, ic2.offset);
                emit1(op, str);
                break;

            case ICON:
            case SCON:
                emit2i(op, ic1.offset, (int)ic2.parm);
                break;
        }
        ofp.write('\n');
    }

    //---------------------------------------------------------------
    private void assign_array_icode(ICode icode, ICode ic1, ICode ic2) throws IOException
    {
        int ival;
        String str, op;

        MathOp mop = (MathOp)icode.parm;

        switch(mop)
        {
            case PAS:    op = "MOV"; break;
            case BW_AND: op = "AND"; break;
            case BW_OR:  op = "OR"; break;
            case BW_XOR: op = "XOR"; break;
            case BW_NOT: op = "INV"; break;
            default:     op = mop.toString(); break;
        }

        switch (ic2.id)
        {
            case REG:
            case IN:
            case MATHI:
            case ARRAY:
            case COMPI:
            case CALL:
                if (mop == MathOp.PAS)
                {
                    str = String.format("(%d), %d", ic1.offset, ic2.offset);
                    emit1("STR", str);
                }
                else
                {
                    str = String.format("1, (%d)", ic1.offset);
                    emit1("LDR", str);
                    emit2(op, 1, ic2.offset);
                    str = String.format("(%d), 1", ic1.offset);
                    emit1("STR", str);
                }
                break;

            case ICON:
            case SCON:
                if (mop == MathOp.PAS)
                {
                    emit2i("MOV", 1, (int)ic2.parm);
                    str = String.format("(%d), 1", ic1.offset);
                    emit1("STR", str);
                }
                else
                {
                    str = String.format("1, (%d)", ic1.offset);
                    emit1("LDR", str);
                    emit2i(op, 1, (int)ic2.parm);
                    str = String.format("(%d), 1", ic1.offset);
                    emit1("STR", str);
                }
                break;
        }
        ofp.write('\n');
    }

    //---------------------------------------------------------------
    private void assign_port_icode(ICode icode, ICode ic1, ICode ic2)
        throws IOException
    {
        int ival;
        String str, op = null;

        MathOp mop = (MathOp)icode.parm;
        if (mop != MathOp.PAS)
        {
            switch (mop)
            {
                case BW_AND: op = "AND"; break;
                case BW_OR:  op = "OR"; break;
                case BW_XOR: op = "XOR"; break;
                default:     op = mop.toString(); break;
            }
        }

        switch (ic2.id)
        {
            case REG:
            case IN:
            case MATHI:
            case ARRAY:
            case CALL:
                if (op != null)
                {
                    emit2("IN", 1, ic1.p1);
                    emit2(op, 1, ic2.offset);
                    emit2("OUT", ic1.p1, 1);
                }
                else
                    emit2("OUT", ic1.p1, ic2.offset);
                break;

            case ICON:
            case SCON:
                if (op != null)
                {
                    emit2("IN", 1, ic1.p1);
                    emit2i(op, 1, (int)ic2.parm);
                    emit2("OUT", ic1.p1, 1);
                }
                else
                {
                    emit2i("MOV", 1, (int)ic2.parm);
                    emit2("OUT", ic1.p1, 1);
                }
                break;
        }
        ofp.write('\n');
    }

    //---------------------------------------------------------------
    private void assign_bool_icode(ICode icode, ICode ic1, ICode ic2) throws IOException
    {
        String op = "BMOV";
        MathOp mop = (MathOp)icode.parm;
        switch (mop)
        {
            case LG_AND: op = "BAND"; break;
            case LG_OR:  op = "BOR";  break;
            case LG_NOT: op = "BNOT"; break;
        }

        switch (ic2.id)
        {
            case BCON:
                if ((boolean)ic2.parm)
                    emit1("BSET", ic1.offset);
                else
                    emit1("BCLR", ic1.offset);
                break;

            default:
                if (op != "BMOV" || ic1.offset != ic2.offset) 
                    emit2(op, ic1.offset, ic2.offset);
                break;
        }
        ofp.write('\n');
    }

    //---------------------------------------------------------------
    private void mathb_icode(ICode icode, ICode ic1, ICode ic2) throws IOException
    {
        String op;
        MathOp mop = (MathOp)icode.parm;

        switch (mop)
        {
            case LG_AND: op = "BAND"; break;
            case LG_OR:  op = "BOR";  break;
            default:     op = "BNOT"; break;
        }

        if (ic2 == null)
            emit2(op, icode.offset, icode.offset);

        else if (icode.offset == ic1.offset)
            emit2(op, icode.offset, ic2.offset);

        else if (icode.offset == ic2.offset)
            emit2(op, icode.offset, ic1.offset);

        else
        {
            emit2("BMOV", icode.offset, ic1.offset);
            emit2(op, icode.offset, ic2.offset);
        }
    }

    //---------------------------------------------------------------
    private void mathi_icode(ICode icode, ICode ic1, ICode ic2)
        throws IOException
    {
        String op;
        MathOp mop = (MathOp)icode.parm;

        switch (mop)
        {
            case BW_AND: op = "AND"; break;
            case BW_OR:  op = "OR"; break;
            case BW_XOR: op = "XOR"; break;
            case BW_NOT: op = "INV"; break; 

            default: op = mop.toString(); break;
        }

        if (icode.offset != ic1.offset)
        {
            switch (ic1.id)
            {
                case ICON:
                    emit2i("MOV", icode.offset, (int)ic1.parm);
                    break;
                case SCON:
                    emit2i("MOV", icode.offset, ic1.offset);
                    break;
                default:
                    emit2("MOV", icode.offset, ic1.offset);
                    break;
            }
        }

        if (ic2 == null)
            emit2(op, icode.offset, icode.offset);
        else
        {
            switch (ic2.id)
            {
                case ICON:
                    emit2i(op, icode.offset, (int)ic2.parm);
                    break;
                case SCON:
                    emit2i(op, icode.offset, ic2.offset);
                    break;
                default:
                    emit2(op, icode.offset, ic2.offset);
                    break;
            }
        }
    }

    //---------------------------------------------------------------
    private void emit0(String op) throws IOException
    {
        String str = String.format("    %-6s\n", op);
        ofp.write(str);
    }

    //---------------------------------------------------------------
    private void emit1(String op, String p1) throws IOException
    {
        String str = String.format("    %-6s  %s\n", op, p1);
        ofp.write(str);
    }

    //---------------------------------------------------------------
    private void emit1(String op, int p1) throws IOException
    {
        String str = String.format("    %-6s  %d\n", op, p1);
        ofp.write(str);
    }

    // -------------------------------------------------------------------------
    private void emit2(String op, String p1, String p2) throws IOException
    {
        String str = String.format("    %-6s  %s, %s\n", op, p1, p2);
        ofp.write(str);
    }

    // -------------------------------------------------------------------------
    private void emit2(String op, int p1, int p2) throws IOException
    {
        String str = String.format("    %-6s  %d, %d\n", op, p1, p2);
        ofp.write(str);
    }

    // -------------------------------------------------------------------------
    private void emit2i(String op, int p1, int p2) throws IOException
    {
        String str = String.format("    %-6s  %d, #%d\n", op, p1, p2);
        ofp.write(str);
    }

    //---------------------------------------------------------------
    private Func get_func(String name)
    {
        String[] sa = name.split("\\.");
        Module module = Module.modules.get(sa[0]);
        return module.funcs.get(name);
    }
}
