//-----------------------------------------------------------------------------
// Miny Assembler
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/13/2020   Add hardware config directive
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

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

//-----------------------------------------------------------------------------
public class Miny18Asm
{
    private enum InstT {INST, VALUE, ADRS};
    private class Inst
    {
        public Inst(InstT t, int op, int ln)
        {
            type = t;
            opcode = op;
            parm = 0;
            line_no = ln;
            marker = null;
        }

        public InstT type;
        public int opcode;
        public int parm;
        public int line_no;
        public String marker;
    }

    private int rom_size = 4096;
    private int ram_size = 4096;
    private int con_size = 4096;

    private String marker, opcode, p1, p2;
    private HashMap<String, Integer> markers = new HashMap<>();
    private LinkedList<Inst> insts = new LinkedList<>();
    private LinkedList<Integer> consts = new LinkedList<>();
    private int line_no = 0;
    private int prog_cntr = 0;
    private int idx;
    private String sline;
    private byte[] bline;
    private int codes, regs, bits, cons;

    //-------------------------------------------------------------------------
    public static void main(String []args)
    {
        if (args.length != 1)
        {
            System.out.println("Usage: java Mini18Asm <file_name>");
            System.exit(-1);
        }

        String file_name = args[0];

        Miny18Asm asm = new Miny18Asm();

        try
        {
            System.out.println("Miny18 Asm " + file_name);
            asm.read_input(file_name);
            asm.fill_adrs();
            asm.write_output(file_name);
            asm.write_list_file(file_name);

            System.out.println("---- Success ----\n");
        }
        catch (MError e)
        {
            System.out.println(e.msg);
            System.exit(-2);
        }
    }

    //-------------------------------------------------------------------------
    public void write_list_file(String file_name) throws MError
    {
        String fmt0 = "            %s\n";
        String fmt1 = "%04o %06o %s\n";
        String str;

        try
        {
            FileReader file = new FileReader(file_name + ".asm");
            BufferedReader ifp = new BufferedReader(file);
            FileWriter ofp = new FileWriter(file_name + ".lst");

            int ln = 0;
            int pc = 0;
            Inst inst = insts.get(pc);

            while (true)
            {
                if (inst.line_no > ln)
                {
                    sline = ifp.readLine();
                    if (sline == null) break;
                    ln++;
                }

                if (inst.line_no == ln)
                {
                    str = String.format(fmt1, pc++, inst.opcode, sline);
                    sline = "";
                    ofp.write(str);

                    if (pc == insts.size()) break;
                    inst = insts.get(pc);
                }
                else
                {
                    str = String.format(fmt0, sline);
                    ofp.write(str);
                }
            }

            ifp.close();
            ofp.close();
        }
        catch (IOException e)
        {
            throw new MError("Error writing file: " + file_name);
        }

        System.out.println(String.format("CODE  %d", insts.size()));
        System.out.println(String.format("CONST %d", consts.size()));
        System.out.println(String.format("REG   %d", regs));
        System.out.println(String.format("BIT   %d", bits));
    }

    //-------------------------------------------------------------------------
    public void write_output(String file_name) throws MError
    {
        int [] ta = new int[(rom_size > con_size) ? rom_size : con_size];
        int i;

        if (insts.size() > rom_size)
            throw new MError("Code ROM size is too small");

        if (consts.size() > con_size)
            throw new MError("Constants ROM size is too small");

        try
        {
            FileWriter ofp = new FileWriter("code.hex");
            for (i = 0; i < insts.size(); i++)
                ta[i] = insts.get(i).opcode;
            for (i = 0; i < rom_size; i++)
            {
                String str = String.format("%05X ", ta[i]);
                ofp.write(str);
                if ((i % 16) == 15) ofp.write('\n');
            }
            ofp.close();

            if (con_size > 0)
            {
                ofp = new FileWriter("const.hex");
                for (i = 0; i < consts.size(); i++)
                    ta[i] = consts.get(i);
                for (; i < con_size; i++) ta[i] = 0;
                for (i = 0; i < con_size; i++)
                {
                    String str = String.format("%05X ", ta[i]);
                    ofp.write(str);
                    if ((i % 16) == 15) ofp.write('\n');
                }
                ofp.close();
            }
        }
        catch (IOException e)
        {
            throw new MError("Error writing file: " + file_name + ".hex");
        }
    }

    //-------------------------------------------------------------------------
    public void read_input(String file_name) throws MError
    {
        regs = bits = 0;

        try
        {
            FileReader file = new FileReader(file_name + ".asm");
            BufferedReader ifp = new BufferedReader(file);

            while (true)
            {
                sline = ifp.readLine();
                if (sline == null) break;
                line_no++;
                process_line();
            }

            ifp.close();
        }
        catch (IOException e)
        {
            throw new MError("File not found: " + file_name + ".asm");
        }
    }

