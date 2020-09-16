//-----------------------------------------------------------------------------
// Miny Parser
//
// History: 
// 0.1.0   07/27/2017   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/13/2020   Add hardware config statement
// 1.2.0   09/16/2020   Add nop statement
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

import java.util.LinkedList;
import java.util.Objects;

//-----------------------------------------------------------------------------
public class Parser
{
    private Scanner scanner;
    private Module module;
    private String module_name;
    private Func func;
    private LinkedList<String> files_to_read = new LinkedList<>();

    //-------------------------------------------------------------------------
    public Parser(Scanner _scanner, String module_name)
    {
        scanner = _scanner;
        files_to_read.add(module_name);
    }

    //-------------------------------------------------------------------------
    // Start -> ConfigStmt | ModuleDecl* ;
    //-------------------------------------------------------------------------
    public void start() throws MError
    {
        while (files_to_read.size() > 0)
        {
            module_name = files_to_read.poll();
            scanner.open(module_name + ".m");

            while (true)
            {
                Token tk = scanner.token();
                if      (tk.id == TokenId.EOF) break;
                else if (tk.id == TokenId.CONFIG) ConfigStmt();
                else if (tk.id == TokenId.MODULE)
                {
                    ModuleDecl();
                    Module.modules.put(module_name, module);
                }
                else
                    throw new MError("Invalid statement", tk.src);
            }
        }
    }

    //-------------------------------------------------------------------------
    // ConfigStmt -> config IntConst IntConst IntConst ;
    //-------------------------------------------------------------------------
    private void ConfigStmt() throws MError
    {
        scanner.next(); // config
        Token tk1 = scanner.token();
        scanner.expect(TokenId.ICON);
        Token tk2 = scanner.token();
        scanner.expect(TokenId.ICON);
        Token tk3 = scanner.token();
        scanner.expect(TokenId.ICON);

        int rom_bits = (int)(tk1.value);
        int ram_bits = (int)(tk2.value);
        int con_bits = (int)(tk3.value);

        if (rom_bits < 1 || rom_bits > 12)
            throw new MError("ROM size error", tk1.src);

        if (ram_bits < 1 || ram_bits > 18)
            throw new MError("RAM size error", tk2.src);

        if (con_bits < 0 || con_bits > 18)
            throw new MError("Constants size error", tk3.src);

        Module.rom_bits = rom_bits;
        Module.ram_bits = ram_bits;
        Module.con_bits = con_bits;
    }

    //-------------------------------------------------------------------------
    // ModuleDecl -> module Label '{' ( ItemDecl | FuncDecl )* '}' ;
    //-------------------------------------------------------------------------
    private void ModuleDecl() throws MError
    {
        while (scanner.token().id != TokenId.EOF)
        {
            scanner.expect(TokenId.MODULE);
            module = new Module();

            Token tk = scanner.token();
            scanner.expect(TokenId.LABEL);
            String module_label = (String)tk.value;
            if (Objects.equals(module_label, module_name) == false)
                throw new MError("Module name does not match file name",
                                 tk.src);
            
            Func init = new Func(module_label, tk.src);

            scanner.expect(TokenId.BRACEL);
            while (scanner.token().id != TokenId.BRACER)
            {
                if (scanner.token().id == TokenId.FUNC)
                {
                    FuncDecl(module_label);
                    if (module.funcs.containsKey(func.label))
                        throw new MError("Duplicate func name: " +
                                         func.label, func.src);
                    module.funcs.put(func.label, func);
                }
                else
                {
                    func = init;
                    Node node = ItemDecl();
                    if (node != null) init.nodes.add(node);
                }
            }
            scanner.expect(TokenId.BRACER);

            if (init.nodes.size() > 0)
                module.funcs.put(module_label, init);
        }
    }   

    //-------------------------------------------------------------------------
    // ItemDecl -> IntDecl | BoolDecl | PortDecl | ConstDecl ;
    //-------------------------------------------------------------------------
    private Node ItemDecl() throws MError
    {
        Token tk = scanner.token();
        switch (tk.id)
        {
            case INT:   return IntDecl();
            case BOOL:  return BoolDecl();
            case PORT:  return PortDecl();
            case CONST: return ConstDecl();

            default:
                System.out.println(tk.toString());
                throw new MError("Invalid statement at module level", tk.src);
        }
    }

