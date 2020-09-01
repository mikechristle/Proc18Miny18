//-----------------------------------------------------------------------------
// Miny Scanner
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

import java.io.*;
import java.util.LinkedList;
import java.util.Map;

//-----------------------------------------------------------------------------
public class Scanner
{
    public LinkedList<String> file_names = new LinkedList<>();

    private String sline = "";
    private LinkedList<Token> tokens = new LinkedList<Token>();
    private Token current_token;
    private int idx = 0;
    private int line_no;
    private BufferedReader ifp;
    private String util_dir;

    //-------------------------------------------------------------------------
    public Scanner()
    {
        String util_dir_string = "MINY_UTIL";
        Map<String, String> env = System.getenv();
        if (env.containsKey(util_dir_string))
            util_dir = env.get(util_dir_string);
    }

    //-------------------------------------------------------------------------
    public void open(String file_name) throws MError
    {
        if (file_names.contains(file_name)) return;
        file_names.add(file_name);
        System.out.println("Openning " + file_name);

        try
        {
            File file = new File(file_name);
            if (file.exists() == false && util_dir != null)
                file = new File(util_dir + file_name);

            FileReader reader = new FileReader(file);
            ifp = new BufferedReader(reader);
            line_no = 0;
        }
        catch (FileNotFoundException e)
        {
            String emsg = "File not found: " + file_name;
            throw new MError(emsg);
        }

        while (tokens.size() == 0) get_tokens();
        current_token = tokens.poll();
    }

    //-------------------------------------------------------------------------
    public void next() throws MError
    {
        while (tokens.size() == 0) get_tokens();
        current_token = tokens.poll();
    }

    //-------------------------------------------------------------------------
    public void expect(TokenId expect) throws MError
    {
        if (current_token.id != expect)
        {
            String emsg = String.format("Expected %s, found %s.",
                expect, current_token.id);
            throw new MError(emsg, current_token.src);
        }

        next();
    }

    //-------------------------------------------------------------------------
    public Token token()
    {
        return current_token;
    }

    //-------------------------------------------------------------------------
    private void get_tokens() throws MError
    {
        try
        {
            sline = ifp.readLine();
            if (sline == null)
            {
                ifp.close();
                tokens.add(new Token(TokenId.EOF, new Src(0, 0)));
            }
            else
            {
                line_no++;
                parse_sline();
            }
        }
        catch (Exception e)
        {
            throw new MError("Error reading source file", src());
        }
    }

    //-------------------------------------------------------------------------
    private void parse_sline() throws MError
    {
        idx = 0;
        sline = sline.trim() + "##";

        while (idx < sline.length())
        {
            char c = sline.charAt(idx);
            if (c == '#') break;
            else if (Character.isWhitespace(c)) idx++;
            else if (Character.isDigit(c)) parse_digit();
            else if (Character.isLetter(c)) parse_label();
            else if (c == '"') parse_string();
            else if (c == '\'') parse_char();
            else parse_symbol();
        }
    }

    //-------------------------------------------------------------------------
    private void parse_char() throws MError
    {
        if ((sline.length() - idx) < 3 ||
             sline.charAt(idx + 2) != '\'')
                throw new MError("Invalid char constant", src());

        char c = sline.charAt(idx + 1);
        idx += 3;

        Token tk = new Token(TokenId.ICON, src(), (int)c);
        tokens.add(tk);
    }

    //-------------------------------------------------------------------------
    private void parse_string() throws MError
    {
        int start = idx + 1;
        int stop = start;
        char c = ' ';

        while (stop < sline.length())
        {
            c = sline.charAt(stop);
            if (c == '"') break;
            stop++;
        }

        if (c != '"')
            throw new MError("Missing end quote on string", src());

        String label = sline.substring(start, stop);
        idx = stop + 1;
        Token tk = new Token(TokenId.SCON, src(), label);
        tokens.add(tk);
    }