    //-------------------------------------------------------------------------
    private void process_line() throws MError
    {
        int idx = sline.indexOf(';');
        if (idx >= 0) sline = sline.substring(0, idx);
        sline = sline.replaceAll("\\s+$","");

        parse_line();

        if (marker != null) markers.put(marker, prog_cntr);

        try
        {
            switch (opcode)
            {
                case "NOP":     simple_opcode(0000000); break;
                case "HALT":    simple_opcode(0000100); break;
                case "PAUSE":   simple_opcode(0000200); break;
                case "RTS":     simple_opcode(0000300); break;
                case "RTI":     simple_opcode(0000400); break;
                case "RESET":   simple_opcode(0000600); break;
                case "RESTART": simple_opcode(0200000); break;

                case "LEVEL": level_opcode(); break;
                case "TIMER": timer_opcode(); break;

                case "CALL":  adrs_opcode(0010000); break;
                case "LDR":   ldr_opcode(0020000); break;
                case "STR":   str_opcode(0030000); break;
                case "LDC":   ldr_opcode(0040000); break;
                case "IN":    reg2_opcode(0060000); break;
                case "OUT":   reg2_opcode(0070000); break;

                case "SEQ":   bit1_opcode(0120000); break;
                case "SBS":   bit1_opcode(0120000); break;
                case "SNE":   bit1_opcode(0130000); break;
                case "SBC":   bit1_opcode(0130000); break;
                case "SLT":   bit1_opcode(0140000); break;
                case "SGT":   bit1_opcode(0150000); break;
                case "SLE":   bit1_opcode(0160000); break;
                case "SGE":   bit1_opcode(0170000); break;

                case "JMP":   adrs_opcode(0200000); break;
                case "JEQ":   adrs_opcode(0220000); break;
                case "JBS":   adrs_opcode(0220000); break;
                case "JNE":   adrs_opcode(0230000); break;
                case "JBC":   adrs_opcode(0230000); break;
                case "JLT":   adrs_opcode(0240000); break;
                case "JGT":   adrs_opcode(0250000); break;
                case "JLE":   adrs_opcode(0260000); break;
                case "JGE":   adrs_opcode(0270000); break;

                case "MOV": mov_opcode(); break;

                case "CMP": math2_opcode(0000000); break;
                case "NEG": math2_opcode(0020000); break;
                case "INV": math2_opcode(0030000); break;
                case "SHR": math2_opcode(0040000); break;
                case "SHL": math2_opcode(0050000); break;
                case "ADD": math2_opcode(0110000); break;
                case "SUB": math2_opcode(0120000); break;
                case "MUL": math2_opcode(0130000); break;
                case "AND": math2_opcode(0140000); break;
                case "OR":  math2_opcode(0150000); break;
                case "XOR": math2_opcode(0160000); break;

                case "BCMP": bit2_opcode(0300000); break;
                case "BMOV": bit2_opcode(0310000); break;
                case "BNOT": bit2_opcode(0320000); break;
                case "BCLR": bit1_opcode(0330000); break;
                case "BSET": bit1_opcode(0340000); break;
                case "BAND": bit2_opcode(0350000); break;
                case "BOR":  bit2_opcode(0360000); break;
                case "BXOR": bit2_opcode(0370000); break;

                case "ORG":  org_opcode(); break;
                case "DC":   define_int_const(); break;
                case "CONFIG": config(); break;
                case "": break;

                default:
                    throw new MError("Invalid opcode: " + opcode);
            }
        } 
        catch (NumberFormatException e)
        {
            throw new MError("Number format error, at line " + line_no);
        }
    }

    //-------------------------------------------------------------------------
    private void org_opcode() throws MError
    {
        skip_whitespace();

        // Parse the value
        p1 = get_str();
        prog_cntr = Integer.parseInt(p1);
    }

    //-------------------------------------------------------------------------
    private void config() throws MError
    {
        skip_whitespace();
        String roms = get_str();
        skip_whitespace();
        String rams = get_str();
        skip_whitespace();
        String cons = get_str();

        int romi, rami, coni;
        try
        {
            romi = Integer.parseInt(roms);
            rami = Integer.parseInt(rams);
            coni = Integer.parseInt(cons);
        }
        catch (NumberFormatException e)
        {
            throw new MError("Invalid config statement");
        }

        if (romi < 1 || romi > 12)
            throw new MError("Invalid ROM config statement");

        if (rami < 1 || rami > 18)
            throw new MError("Invalid RAM config statement");

        if (coni < 0 || coni > 12)
            throw new MError("Invalid constants config statement");

        rom_size = 1 << romi;
        ram_size = 1 << rami;
        con_size = (coni == 0) ? 0 : 1 << coni;
    }