    //-------------------------------------------------------------------------
    // FuncDecl -> func Label ParmList ( bool | int | IsrDecl )? CodeBlock ;
    // ParmList -> '(' ( ParmType Label )#? ')' ;
    // ParmType -> bool | int | ram | rom ;
    // IsrDecl  -> isr DecConst ;
    //-------------------------------------------------------------------------
    private void FuncDecl(String module_name) throws MError
    {
        scanner.next(); // func

        Token tk = scanner.token();
        scanner.expect(TokenId.LABEL);
        String func_label = module_name + '.' + (String)tk.value;
        func = new Func(func_label, tk.src);
        func.mod_syms = module.symbols;

        scanner.expect(TokenId.PARENL);
        while (true)
        {
            tk = scanner.token();
            if (tk.id != TokenId.INT && 
                tk.id != TokenId.BOOL &&
                tk.id != TokenId.RAM &&
                tk.id != TokenId.ROM) break;

            scanner.next(); // Type
            TypeId type = (TypeId)(tk.value);

            StoreId store = StoreId.REG;
            switch (type)
            {
                case BOOL: store = StoreId.BIT; break;
                case RAMP: store = StoreId.REG; break;
                case ROMP: store = StoreId.REG; break;
            }

            tk = scanner.token();
            scanner.expect(TokenId.LABEL);
            String label = (String)tk.value;

            Node node = new Node(NodeId.LABEL, tk.src, type,
                                 func_label + '.' + label);
            func.parms.add(node);
            func.symbols.put(label, new Symbol(type, store));
            
            if (scanner.token().id != TokenId.COMMA) break;
            scanner.expect(TokenId.COMMA);
        }
        scanner.expect(TokenId.PARENR);

        tk = scanner.token();
        if (tk.id == TokenId.INT || tk.id == TokenId.BOOL)
        {
            func.type = (TypeId)tk.value;
            scanner.next(); // int, bool
            func.symbols.put("return", new Symbol(func.type, StoreId.REG));
        }
        else if (tk.id == TokenId.ISR)
        {
            scanner.next(); // isr
            tk = scanner.token();
            scanner.expect(TokenId.ICON);
            func.isr = true;
            Module.add_isr((int)tk.value, func_label);
        }

        scanner.expect(TokenId.BRACEL);
        while (scanner.token().id != TokenId.BRACER)
        {
            func.nodes.add(Statement());
        }
        scanner.expect(TokenId.BRACER);
    }

    //-------------------------------------------------------------------------
    // IntDecl -> int Label ( ( '[' IntConst ']' )? )# ;
    //-------------------------------------------------------------------------
    private Node IntDecl() throws MError
    {
        scanner.next(); // int

        while (true)
        {
            Token tk = scanner.token();
            scanner.expect(TokenId.LABEL);
            Symbol symbol = new Symbol(TypeId.INT, StoreId.REG);

            if (scanner.token().id == TokenId.BRACKL)
            {
                scanner.expect(TokenId.BRACKL);
                Token count = scanner.token();
                scanner.expect(TokenId.ICON);
                scanner.expect(TokenId.BRACKR);
                symbol.count = (int)count.value;
                symbol.store = StoreId.RAM;
                symbol.type = TypeId.RAMP;
            }

            String label = (String)tk.value;
            if (module.symbols.containsKey(label))
                throw new MError("Duplicate label: " + label, tk.src);
            module.symbols.put(label, symbol);

            if (scanner.token().id != TokenId.COMMA) break;
            scanner.next(); // ','
        }

        return null;
    }

