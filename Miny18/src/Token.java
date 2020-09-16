//-----------------------------------------------------------------------------
// Miny Token
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/13/2020   Add config command
// 1.2.0   09/16/2020   Add nop command
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
enum TokenId
{
    EOF,
    MATHOP, COMPARE,
    MODULE, FUNC, ISR,
    LABEL, ASSIGN, CONFIG,
    IF, ELIF, ELSE, NOP,
    LOOP, BREAK, CONTINUE,
    LEVEL, HALT, PAUSE, TIMER,
    RETURN, RESET, RESTART,
    ICON, SCON, BCON,
    INT, BOOL, RAM, ROM,
    PORT, CONST,
    COMMA, PERIOD,
    PARENL, PARENR,
    BRACKL, BRACKR,
    BRACEL, BRACER
}

enum MathOp
{
    PAS, ITOR,
    ADD, SUB, MUL, DIV, MOD,
    SHR, SHL, NEG,
    BW_AND, BW_OR, BW_XOR, BW_NOT, 
    LG_AND, LG_OR, LG_NOT,
    EQ, NE, GT, LT, GE, LE
}

enum TypeId
{
    NONE, INT, BOOL, RAMA, ROMA, RAMP, ROMP
}

//-----------------------------------------------------------------------------
class Token
{
    public final TokenId id;
    public final Src src;
    public final Object value;

    //-------------------------------------------------------------------------
    public Token(TokenId _id, Src _src)
    {
        id = _id;
        src = _src;
        value = null;
    }

    //-------------------------------------------------------------------------
    public Token(TokenId _id, Src _src, Object _value)
    {
        id = _id;
        src = _src;
        value = _value;
    }
    
    //-------------------------------------------------------------------------
    public MathOp mop()
    {
        return (MathOp)value;
    }

    //-------------------------------------------------------------------------
    public String toString()
    {
        if (value == null)
            return id.toString();
        else
            return String.format("%s %s", id, value);
    }
}