    //-------------------------------------------------------------------------
    private void parse_digit() throws MError
    {
        final String ALL_DIGITS = "_0123456789ABCDEF";
        StringBuilder str_build = new StringBuilder(20);
 
        char c;
        int start = idx;
        int base = 10;
        TokenId id = TokenId.ICON;

        if (sline.charAt(start) == '0')
        {
            switch (sline.charAt(start + 1))
            {
                case 'X':
                case 'x': base = 16; break;
                case 'B':
                case 'b': base = 2; break;
                case 'O':
                case 'o': base = 8; break;
            }
        }

        if (base != 10) start += 2;
        int stop = start;

        String valid_digits = ALL_DIGITS.substring(0, base + 1);
        
        while (true)
        {
            c = sline.charAt(stop);
            c = Character.toUpperCase(c);
            if (valid_digits.indexOf(c) < 0) break;
            stop++;
            if (c != '_') str_build.append(c);
        }

        idx = stop;
        String digits = str_build.toString();

        try
        {
            Token tk;
            int value = (int)Integer.parseInt(digits, base);
            tk = new Token(id, src(), value);
            tokens.add(tk);
        }
        catch (NumberFormatException e)
        {
            throw new MError("Number format error: " + digits, src());
        }
    }

    //-------------------------------------------------------------------------
    private void parse_label()
    {
        int start = idx;
        int stop = idx;
        while (stop < sline.length())
        {
            char c = sline.charAt(stop);
            if (Character.isLetterOrDigit(c) || c == '_') stop++;
            else break;
        }
        String label = sline.substring(start, stop);
        idx = stop;

        MathOp mop = MathOp.PAS;
        TokenId id;
        boolean bval = false;
        TypeId type = TypeId.NONE;

        switch (label)
        {
            case "module"   : id = TokenId.MODULE; break;
            case "func"     : id = TokenId.FUNC; break;
            case "level"    : id = TokenId.LEVEL; break;
            case "halt"     : id = TokenId.HALT; break;
            case "pause"    : id = TokenId.PAUSE; break;
            case "timer"    : id = TokenId.TIMER; break;
            case "return"   : id = TokenId.RETURN; break;
            case "reset"    : id = TokenId.RESET; break;
            case "restart"  : id = TokenId.RESTART; break;
            case "isr"      : id = TokenId.ISR; break;
            case "port"     : id = TokenId.PORT; break;
            case "const"    : id = TokenId.CONST; break;
            case "break"    : id = TokenId.BREAK; break;
            case "continue" : id = TokenId.CONTINUE; break;
            case "loop"     : id = TokenId.LOOP; break;
            case "if"       : id = TokenId.IF; break;
            case "elif"     : id = TokenId.ELIF; break;
            case "else"     : id = TokenId.ELSE; break;
            case "and"      : id = TokenId.MATHOP; mop = MathOp.LG_AND; break;
            case "or"       : id = TokenId.MATHOP; mop = MathOp.LG_OR; break;
            case "not"      : id = TokenId.MATHOP; mop = MathOp.LG_NOT; break;
            case "true"     : id = TokenId.BCON; bval = true; break;
            case "false"    : id = TokenId.BCON; break;
            case "bool"     : id = TokenId.BOOL; type = TypeId.BOOL; break;
            case "int"      : id = TokenId.INT; type = TypeId.INT; break;
            case "ram"      : id = TokenId.RAM; type = TypeId.RAMP; break;
            case "rom"      : id = TokenId.ROM; type = TypeId.ROMP; break;

            default : id = TokenId.LABEL; break;
        }

        Token tk;
        if (id == TokenId.LABEL)
            tk = new Token(id, src(), label);
        else if (id == TokenId.BCON)
            tk = new Token(id, src(), bval);
        else if (id == TokenId.MATHOP)
            tk = new Token(id, src(), mop);
        else if (type != TypeId.NONE)
            tk = new Token(id, src(), type);
        else
            tk = new Token(id, src());

        tokens.add(tk);
    }