    //-------------------------------------------------------------------------
    // BoolDecl -> bool Label ( ( IntConst )? )#
    //-------------------------------------------------------------------------
    private Node BoolDecl() throws MError
    {
        TypeId type = (TypeId)scanner.token().value;
        scanner.next(); // bool

        while (true)
        {
            Token tk = scanner.token();
            scanner.expect(TokenId.LABEL);
            Symbol symbol = new Symbol(TypeId.BOOL, StoreId.BIT);

            if (scanner.token().id == TokenId.ICON)
            {
                Token offset = scanner.token();
                scanner.expect(TokenId.ICON);
                symbol.offset = (int)offset.value;
            }

            String label = (String)tk.value;
            if (module.symbols.containsKey(label))
                throw new MError("Duplicate label: " + label, tk.src);
            module.symbols.put(label, symbol);

            if (scanner.token().id != TokenId.COMMA) break;
            scanner.next(); // ','
        }

        return null;
    }

    //-------------------------------------------------------------------------
    // PortDecl -> port Label IntConst ;
    //-------------------------------------------------------------------------
    private Node PortDecl() throws MError
    {
        scanner.next(); // port

        Token tk = scanner.token();
        scanner.expect(TokenId.LABEL);
        String label = (String)tk.value;
        if (module.symbols.containsKey(label))
            throw new MError("Duplicate label: " + label, tk.src);

        Token offset = scanner.token();
        scanner.expect(TokenId.ICON);

        Symbol symbol = new Symbol(TypeId.INT, StoreId.PORT);
        symbol.offset = (int)offset.value;
        module.symbols.put(label, symbol);

        return null;
    }

    //-------------------------------------------------------------------------
    // ConstDecl -> const Label ( LogOrExpr# | StringConst ) ;
    //-------------------------------------------------------------------------
    private Node ConstDecl() throws MError
    {
        scanner.next(); // const

        Token tk = scanner.token();
        scanner.expect(TokenId.LABEL);
        String label = (String)tk.value;

        BlockNode node = new BlockNode(NodeId.CONST, tk.src);
        node.type = TypeId.INT;
        node.value = label;

        tk = scanner.token();
        if (tk.id == TokenId.SCON)
        {
            Node snode = new Node(NodeId.SCON, tk.src, TypeId.ROMP, tk.value);
            node.nodes.add(snode);
            scanner.next(); // SCON
            node.type = TypeId.ROMP;
        }
        else // Integers
        {
            while (true)
            {
                node.nodes.add(OrExpr());
                if (scanner.token().id != TokenId.COMMA) break;
                scanner.next(); // ','
                node.type = TypeId.ROMP;
            }
        }

        Symbol symbol = new Symbol(node.type, StoreId.ROM);
        symbol.count = node.nodes.size();
        module.symbols.put(label, symbol);

        return node;
    }

    //-------------------------------------------------------------------------
    // Statement -> DataDecl | AssignStmt
    //            | IfStmt | LoopStmt | BreakStmt | ContinueStmt
    //            | LevelStmt | HaltStmt | TimerStmt | PauseStmt | NopStmt
    //            | ResetStmt | RestartStmt | ReturnStmt | CodeBlock ;
    //
    // BreakStmt    -> break ;
    // ContinueStmt -> continue ;
    // HaltStmt     -> halt ;
    // PauseStmt    -> pause ;
    // NopStmt      -> nop ;
    // ResetStmt    -> reset ;
    // RestartStmt  -> restart ;
    //-------------------------------------------------------------------------
    private Node Statement() throws MError
    {
        Token tk = scanner.token();
        switch (tk.id)
        {
            case INT: return DataDecl(TypeId.INT, StoreId.REG);
            case BOOL: return DataDecl(TypeId.BOOL, StoreId.BIT);
            case LABEL: return AssignStmt();
            case IF: return IfStmt();
            case LOOP: return LoopStmt();
            case BRACEL: return CodeBlock();
            case LEVEL: return LevelStmt();
            case TIMER: return TimerStmt();
            case RETURN: return ReturnStmt();

            case BREAK: 
                scanner.next(); // break
                return new Node(NodeId.BREAK, tk.src);
            case CONTINUE:
                scanner.next(); // continue
                return new Node(NodeId.CONTINUE, tk.src);
            case HALT:
                scanner.next(); // halt
                return new Node(NodeId.HALT, tk.src);
            case PAUSE:
                scanner.next(); // pause
                return new Node(NodeId.PAUSE, tk.src);
            case NOP:
                scanner.next(); // nop
                return new Node(NodeId.NOP, tk.src);
            case RESET:
                scanner.next(); // reset
                return new Node(NodeId.RESET, tk.src);
            case RESTART:
                scanner.next(); // restart
                return new Node(NodeId.RESTART, tk.src);

            default:
                System.out.println(tk.toString());
                throw new MError("Syntax error", tk.src);
        }
    }

