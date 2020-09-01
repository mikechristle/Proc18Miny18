//-----------------------------------------------------------------------------
// Miny Node
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

//-----------------------------------------------------------------------------
enum NodeId
{
    // Node
    BREAK,      // Id
    CONTINUE,   // Id
    HALT,       // Id
    PAUSE,      // Id
    RESET,      // Id
    RESTART,    // Id
    ICON,       // Id > Value
    SCON,       // Id > Value
    BCON,       // Id > Value
    LABEL,      // Id > Label
    PORT,       // Id > Label
    LEVEL,      // Id > Value

    // BlockNode
    CALL,       // Id > Label, Expr...
    CONST,      // Id, Expr...
    BLOCK,      // Id, Node...
    LOOP,       // Id, Node...

    // StmtNode
    IF,         // Id, Expr, If, Else
    ASSIGN,     // Id, Label, Expr
    COMPARE,    // Id > CompOp, Expr1, Expr2
    MATHOP,     // Id > MathOp, Expr1, Expr2
    ARRAY,      // Id > Label, Expr1
    TIMER,      // Id, Expr1
    RETURN      // Id, Expr1
}

//-----------------------------------------------------------------------------
public class Node
{
    public NodeId id;
    public Src src;
    public TypeId type;
    public Object value;

    //-------------------------------------------------------------------------
    public Node(NodeId _id, Src _src)
    {
        id = _id;
        src = _src;
        type = TypeId.NONE;
        value = null;
    }

    //-------------------------------------------------------------------------
    public Node(NodeId _id, Src _src, TypeId _type, Object _value)
    {
        id = _id;
        src = _src;
        type = _type;
        value = _value;
    }

    //-------------------------------------------------------------------------
    public boolean bval() { return (boolean)value; }
    public int     ival() { return (int)value; }
    public String  sval() { return (String)value; }

    //-------------------------------------------------------------------------
    public String toString()
    {
        String str = String.format("%s", id.toString());
        if (type != TypeId.NONE) str += String.format(" %s", type.toString());
        if (value != null) str += String.format(" %s", value.toString());
        return str;
    }
}

