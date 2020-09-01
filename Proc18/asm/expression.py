#----------------------------------------------------------------------------
# Company:     Christle Engineering
# Engineer:    Mike Christle
# Module Name: Expression Evaluator
#
# History: 
# 0.1.0   08/16/2020   File Created
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------------------------
# OrExpr    -> XorExpr ( '|' XorExpr )* ;
#
# XorExpr   -> AndExpr ( '^' AndExpr )* ; 
#
# AndExpr   -> ShiftExpr ( '&' ShiftExpr )* ;
#
# ShiftExpr -> AddExpr ( ( '<<' | '>>' ) AddExpr )? ;
#
# AddExpr   -> MultExpr ( ( '+' | '-' ) MultExpr )* ;
#
# MultExpr  -> UnaryExpr ( ( '*' | '/' | '%' ) UnaryExpr )* ;
#
# UnaryExpr -> ( '+' | '-' | '~' )? Atom ;
#
# Atom      -> Label
#            | IntConst
#            | '(' OrExpr ')' ;
#
# IntConst  -> [0-9]+
#            | '0x' [0-9a-fA-F]+
#            | '0o' [0-7]+
#            | '0b' [01]+
#            | ''' [ASCII Char] ''' ;
#----------------------------------------------------------------------------
# Copyright 2020 Mike Christle
#
# Permission is hereby granted, free of charge, to any person
# obtaining a copy of this software and associated documentation
# files (the "Software"), to deal in the Software without
# restriction, including without limitation the rights to use,
# copy, modify, merge, publish, distribute, sublicense, and/or
# sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following
# conditions:
#
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
# OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
# HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
# WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.
#----------------------------------------------------------------------------

from exceptions import Error
from enum import Enum

class Tokens(Enum):
    NONE        = 0
    VALUE       = 1
    PLUS        = 2
    MINUS       = 3
    NOT         = 4
    AND         = 5
    OR          = 6
    XOR         = 7
    MULTIPLY    = 8
    DIVIDE      = 9
    MODULUS     = 10
    SHIFT_RIGHT = 11
    SHIFT_LEFT  = 12
    PAREN_LEFT  = 13
    PAREN_RIGHT = 14

    def __str__(self):
        return self.name
    def __repr__(self):
        return self.name

element_list = []
token_index = 0
token_value = 0
token = Tokens.NONE

#----------------------------------------------------------------------------
def ParseExpr(symbol_table, equation, line_no):
    global symbols, equation_string, err_line_no, token_index

    symbols = symbol_table
    err_line_no = line_no

    s = ' ' + equation + ' '
    equation_string = s.replace('\t', ' ')

    ParseString(equation_string)

    token_index = -1
    GetToken()

    op1 = OrExpr()
    element_list.clear()
    return op1

#----------------------------------------------------------------------------
# OrExpr    -> XorExpr ( '|' XorExpr )* ;
#----------------------------------------------------------------------------
def OrExpr():
    op1 = XorExpr();

    while token == Tokens.OR:
        GetToken(); # '|'
        op1 = op1 | XorExpr()

    return op1

#----------------------------------------------------------------------------
# XorExpr   -> AndExpr ( '^' AndExpr )* ; 
#----------------------------------------------------------------------------
def XorExpr():
    op1 = AndExpr();

    while token == Tokens.XOR:
        GetToken(); # '^'
        op1 = op1 ^ AndExpr()

    return op1

#----------------------------------------------------------------------------
# AndExpr   -> ShiftExpr ( '&' ShiftExpr )* ;
#----------------------------------------------------------------------------
def AndExpr():
    op1 = ShiftExpr();

    while token == Tokens.AND:
        GetToken(); # '&'
        op1 = op1 & ShiftExpr()

    return op1

#----------------------------------------------------------------------------
# ShiftExpr -> AddExpr ( ( '<<' | '>>' ) AddExpr )? ;
#----------------------------------------------------------------------------
def ShiftExpr():

    op1 = AddExpr()

    while token == Tokens.SHIFT_LEFT or token == Tokens.SHIFT_RIGHT:
        tk = token
        GetToken() # '<<' '>>'

        if tk == Tokens.SHIFT_LEFT:
            op1 = op1 << AddExpr()

        elif tk == Tokens.SHIFT_RIGHT:
            op1 = op1 >> AddExpr()

    return op1

#----------------------------------------------------------------------------
# AddExpr   -> MultExpr ( ( '+' | '-' ) MultExpr )* ;
#----------------------------------------------------------------------------
def AddExpr():
    op1 = MultExpr();

    while token == Tokens.PLUS or token == Tokens.MINUS:
        tk = token
        GetToken(); # '+' '-'

        if tk == Tokens.PLUS:
            op1 = op1 + MultExpr()

        elif tk == Tokens.MINUS:
            op1 = op1 - MultExpr()

    return op1

#----------------------------------------------------------------------------
# MultExpr  -> UnaryExpr ( '*' UnaryExpr )* ;
#----------------------------------------------------------------------------
def MultExpr():
    op1 = UnaryExpr()

    while token == Tokens.MULTIPLY or token == Tokens.DIVIDE or token == Tokens.MODULUS:
        tk = token
        GetToken() # '*' '/' '%'

        if tk == Tokens.MULTIPLY:
            op1 = op1 * UnaryExpr()

        elif tk == Tokens.DIVIDE:
            op1 = op1 // UnaryExpr()

        elif tk == Tokens.MODULUS:
            op1 = op1 % UnaryExpr()

    return op1