    //-------------------------------------------------------------------------
    // DataDecl -> ( bool | int ) Label '=' LogOrExpr ;
    //-------------------------------------------------------------------------
    private Node DataDecl(TypeId type, StoreId store) throws MError
    {
        scanner.next(); // bool, int

        Token tk = scanner.token();
        scanner.expect(TokenId.LABEL);
        String label = (String)tk.value;
        if (func.symbols.containsKey(label))
            throw new MError("Duplicate label: " + label, tk.src);

        func.symbols.put(label, new Symbol(type, store));
        Node label_node = new Node(NodeId.LABEL, tk.src);
        label_node.value = label;

        tk = scanner.token();
        scanner.expect(TokenId.ASSIGN);
        if (tk.value != null)
            throw new MError("Data declaration error", tk.src);

        StmtNode node = new StmtNode(NodeId.ASSIGN, tk.src);
        node.value = MathOp.PAS;
        node.p1 = label_node;
        node.p2 = LogOrExpr();

        return node;
    }

    //-------------------------------------------------------------------------
    // AssignStmt -> LabelDest AssignOp LogOrExpr ;
    // AssignOp   -> '=' | '+=' | '-=' | '*=' | '/=' | '%='
    //             | '&=' | '|=' | '^=' | '>>=' | '<<=' ;
    // LabelDest  -> Label
    //             | Label '[' OrExpr ']'
    //             | Label '.' Label
    //             | Label '.' Label '[' OrExpr ']' ;
    //-------------------------------------------------------------------------
    private Node AssignStmt() throws MError
    {
        Node label = LabelExpr();

        Token tk = scanner.token();
        if (tk.id != TokenId.ASSIGN) return label;

        if (label.id == NodeId.CALL)
            throw new MError("Assignment to a function call", label.src);

        StmtNode assign = new StmtNode(NodeId.ASSIGN, tk.src);
        assign.value = tk.value;
        scanner.expect(TokenId.ASSIGN);

        Node expr = LogOrExpr();

        assign.p1 = label;
        assign.p2 = expr;
        assign.value = tk.value;
        if (assign.value == null) assign.value = MathOp.PAS;
        return assign;
    }

    //-------------------------------------------------------------------------
    // IfStmt     -> IfClause ElIfClause* ElseClause? ;
    // IfClause   -> if LogOrExpr Statement ;
    // ElIfClause -> elif LogOrExpr Statement ;
    // ElseClause -> else Statement ;
    //-------------------------------------------------------------------------
    private Node IfStmt() throws MError
    {
        Token tk = scanner.token();
        scanner.next(); // if, elif

        StmtNode node = new StmtNode(NodeId.IF, tk.src);
        node.p1 = LogOrExpr();
        node.p2 = Statement();

        tk = scanner.token();
        if (tk.id == TokenId.ELIF)
        {
            node.p3 = IfStmt();
        }
        else if (tk.id == TokenId.ELSE)
        {
            scanner.next(); // else
            node.p3 = Statement();
        }

        return node;
    }

    //-------------------------------------------------------------------------
    // LoopStmt -> loop Statement ;
    //-------------------------------------------------------------------------
    private Node LoopStmt() throws MError
    {
        Token tk = scanner.token();
        scanner.next(); // loop

        StmtNode node = new StmtNode(NodeId.LOOP, tk.src);
        node.p1 = Statement();

        return node;
    }

    //-------------------------------------------------------------------------
    // LevelStmt -> level IntConst ;
    //-------------------------------------------------------------------------
    private Node LevelStmt() throws MError
    {
        scanner.next(); // level
        Token tk = scanner.token();
        scanner.expect(TokenId.ICON);
        Node node = new Node(NodeId.LEVEL, tk.src, TypeId.INT, tk.value);
        return node;
    }

