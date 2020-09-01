//-----------------------------------------------------------------------------
// Miny Call Tree
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
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

import java.util.Comparator;
import java.util.TreeSet;

//-----------------------------------------------------------------------------
public class CallTree implements Comparator<CallTree> 
{
    public TreeSet<CallTree> tree;
    public String name;

    private static TreeSet<String> path = new TreeSet<>();

    //-------------------------------------------------------------------------
    public CallTree(String _name)
    {
        name = _name;
        tree = null;
    }

    //-------------------------------------------------------------------------
    public void add(CallTree call)
    {
        if (tree == null) tree = new TreeSet<>(this);
        tree.add(call);
    }

    //-------------------------------------------------------------------------
    public int compare(CallTree a, CallTree b) 
    { 
        return a.name.compareTo(b.name); 
    } 

    //-------------------------------------------------------------------------
    public static CallTree build(String name) throws MError
    {
        int idx = name.indexOf('.');
        String mod_name = name.substring(0, idx);
        if (Module.modules.containsKey(mod_name) == false)
            throw new MError("Invalid module name: " + mod_name);

        Module mod = Module.modules.get(mod_name);
        if (mod.funcs.containsKey(name) == false)
            throw new MError("Missing main function");

        CallTree root = new CallTree(name);
        add_func(root);
        return root;
    }

    //-------------------------------------------------------------------------
    private static void add_func(CallTree tree) throws MError
    {
        if (path.contains(tree.name))
            throw new MError("Recursive function call: " + tree.name);

        int idx = tree.name.indexOf('.');
        String mod_name = tree.name.substring(0, idx);
        Module mod = Module.modules.get(mod_name);
        Func func = mod.funcs.get(tree.name);
        path.add(tree.name);

        for (ICode icode : func.icodes)
            if (icode.id == ICodeId.CALL)
            {
                CallTree sub_tree = new CallTree((String)icode.parm);
                tree.add(sub_tree);
                add_func(sub_tree);
            }

        path.remove(tree.name);
    }

    //-------------------------------------------------------------------------
    public static void dump_tree(CallTree tree, int indent)
    {
        for (int i = 0; i < indent; i++)
            System.out.print("   ");

        Func func = Module.find_func(tree.name);
        String str = String.format("%s, %d %d, %d %d", tree.name,
            func.reg_base, func.reg_cnt,
            func.bit_base, func.bit_cnt);
        System.out.println(str);

        if (tree.tree != null)
            for (CallTree t : tree.tree)
                dump_tree(t, indent + 1);
    }
}