#----------------------------------------------------------------------------
# UnaryExpr -> ( '+' | '-' | '~' )? Atom ;
#----------------------------------------------------------------------------
def UnaryExpr():

    if token == Tokens.NOT:
        GetToken() # '~'
        return ~Atom()

    if token == Tokens.MINUS:
        GetToken() # '-'
        return -Atom()

    if token == Tokens.PLUS:
        GetToken() # '+'
        return Atom()

    return Atom()

#----------------------------------------------------------------------------
# Atom      -> Label
#            | IntConst
#            | '(' OrExpr ')' ;
#
# IntConst  -> [0-9]+
#            | '0x' [0-9a-fA-F]+
#            | '0o' [0-7]+
#            | '0b' [01]+
#            | ''' [ASCII Char] ''' ;
#----------------------------------------------------------------------------
def Atom():

    if token == Tokens.VALUE:
        op1 = token_value
        GetToken() # VALUE

    elif token == Tokens.PAREN_LEFT:
        GetToken() # '('
        op1 = OrExpr()
        if token != Tokens.PAREN_RIGHT:
            raise Error("EP01 Missing right paren", err_line_no)

        GetToken() # ')'

    else:
        raise Error("EP02 Invalid expression", err_line_no)

    return op1

#----------------------------------------------------------------------------
def GetToken():
    global token_index, token, token_value

    token_index += 1
    list_len = len(element_list)

    if token_index > list_len:
        raise Error("EP03 Invalid expression", err_line_no)

    if token_index == list_len:
        token = Tokens.NONE
        token_value = 0

    else:
        token = element_list[token_index][0]
        token_value = element_list[token_index][1]

#----------------------------------------------------------------------------
def ParseString(equation_string):

    HEX_DIGITS = "0123456789ABCDEF"

    c = ' '
    value = 0
    index = 0
    equ_len = len(equation_string)

    if equ_len == 0:
        raise Error("EP04 NULL equation string", err_line_no)

    while index < equ_len:
        c = equation_string[index]
        index += 1

        if c.isalpha():
            str = ''

            while index < equ_len:
                str += c
                c = equation_string[index]
                if not c.isalpha() and not c.isnumeric() and c != '_':
                    break
                index += 1

            if str not in symbols:
                raise Error("EP05 Symbol not defined: " + str, err_line_no)

            value = symbols[str]
            element_list.append((Tokens.VALUE, value))

        elif c.isdigit():
            value = 0
            count = 0
            next_c = equation_string[index]

            # Hexidecimal
            if c == '0' and next_c == 'X':
                index += 1
                c = equation_string[index]
                while index < equ_len:
                    c = equation_string[index]
                    if c not in HEX_DIGITS and c != '_': break
                    index += 1
                    if c != '_':
                        count += 1
                        value <<= 4
                        value |= HEX_DIGITS.index(c)

                if count == 0:
                    raise Error("EP06 Invalid hex number", err_line_no)

            # Octal
            elif c == '0' and next_c == 'O':
                index += 1
                c = equation_string[index]
                while index < equ_len:
                    c = equation_string[index]
                    if (c < '0' or c > '7') and c != '_': break
                    index += 1
                    if c != '_':
                        count += 1
                        value <<= 3
                        value |= HEX_DIGITS.index(c)

                if count == 0:
                    raise Error("EP06 Invalid hex number", err_line_no)

            # Binary
            elif c == '0' and next_c == 'B':
                index += 1
                c = equation_string[index]
                while index <= equ_len:
                    if c != '0' and c != '1' and c != '_': break
                    index += 1
                    if c != '_':
                        value <<= 1
                        if c == '1': value |= 1
                        count += 1
                    c = equation_string[index]

                if count == 0:
                    raise Error("EP06 Invalid hex number", err_line_no)

            # Decimal
            else:
                while index < equ_len:
                    if c != '_':
                        value *= 10
                        value += ord(c) & 0x0F
                    c = equation_string[index]
                    if not c.isdigit() and c != '_': break
                    index += 1

            element_list.append((Tokens.VALUE, value))

        elif c == '\'':
            value = ord(equation_string[index])
            element_list.append((Tokens.VALUE, value))
            index += 1

            if equation_string[index] != '\'':
                raise Error("EP07 Missing char constant quote", err_line_no)
            index += 1

        elif c == '+':
            element_list.append((Tokens.PLUS, 0))

        elif c == '-':
            element_list.append((Tokens.MINUS, 0))

        elif c == '&':
            element_list.append((Tokens.AND, 0))

        elif c == '|':
            element_list.append((Tokens.OR, 0))

        elif c == '^':
            element_list.append((Tokens.XOR, 0))

        elif c == '~':
            element_list.append((Tokens.NOT, 0))

        elif c == '*':
            element_list.append((Tokens.MULTIPLY, 0))

        elif c == '/':
            element_list.append((Tokens.DIVIDE, 0))

        elif c == '%':
            element_list.append((Tokens.MODULUS, 0))

        elif c == '(':
            element_list.append((Tokens.PAREN_LEFT, 0))

        elif c == ')':
            element_list.append((Tokens.PAREN_RIGHT, 0))

        elif c == '<' and equation_string[index] == '<':
                element_list.append((Tokens.SHIFT_LEFT, 0))
                index += 1

        elif c == '>' and equation_string[index] == '>':
                element_list.append((Tokens.SHIFT_RIGHT, 0))
                index += 1

        elif c == ' ':
            pass

        else:
            raise Error("EP08 Invalid expression", err_line_no)