    //-------------------------------------------------------------------------
    // TimerStmt -> timer OrExpr ;
    //-------------------------------------------------------------------------
    private Node TimerStmt() throws MError
    {
        Token tk = scanner.token();
        scanner.next(); // timer
        StmtNode node = new StmtNode(NodeId.TIMER, tk.src);
        node.p1 = OrExpr();
        return node;
    }

    //-------------------------------------------------------------------------
    // ReturnStmt -> return ( LogAndExpr )? ;
    //-------------------------------------------------------------------------
    private Node ReturnStmt() throws MError
    {
        Token tk = scanner.token();
        scanner.next(); // return
        StmtNode node = new StmtNode(NodeId.RETURN, tk.src);
        node.type = func.type;

        if (func.type != TypeId.NONE)
        {
            node.type = func.type;
            node.p1 = LogOrExpr();
        }

        return node;
    }

    //-------------------------------------------------------------------------
    // CodeBlock -> '{' Statement* '}' ;
    //-------------------------------------------------------------------------
    private Node CodeBlock() throws MError
    {
        Token tk = scanner.token();
        scanner.expect(TokenId.BRACEL);

        BlockNode node = new BlockNode(NodeId.BLOCK, tk.src);

        while (scanner.token().id != TokenId.BRACER)
            node.nodes.add(Statement());

        scanner.expect(TokenId.BRACER);
        return node;
    }

    //-------------------------------------------------------------------------
    // LogOrExpr -> LogAndExpr (or LogAndExpr)* ;
    //-------------------------------------------------------------------------
    private Node LogOrExpr() throws MError
    {
        Node lhs = LogAndExpr();

        Token tk = scanner.token();
        while (tk.id == TokenId.MATHOP && tk.mop() == MathOp.LG_OR)
        {
            scanner.next(); // or
            StmtNode node = new StmtNode(NodeId.MATHOP, tk.src, MathOp.LG_OR);
            node.p1  = lhs;
            node.p2 = LogAndExpr();
            lhs = node;
            tk = scanner.token();
        }

        return lhs;
    }

    //-------------------------------------------------------------------------
    // LogAndExpr -> RelExpr (and RelExpr)* ;
    //-------------------------------------------------------------------------
    private Node LogAndExpr() throws MError
    {
        Node lhs = RelExpr();

        Token tk = scanner.token();
        while (tk.id == TokenId.MATHOP && tk.mop() == MathOp.LG_AND)
        {
            scanner.next(); // and
            StmtNode node = new StmtNode(NodeId.MATHOP, tk.src, MathOp.LG_AND);
            node.p1 = lhs;
            node.p2 = RelExpr();
            lhs = node;
            tk = scanner.token();
        }

        return lhs;
    }

    //-------------------------------------------------------------------------
    // RelExpr   -> OrExpr (CompareOp OrExpr)? ;
    // CompareOp -> '==' | '!=' | '>=' | '<=' | '>' | '<' ; 
    //-------------------------------------------------------------------------
    private Node RelExpr() throws MError
    {
        Node lhs = OrExpr();

        Token tk = scanner.token();
        if (tk.id == TokenId.COMPARE)
        {
            scanner.next(); // CompareOp
            StmtNode node = new StmtNode(NodeId.COMPARE, tk.src, tk.mop());
            node.p1 = lhs;
            node.p2 = OrExpr();
            lhs = node;
        }

        return lhs;
    }

    //-------------------------------------------------------------------------
    // OrExpr -> XorExpr ( '|' XorExpr )* ;
    //-------------------------------------------------------------------------
    private Node OrExpr() throws MError
    {
        Node lhs = XorExpr();

        Token tk = scanner.token();
        while (tk.id == TokenId.MATHOP && tk.mop() == MathOp.BW_OR)
        {
            scanner.next(); // '|'
            StmtNode node = new StmtNode(NodeId.MATHOP, tk.src, MathOp.BW_OR);
            node.p1 = lhs;
            node.p2 = XorExpr();
            lhs = node;
            tk = scanner.token();
        }

        return lhs;
    }