    //-------------------------------------------------------------------------
    private void parse_symbol() throws MError
    {
        TokenId id = TokenId.EOF;
        MathOp math_op = null;
        int start = idx;
        int stop = start + 3;

        switch (sline.substring(start, stop))
        {
            case ">>=": id = TokenId.ASSIGN; math_op = MathOp.SHR; break;
            case "<<=": id = TokenId.ASSIGN; math_op = MathOp.SHL; break;
        }

        if (id == TokenId.EOF)
        {
            stop--;
            switch (sline.substring(start, stop))
            {
                case "+=": id = TokenId.ASSIGN; math_op = MathOp.ADD; break;
                case "-=": id = TokenId.ASSIGN; math_op = MathOp.SUB; break;
                case "*=": id = TokenId.ASSIGN; math_op = MathOp.MUL; break;
                case "/=": id = TokenId.ASSIGN; math_op = MathOp.DIV; break;
                case "%=": id = TokenId.ASSIGN; math_op = MathOp.MOD; break;
                case "&=": id = TokenId.ASSIGN; math_op = MathOp.BW_AND; break;
                case "|=": id = TokenId.ASSIGN; math_op = MathOp.BW_OR; break;
                case "^=": id = TokenId.ASSIGN; math_op = MathOp.BW_XOR; break;
                case "<<": id = TokenId.MATHOP; math_op = MathOp.SHL; break;
                case ">>": id = TokenId.MATHOP; math_op = MathOp.SHR; break;
                case "==": id = TokenId.COMPARE; math_op = MathOp.EQ; break;
                case "!=": id = TokenId.COMPARE; math_op = MathOp.NE; break;
                case ">=": id = TokenId.COMPARE; math_op = MathOp.GE; break;
                case "<=": id = TokenId.COMPARE; math_op = MathOp.LE; break;
            }
        }

        if (id == TokenId.EOF)
        {
            stop--;
            switch (sline.substring(start, stop))
            {
                case "=": id = TokenId.ASSIGN; break;
                case "+": id = TokenId.MATHOP; math_op = MathOp.ADD; break;
                case "-": id = TokenId.MATHOP; math_op = MathOp.SUB; break;
                case "*": id = TokenId.MATHOP; math_op = MathOp.MUL; break;
                case "/": id = TokenId.MATHOP; math_op = MathOp.DIV; break;
                case "%": id = TokenId.MATHOP; math_op = MathOp.MOD; break;
                case "&": id = TokenId.MATHOP; math_op = MathOp.BW_AND; break;
                case "|": id = TokenId.MATHOP; math_op = MathOp.BW_OR; break;
                case "^": id = TokenId.MATHOP; math_op = MathOp.BW_XOR; break;
                case "~": id = TokenId.MATHOP; math_op = MathOp.BW_NOT; break;
                case "(": id = TokenId.PARENL; break;
                case ")": id = TokenId.PARENR; break;
                case "{": id = TokenId.BRACEL; break;
                case "}": id = TokenId.BRACER; break;
                case "[": id = TokenId.BRACKL; break;
                case "]": id = TokenId.BRACKR; break;
                case ".": id = TokenId.PERIOD; break;
                case ",": id = TokenId.COMMA; break;
                case ">": id = TokenId.COMPARE; math_op = MathOp.GT; break;
                case "<": id = TokenId.COMPARE; math_op = MathOp.LT; break;
            }
        }

        if (id == TokenId.EOF)
        {
            String emsg = "Syntax error: " + sline.substring(start, stop);
            throw new MError(emsg, src());
        }

        Token tk;
        if (math_op != null)
            tk = new Token(id, src(), math_op);
        else
            tk = new Token(id, src());

        tokens.add(tk);
        idx = stop;
    }

    //-------------------------------------------------------------------------
    private Src src()
    {
        return new Src(file_names.size() - 1, line_no);
    }
}
