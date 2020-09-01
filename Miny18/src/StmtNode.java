//-----------------------------------------------------------------------------
// Miny StmtNode
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
public class StmtNode extends  Node
{
    public Node p1;
    public Node p2;
    public Node p3;

    //-------------------------------------------------------------------------
    public StmtNode(NodeId _id, Src _src)
    {
        super(_id, _src);
        p1 = null;
        p2 = null;
        p3 = null;
    }

    //-------------------------------------------------------------------------
    public StmtNode(NodeId _id, Src _src, MathOp mop)
    {
        super(_id, _src);
        value = mop;
        p1 = null;
        p2 = null;
        p3 = null;
    }

    //-------------------------------------------------------------------------
    public String toString()
    {
        String str = String.format("%s", id.toString());
        if (type != TypeId.NONE) str += String.format(" %s", type.toString());
        if (value != null) str += String.format(" %s", value.toString());
        return str;
    }
}