    //-------------------------------------------------------------------------
    // XorExpr -> AndExpr ( '^' AndExpr )* ; 
    //-------------------------------------------------------------------------
    private Node XorExpr() throws MError
    {
        Node lhs = AndExpr();

        Token tk = scanner.token();
        while (tk.id == TokenId.MATHOP && tk.mop() == MathOp.BW_XOR)
        {
            scanner.next(); // '^'
            StmtNode node = new StmtNode(NodeId.MATHOP, tk.src, MathOp.BW_XOR);
            node.p1 = lhs;
            node.p2 = AndExpr();
            lhs = node;
            tk = scanner.token();
        }

        return lhs;
    }

    //-------------------------------------------------------------------------
    // AndExpr -> ShiftExpr ( '&' ShiftExpr )* ;
    //-------------------------------------------------------------------------
    private Node AndExpr() throws MError
    {
        Node lhs = ShiftExpr();

        Token tk = scanner.token();
        while (tk.id == TokenId.MATHOP && tk.mop() == MathOp.BW_AND)
        {
            scanner.next(); // '&'
            StmtNode node = new StmtNode(NodeId.MATHOP, tk.src, MathOp.BW_AND);
            node.p1 = lhs;
            node.p2 = ShiftExpr();
            lhs = node;
            tk = scanner.token();
        }

        return lhs;
    }

    //-------------------------------------------------------------------------
    // ShiftExpr -> AddExpr ( ( '<<' | '>>' ) AddExpr )? ;
    //-------------------------------------------------------------------------
    private Node ShiftExpr() throws MError
    {
        Node lhs = AddExpr();

        Token tk = scanner.token();
        if (tk.id == TokenId.MATHOP && 
            (tk.mop() == MathOp.SHR || tk.mop() == MathOp.SHL))
        {
            scanner.next(); // '<<', '>>'
            StmtNode node = new StmtNode(NodeId.MATHOP, tk.src, tk.mop());
            node.p1 = lhs;
            node.p2 = AddExpr();
            lhs = node;
        }

        return lhs;
    }

    //-------------------------------------------------------------------------
    // AddExpr -> MultExpr ( ( '+' | '-' ) MultExpr )* ;
    //-------------------------------------------------------------------------
    private Node AddExpr() throws MError
    {
        Node lhs = MultExpr();

        Token tk = scanner.token();
        while (tk.id == TokenId.MATHOP && 
            (tk.mop() == MathOp.ADD || tk.mop() == MathOp.SUB))
        {
            scanner.next(); // '+', '-'
            StmtNode node = new StmtNode(NodeId.MATHOP, tk.src, tk.mop());
            node.p1 = lhs;
            node.p2 = MultExpr();
            lhs = node;
            tk = scanner.token();
        }

        return lhs;
    }

    //-------------------------------------------------------------------------
    // MultExpr -> UnaryExpr ( ( '*' | '/' | '%' ) UnaryExpr )* ;
    //-------------------------------------------------------------------------
    private Node MultExpr() throws MError
    {
        Node lhs = UnaryExpr();

        Token tk = scanner.token();
        while (tk.id == TokenId.MATHOP && 
              (tk.mop() == MathOp.MUL ||
               tk.mop() == MathOp.DIV ||
               tk.mop() == MathOp.MOD))
        {
            scanner.next(); // '*', '/', '%'
            StmtNode node = new StmtNode(NodeId.MATHOP, tk.src, tk.mop());
            node.p1 = lhs;
            node.p2 = UnaryExpr();
            lhs = node;
            tk = scanner.token();
        }

        return lhs;
    }