    //-------------------------------------------------------------------------
    private void define_int_const() throws MError
    {
        skip_whitespace();

        while (true)
        {
            // Parse the value
            p1 = get_str();
            if (p1 == "") break;
            consts.add(Integer.parseInt(p1));
            prog_cntr++;
            skip_whitespace();
        }

        if (consts.size() > 4095)
            throw new MError("Exceeded constants space");
    }

    //-------------------------------------------------------------------------
    private void reg2_opcode(int opcode) throws MError
    {
        parse_parms(2);

        int dreg = get_reg(p1);
        int sreg = get_reg(p2);

        opcode |= (sreg << 6) | dreg;
        insts.add(new Inst(InstT.INST, opcode, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void str_opcode(int opcode) throws MError
    {
        parse_parms(2);

        if (p1.charAt(0) != '(' || p1.charAt(p1.length() - 1) != ')')
            throw new MError("Invalid parameter, at line " + line_no);

        p1 = p1.substring(1, p1.length() - 1);
        int dreg = get_reg(p1);
        int sreg = get_reg(p2);

        opcode |= (sreg << 6) | dreg;
        insts.add(new Inst(InstT.INST, opcode, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void ldr_opcode(int opcode) throws MError
    {
        parse_parms(2);

        if (p2.length() < 3)
            throw new MError("Invalid parameter, at line " + line_no);

        int dreg = get_reg(p1);

        if (p2.charAt(0) != '(' || p2.charAt(p2.length() - 1) != ')')
            throw new MError("Invalid parameter, at line " + line_no);

        p2 = p2.substring(1, p2.length() - 1);
        int sreg = get_reg(p2);

        opcode |= (sreg << 6) | dreg;
        insts.add(new Inst(InstT.INST, opcode, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void math1_opcode(int opcode) throws MError
    {
        parse_parms(1);

        int dreg = get_reg(p1);
        opcode |= dreg | dreg << 6;
        insts.add(new Inst(InstT.INST, opcode, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void bit2_opcode(int opcode) throws MError
    {
        parse_parms(2);
        if (p2.length() < 1)
            throw new MError("Invalid parameter, at line " + line_no);

        int dreg = get_bit(p1);
        int sreg = get_bit(p2);

        opcode |= (sreg << 6) | dreg;
        insts.add(new Inst(InstT.INST, opcode, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void mov_opcode() throws MError
    {
        parse_parms(2);

        int dreg = get_reg(p1);

        if (p2.length() < 1)
            throw new MError("Invalid parameter, at line " + line_no);

        if (p2.charAt(0) == '#')
        {
            int value = imm_value(p2);
            if (value > 0x3FFFF || value < -131071)
                throw new MError("Value out of range, at line " + line_no);

            if (value >= 0 && value < 64)
            {
                int opcode = 0610000 | (value << 6) | dreg;
                insts.add(new Inst(InstT.INST, opcode, line_no));
                prog_cntr++;
            }
            else
            {
                int opcode = 0000700 | dreg;
                insts.add(new Inst(InstT.INST, opcode, line_no));
                insts.add(new Inst(InstT.VALUE, value & 0x3FFFF, line_no));
                prog_cntr += 2;
            }
        }
        else
        {
            int sreg = get_reg(p2);
            int opcode = 0410000 | (sreg << 6) | dreg;
            insts.add(new Inst(InstT.INST, opcode, line_no));
            prog_cntr++;
        }
    }

    //-------------------------------------------------------------------------
    private void math2_opcode(int opcode) throws MError
    {
        parse_parms(2);

        int dreg = get_reg(p1);

        if (p2.length() < 1)
            throw new MError("Invalid parameter, at line " + line_no);

        if (p2.charAt(0) == '#')
        {
            int value = imm_value(p2);
            if (value > 0x3FFFF || value < -131071)
                throw new MError("Value out of range, at line " + line_no);

            if (value >= 0 && value < 64)
            {
                opcode |= (value << 6) | dreg | 0600000;
                insts.add(new Inst(InstT.INST, opcode, line_no));
                prog_cntr++;
            }
            else
            {
                opcode |= dreg | 0400000;
                insts.add(new Inst(InstT.INST, 0000700, line_no));
                insts.add(new Inst(InstT.VALUE, value & 0x3FFFF, line_no));
                insts.add(new Inst(InstT.INST, opcode, line_no));
                prog_cntr += 3;
            }
        }
        else
        {
            int sreg = get_reg(p2);
            opcode |= 0400000 | (sreg << 6) | dreg;
            insts.add(new Inst(InstT.INST, opcode, line_no));
            prog_cntr++;
        }
    }

    //-------------------------------------------------------------------------
    private void bit1_opcode(int opcode) throws MError
    {
        parse_parms(1);

        int offset = get_bit(p1);
        insts.add(new Inst(InstT.INST, opcode | offset, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void reg1_opcode(int opcode) throws MError
    {
        parse_parms(1);

        int offset = get_reg(p1);
        insts.add(new Inst(InstT.INST, opcode | offset, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void timer_opcode() throws MError
    {
        parse_parms(1);

        if (p1.charAt(0) != '#')
            throw new MError("Timer requires immediate value, at line " +
                             line_no);

        int value = Integer.parseInt(p1.substring(1));
        if (value > 4095 || value < 0)
            throw new MError("Value out of range, at line " + line_no);

        insts.add(new Inst(InstT.INST, 0050000 | value, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void level_opcode() throws MError
    {
        parse_parms(1);

        if (p1.charAt(0) != '#')
            throw new MError("Syntax error, at line " + line_no);

        int value = Integer.parseInt(p1.substring(1));
        if (value > 63 || value < 0)
            throw new MError("Level out of range, at line " + line_no);

        insts.add(new Inst(InstT.INST, 0000500 | value, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void adrs_opcode(int opcode) throws MError
    {
        parse_parms(1);
        Inst inst = new Inst(InstT.ADRS, opcode, line_no);
        inst.marker = p1;
        insts.add(inst);
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void simple_opcode(int opcode) throws MError
    {
        insts.add(new Inst(InstT.INST, opcode, line_no));
        prog_cntr++;
    }

    //-------------------------------------------------------------------------
    private void fill_adrs() throws MError
    {
        for (Inst inst : insts)
            if (inst.type == InstT.ADRS)
            {
                if (markers.containsKey(inst.marker) == false)
                    throw new MError("Label not declared: " + inst.marker);
                inst.opcode |= markers.get(inst.marker);
            }
    }

    //-------------------------------------------------------------------------
    private void parse_line() throws MError
    {
        byte c;

        opcode = "";
        marker = p1 = p2 = null;

        // Ignore blank lines
        if (sline.length() == 0) return;

        idx = 0;
        bline = (sline + ' ').getBytes();

        // Get any marker
        if (Character.isLetter(bline[idx]))
            marker = get_str();

        // Skip white space before opcode
        skip_whitespace();

        // Get opcode
        opcode = get_str();
    }

    //-------------------------------------------------------------------------
    private void parse_parms(int count) throws MError
    {
        // Skip white space after opcode
        skip_whitespace();

        // Get p1
        p1 = get_str();

        // Skip white space after p1
        skip_whitespace();

        // Check for comma if more parameters
        if (idx < bline.length && bline[idx++] != ',')
            throw new MError("Syntax error: " + sline);

        // Skip white space before p2
        skip_whitespace();

        // Get p2
        p2 = get_str();

        // Check for parm count errors
        if (p1 == "")
            throw new MError("Missing parameter, at line " + line_no);

        if (count == 2 && p2 == "")
            throw new MError("Missing parameter, at line " + line_no);
    }

    //-------------------------------------------------------------------------
    private String get_str()
    {
        String str = "";
        while (idx < bline.length)
        {
            byte c = bline[idx];
            if (c == '#' || c == '.' || c == '(' || c == ')' ||
                c == '_' || c == '-' ||
                Character.isLetterOrDigit(c))
            {
                str += (char)c;
                idx++;
            }
            else
                break;
        }
        return str;
    }

    //-------------------------------------------------------------------------
    private void skip_whitespace()
    {
        while (idx < bline.length)
        {
            byte c = bline[idx];
            if (Character.isWhitespace(c))
                idx++;
            else
                break;
        }
    }

    //-------------------------------------------------------------------------
    private int imm_value(String p2)
    {
        int value;
        if (p2.charAt(1) == '-')
            return -Integer.parseInt(p2.substring(2));
        else
            return Integer.parseInt(p2.substring(1));
    }

    //-------------------------------------------------------------------------
    private int get_reg(String str) throws MError
    {
        int reg = Integer.parseInt(str);
        if (reg > 63 || reg < 0)
            throw new MError("Register offset out of range, at line " +
                             line_no);

        if (reg > regs) regs = reg;
        return reg;
    }

    //-------------------------------------------------------------------------
    private int get_bit(String str) throws MError
    {
        int bit = Integer.parseInt(str);
        if (bit > 63 || bit < 0)
            throw new MError("Bit offset out of range, at line " + line_no);

        if (bit > bits) bits = bit;
        return bit;
    }
}
