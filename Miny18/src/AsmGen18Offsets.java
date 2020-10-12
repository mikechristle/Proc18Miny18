//-----------------------------------------------------------------------------
// Miny Asm Code Generator, Assign Memory Offsets
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/16/2020   Add NOP command
//                      Change NOP ICodeId to NONE to support NOP command
// 1.2.0   10/12/2020   Fix bug in boolean register allocation
//-----------------------------------------------------------------------------
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
//-----------------------------------------------------------------------------

import java.util.HashMap;

//-----------------------------------------------------------------------------
public class AsmGen18Offsets
{
    public int[] rom = new int[1096];
    public int rom_offset = 0;
    public int reg_offset = 2;
    public int ram_offset = 0;
    public int bit_offset = 1;

    private HashMap<Integer, Integer> temp_regs = new HashMap<>();
    private HashMap<Integer, Integer> temp_bits = new HashMap<>();

    //-------------------------------------------------------------------------
    public void scan_symbols(HashMap<String, Symbol> symbols) throws MError
    {
        for (String skey : symbols.keySet())
        {
            Symbol sym = symbols.get(skey);

            if (sym.type == TypeId.BOOL && sym.count > 1)
                throw new MError("M32 does not support bool arrays");

            if (sym.store == StoreId.RAM)
            {
                sym.offset = ram_offset;
                ram_offset += sym.count;
            }
            else if (sym.store == StoreId.REG)
            {
                sym.offset = reg_offset;
                reg_offset += sym.count;
            }
            else if (sym.store == StoreId.BIT && sym.offset <= 0)
            {
                sym.offset = bit_offset;
                bit_offset += 1;
            }
        }
    }