    //-------------------------------------------------------------------------
    // UnaryExpr -> ( '+' | '-' | '~' | not )? Atom ;
    //-------------------------------------------------------------------------
    private Node UnaryExpr() throws MError
    {
        Token tk = scanner.token();

        if (tk.id == TokenId.MATHOP)
        {
            scanner.next(); // '+', '-', '~', not
            Node node = Atom();
            StmtNode node1;

            switch (tk.mop())
            {
                case ADD:
                    return node;

                case SUB:
                    if (node.id == NodeId.ICON)
                    {
                        node.value = -node.ival();
                        return node;
                    }
                    else
                    {
                        node1 = new StmtNode(NodeId.MATHOP, tk.src,
                                             MathOp.NEG);
                        node1.p1 = node;
                        return node1;
                    }

                case BW_NOT:
                    if (node.id == NodeId.ICON)
                    {
                        node.value = ~node.ival();
                        return node;
                    }
                    else
                    {
                        node1 = new StmtNode(NodeId.MATHOP, tk.src,
                                             MathOp.BW_NOT);
                        node1.p1 = node;
                        return node1;
                    }

                case LG_NOT:
                    if (node.id == NodeId.BCON)
                    {
                        node.value = !node.bval();
                        return node;
                    }
                    else
                    {
                        node1 = new StmtNode(NodeId.MATHOP, tk.src,
                                             MathOp.LG_NOT);
                        node1.p1 = node;
                        return node1;
                    }

                default: throw new MError("Invalid unary expression", tk.src);
            }
        }

        return Atom();
    }

    //-------------------------------------------------------------------------
    // Atom -> LabelExpr | BoolConst | IntConst
    //       | '(' LogOrExpr ')' ;
    //-------------------------------------------------------------------------
    private Node Atom() throws MError
    {
        Token tk = scanner.token();

        switch (tk.id)
        {
            case LABEL:
                return LabelExpr();

            case BCON:
                scanner.next(); // BCON
                return new Node(NodeId.BCON, tk.src, TypeId.BOOL, tk.value);

            case ICON:
                scanner.next(); // ICON
                return new Node(NodeId.ICON, tk.src, TypeId.INT, tk.value);

            case PARENL:
                scanner.next(); // '('
                Node node = LogOrExpr();
                scanner.expect(TokenId.PARENR);
                return node;

            default: throw new MError("Invalid expression atom", tk.src);
        }
    }

    //-------------------------------------------------------------------------
    // LabelExpr -> Label
    //            | Label '[' OrExpr ']'
    //            | Label '(' LogOrExpr# ')'
    //            | Label '.' Label
    //            | Label '.' Label '[' OrExpr ']'
    //            | Label '.' Label '(' LogOrExpr# ')' ;
    //-------------------------------------------------------------------------
    private Node LabelExpr() throws MError
    {
        Token tklabel = scanner.token();
        String label = (String)tklabel.value;
        scanner.next(); // LABEL

        Token tk = scanner.token();
        if (tk.id == TokenId.PERIOD)
        {
            if (files_to_read.contains(label) == false &&
                scanner.file_names.contains(label + ".m") == false)
            {
                files_to_read.add(label);
            }

            scanner.next(); // '.'
            tk = scanner.token();
            scanner.expect(TokenId.LABEL);
            label += '.' + (String)tk.value;
            tk = scanner.token();
        }
        else
        {
            if (tk.id != TokenId.PARENL)
                CheckLabel(tklabel);
        }

        if (tk.id == TokenId.BRACKL)
        {
            scanner.next(); // '['
            StmtNode array_node = new StmtNode(NodeId.ARRAY, tk.src);
            array_node.value = label;
            array_node.p1 = OrExpr();
            scanner.expect(TokenId.BRACKR);
            return array_node;
        }
        else if (tk.id == TokenId.PARENL)
        {
            scanner.next(); // '('
            BlockNode call_node = new BlockNode(NodeId.CALL, tk.src);
            if (label.indexOf('.') < 0) label = module_name + '.' + label;
            call_node.value = label;

            boolean not_first = false;
            while (scanner.token().id != TokenId.PARENR)
            {
                if (not_first)
                    scanner.expect(TokenId.COMMA);
                not_first = true;
                call_node.nodes.add(LogOrExpr());
            }
            scanner.expect(TokenId.PARENR);
            return call_node;
        }
        else
        {
            Node label_node =  new Node(NodeId.LABEL, tk.src);
            label_node.value = label;
            return label_node;
        }
    }

    //-------------------------------------------------------------------------
    private void CheckLabel(Token tk) throws MError
    {
        String label = (String)tk.value;

        if (func.symbols.containsKey(label)) return;
        if (module.symbols.containsKey(label)) return;

        String msg = String.format("Label not declared: %s", label);
        throw new MError(msg, tk.src);
    }
}