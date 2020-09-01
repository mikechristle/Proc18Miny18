//-----------------------------------------------------------------------------
// Miny Main Program
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

import java.io.File;

//-----------------------------------------------------------------------------
public class Miny18
{
    //-------------------------------------------------------------------------
    public static void main(String []args)
    {
        boolean debug_flag = false;
        boolean dump_symbol_table = false;
        boolean dump_parse_tree = false;
        boolean dump_icodes = false;
        boolean asm_gen_flag = false;
        String file_name = null;

        for (String s : args)
        {
            switch (s)
            {
                case "-d": debug_flag = true; break;
                case "-s": dump_symbol_table = true; break;
                case "-t": dump_parse_tree = true; break;
                case "-i": dump_icodes = true; break;
                case "-a": asm_gen_flag = true; break;
                default: file_name = s; break;
            }
        }

        if (file_name == null ||
            new File(file_name + ".m").exists() == false)
        {
            System.out.println("Usage: java Miny18 <file_name> [Options]");
            System.out.println("   -a    Output Asm Code");
            System.out.println("   -s    Dump Symbol Table");
            System.out.println("   -t    Dump Parse Tree");
            System.out.println("   -i    Dump ICodes");
            System.out.println("   -d    Debug");
            System.exit(-1);
        }

        Scanner scanner = new Scanner();
        Parser parser = new Parser(scanner, file_name);
        Checker checker = new Checker();
        FoldConst fold_const = new FoldConst();
        ICodeGen icode_gen = new ICodeGen();
        ICodeOpt icode_opt = new ICodeOpt();
        AsmGen18 asm_gen = new AsmGen18();

        try
        {
            Module.add_isr(0, file_name + ".main");

            if (debug_flag) System.out.println("---- Parser -----");
            parser.start();

            if (debug_flag) System.out.println("---- Checker ----");
            checker.check();

            if (debug_flag) System.out.println("---- FoldConst ----");
            fold_const.fold();

            if (debug_flag) System.out.println("---- ICodeGen ----");
            icode_gen.run();

            if (debug_flag) System.out.println("---- ICodeOpt ----");
            icode_opt.run();

            if (asm_gen_flag)
            {
                if (debug_flag) System.out.println("---- Miny18 ----");
                asm_gen.run(file_name);
            }

            System.out.println("---- Success ----");

            if (dump_symbol_table) Util.dump_symbols();
            if (dump_parse_tree) Util.dump_parse_tree();
            if (dump_icodes) Util.dump_icodes();
        }
        catch (MError e)
        {
            if (e.src != null)
            {
                String fmt = "\nError in file %s at line %d.\n%s\n";
                String msg = String.format(fmt,
                    scanner.file_names.get(e.src.file), e.src.line, e.msg);
                System.out.println(msg);
            }
            else
            {
                System.out.println(e.msg);
            }
            System.exit(-2);
        }
    }
}