    //-------------------------------------------------------------------------
    public void scan_func(Func func) throws MError
    {
        String label;
        Symbol symbol;
        ICode icode, ic1, ic2;
        int val, i;

        int last = func.icodes.size();
        for (String skey : func.symbols.keySet())
        {
            Symbol sym = func.symbols.get(skey);
            switch (sym.type)
            {
                case INT:
                case RAMP:
                case ROMP:
                    temp_regs.put(--reg_offset, last);
                    sym.offset = reg_offset;
                    break;

                case BOOL:
                    temp_bits.put(--bit_offset, last);
                    sym.offset = bit_offset;
                    break;
            }
        }

        for (i = 0; i < func.icodes.size(); i++)
        {
            icode = func.icodes.get(i);
            switch (icode.id)
            {
                case NONE:   // id
                case MARKER: // id > Marker
                case JMP:    // id > Marker
                case HALT:   // id
                case RESTART:// id
                case RESET:  // id
                case PAUSE:  // id
                case NOP:    // id
                case LEVEL:  // id > Value
                case BCON:   // id > Value
                case ICON:   // id > Value
                case SCON:   // id > Value
                case PARM:   // Id > Label, Expr
                case TIMER:  // id, Expr
                    break;

                case ASSIGNB: // Id, Dst, Src
                    icode.offset = -1;
                    break;

                case ASSIGNI: // Id, Dst, Src
                    icode.offset = -1;
                    ic1 = func.icodes.get(icode.p1);
                    ic2 = func.icodes.get(icode.p2);
                    if (icode.parm == MathOp.PAS && 
                        ic2.id == ICodeId.IN &&
                        ic1.id == ICodeId.REG)
                    {
                        ic2.offset = ic1.offset;
                        ic1.id = ICodeId.NONE;
                        icode.id = ICodeId.NONE;
                    }
                    break;

                case CALL:   // id > FuncLabel
                    if (func.isr)
                        throw new MError("ISR can't make calls", icode.src);
                    Func call_func = Module.find_func((String)icode.parm);
                    if (call_func.type != TypeId.NONE)
                        icode.offset = -1;
                    break;

                case JMPT:   // id > Marker, Expr
                case JMPF:   // id > Marker, Expr
                    ic1 = func.icodes.get(icode.p1);
                    if (ic1.id == ICodeId.COMPI)
                        ic1.offset = 0;
                    break;

                case COMPB:// id > MathOp, Op1, Op2
                    break;

                case COMPI:// id > MathOp, Op1, Op2
                    icode.offset = temp_bit(i, icode.last_ref);
                    ic1 = func.icodes.get(icode.p1);
                    switch (ic1.id)
                    {
                        case ICON:
                        case SCON:
                            ic1.offset = temp_bit(i, icode.last_ref);
                            break;
                    }
                    break;

                case BIT:    // id > Label
                    label = (String)icode.parm;
                    symbol = Module.find_symbol(func, label);
                    if (symbol.offset == 0)
                        symbol.offset = temp_bit(i, icode.last_ref);
                    icode.offset = symbol.offset;
                    break;

                case REG:  // id > Label
                    label = (String)icode.parm;
                    symbol = Module.find_symbol(func, label);
                    if (symbol.offset == 0)
                        symbol.offset = temp_reg(i, icode.last_ref);
                    icode.offset = symbol.offset;
                    break;

                case RAM:  // id > Label
                case ROM:  // id > Label
                    label = (String)icode.parm;
                    symbol = Module.find_symbol(func, label);
                    icode.offset = symbol.offset;
                    break;

                case IN:     // id > Label
                    label = (String)icode.parm;
                    symbol = Module.find_symbol(func, label);
                    icode.offset = temp_reg(i, icode.last_ref);
                    icode.p1 = symbol.offset;
                    break;

                case OUT:     // id > Label
                    label = (String)icode.parm;
                    symbol = Module.find_symbol(func, label);
                    icode.offset = temp_reg(i, icode.last_ref);
                    icode.p1 = symbol.offset;
                    break;

                case RETURN: // id, Expr
                    if (icode.p1 >= 0)
                    {
                        symbol = Module.find_symbol(func, "return");
                        if (symbol.type == TypeId.BOOL)
                            icode.offset = temp_bit(i, icode.last_ref);
                        else
                            icode.offset = temp_reg(i, icode.last_ref);
                        symbol.offset = icode.offset;
                    }
                    break;

                case MATHB:   // id > MathOp, Op1, Op2
                    ic1 = func.icodes.get(icode.p1);
                    if (ic1.offset > 0 && ic1.last_ref <= i)
                    {
                        icode.offset = ic1.offset;
                        temp_bits.put(icode.offset, icode.last_ref);
                    }
                    else
                        icode.offset = temp_bit(i, icode.last_ref);
                    break;

                case MATHI:   // id > MathOp, Op1, Op2
                    ic1 = func.icodes.get(icode.p1);
                    if (ic1.offset > 0 && ic1.last_ref <= i)
                    {
                        icode.offset = ic1.offset;
                        temp_regs.put(icode.offset, icode.last_ref);
                    }
                    else
                        icode.offset = temp_reg(i, icode.last_ref);
                    break;

                case ADDRESS:// id > Label, Expr
                case ARRAY:  // id > Label, Expr
                    ic1 = func.icodes.get(icode.p1);
                    if (ic1.last_ref == i &&
                       (ic1.id == ICodeId.MATHI || ic1.id == ICodeId.ARRAY))
                       {
                            icode.offset = ic1.offset;
                            temp_regs.put(icode.offset, icode.last_ref);
                       }
                    else
                        icode.offset = temp_reg(i, icode.last_ref);
                    break;

                default:
                    throw new MError("Invalid ICode: " + icode.id.toString());
            }
        }

        func.reg_cnt += temp_regs.size();
        func.bit_cnt += temp_bits.size();

        temp_regs.clear();
        temp_bits.clear();
    }

    //-------------------------------------------------------------------------
    private int temp_bit(int idx, int last_ref) throws MError
    {
        for (int ikey : temp_bits.keySet())
        {
            if (temp_bits.get(ikey) <= idx)
            {
                temp_bits.put(ikey, last_ref);
                return ikey;
            }
        }

        temp_bits.put(--bit_offset, last_ref);
        return bit_offset;
    }

    //-------------------------------------------------------------------------
    private int temp_reg(int idx, int last_ref) throws MError
    {
        for (int ikey : temp_regs.keySet())
        {
            if (temp_regs.get(ikey) < idx)
            {
                temp_regs.put(ikey, last_ref);
                return ikey;
            }
        }

        temp_regs.put(--reg_offset, last_ref);
        return reg_offset;
    }
}
