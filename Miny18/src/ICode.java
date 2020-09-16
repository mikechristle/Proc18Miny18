//-----------------------------------------------------------------------------
// Miny ICode
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/16/2020   Add NOP command
//                      Change NOP ICodeId to NONE to support NOP command
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
enum ICodeId
{
    NONE,   // id
    MARKER, // id > Marker
    JMP,    // id > Marker
    JMPT,   // id > Marker, Expr
    JMPF,   // id > Marker, Expr
    SETB,   // id > CompOp, Dst
    COMPB,  // id > MathOp, Op1, Op2
    COMPI,  // id > MathOp, Op1, Op2
    MATHB,  // id > MathOp, Op1, Op2
    MATHI,  // id > MathOp, Op1, Op2
    ASSIGNB,// Id, Dst, Src
    ASSIGNI,// Id, Dst, Src
    CALL,   // id > FuncLabel
    RETURN, // id, Expr
    TIMER,  // id, Expr
    HALT,   // id
    RESET,  // id
    RESTART,// id
    PAUSE,  // id
    NOP,    // id
    LEVEL,  // id > Value
    IN,     // id > Label, Adrs
    OUT,    // id > Label, Adrs
    REG,    // id > Label
    RAM,    // id > Label
    ROM,    // id > Label
    BIT,    // id > Label
    ADDRESS,// id > Label, Expr
    ARRAY,  // id > Label, Expr
    BCON,   // id > Value
    ICON,   // id > Value
    SCON,   // id > Value
    PARM    // Id > Label, Expr
}

//-----------------------------------------------------------------------------
public class ICode
{
    public ICodeId id;
    public Object parm;
    public int p1, p2;
    public Src src;
    public int offset;
    public int last_ref;

    //-------------------------------------------------------------------------
    public ICode(ICodeId _id, Src _src)
    {
        id = _id;
        src = _src;
        parm = null;
        offset = 0;
        last_ref = -1;
        p1 = p2 = -1;
    }

    //-------------------------------------------------------------------------
    public ICode(ICodeId _id, Src _src, Object _parm)
    {
        id = _id;
        src = _src;
        parm = _parm;
        offset = 0;
        last_ref = -1;
        p1 = p2 = -1;
    }

    //-------------------------------------------------------------------------
    public String toString()
    {
        String str = id.toString();
        if (parm != null) str += String.format(" > %s", parm);
        if (p1 >= 0) str += String.format(" p1=%d", p1);
        if (p2 >= 0) str += String.format(" p2=%d", p2);
       /* if (offset > 0)*/ str += String.format(" [%d]", offset);
        if (last_ref >= 0) str += String.format(" {%d}", last_ref);
        return str;
    }
}
