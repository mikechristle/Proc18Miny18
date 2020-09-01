#----------------------------------------------------------------------------
# Company:     Christle Engineering
# Engineer:    Mike Christle
# Module Name: Proc18 Assembler
#
# History: 
# 0.1.0   08/16/2020   File Created
# 1.0.0   09/01/2020   Initial release
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

import sys
import os.path
import re

from exceptions import Error
from os.path import isfile
from expression import ParseExpr

OP1 = 1
OP2 = 2
ADR = 3
IMM = 4
DWL = 5
DSL = 6

line_no = 0
max_adrs = 0
parts = []
code_adrs = 0
const_adrs = 0
data_adrs = 0
symbols = {}
# addresses = {}
op_table = {}
bin_code = []
file_names = []

#----------------------------------------------------------------------------
def fill_symbols():
    for idx in range(len(bin_code)):
        code = bin_code[idx]

        if code[0] == ADR:
            label = code[4]
            if label not in symbols:
                raise Error('Label not defined: ' + label, code[1])
            value = symbols[label]
            bin_code[idx][3] |= value
            bin_code[idx][0] = OP1

        if code[0] == IMM:
            label = code[4]
            if label not in symbols:
                raise Error('Label not defined: ' + label, code[1])
            value = symbols[label]
            bin_code[idx][4] = value
            bin_code[idx][0] = OP2

#----------------------------------------------------------------------------
def fill_code_buffer():
    global max_adrs

    max_adrs = 0
    for code in bin_code:

        if code[0] != DWL and code[0] != DSL:
            adrs = code[2]
            if adrs >= rom_size:
                raise Error('Address out of range', code[1])

            code[3] = load_code_buffer(adrs, code[3], code[1])
            adrs += 1

            if code[0] == OP2:
                code[4] = load_code_buffer(adrs, code[4], code[1])
                adrs += 1

            if adrs > max_adrs:
                max_adrs = adrs

    for idx in range(rom_size):
        if code_buffer[idx] == None:
            code_buffer[idx] = 0

#----------------------------------------------------------------------------
def load_code_buffer(adr, value, line_no):
    if code_buffer[adr] != None:
        raise Error('Memory conflict', adr)
    value &= 0x3FFFF
    code_buffer[adr] = value
    return value

#----------------------------------------------------------------------------
def fill_const_buffer():
    for code in bin_code:
        if code[0] == DWL:
            adrs = code[2]
            if adrs >= rom_size:
                raise Error('Address out of range', code[1])

            for idx in range(3, len(code)):
                code[idx] = load_const_buffer(adrs, code[idx], code[1])
                adrs += 1

    for idx in range(rom_size):
        if const_buffer[idx] == None:
            const_buffer[idx] = 0

#----------------------------------------------------------------------------
def load_const_buffer(adr, value, line_no):
    if const_buffer[adr] != None:
        raise Error('Memory conflict', line_no)
    value &= 0x3FFFF
    const_buffer[adr] = value
    return value

#----------------------------------------------------------------------------
def dump_hex_file(file_name):

    with open('code.hex', 'w') as hex_file:
        for idx in range(rom_size):
            str = '{0:05X} '.format(code_buffer[idx])
            hex_file.write(str)
            if idx % 16 == 15:
                hex_file.write('\n')

    with open('const.hex', 'w') as hex_file:
        for idx in range(rom_size):
            str = '{0:05X} '.format(const_buffer[idx])
            hex_file.write(str)
            if idx % 16 == 15:
                hex_file.write('\n')

#----------------------------------------------------------------------------
def dump_lst_file(file_name):
    fmt0 = '                  : {0}'
    fmt1 = '{0:03o} {1:06o}        : {2}'
    fmt2 = '{0:03o} {1:06o} {2:06o} : {3}'
    fmt3 = '{0:03o}  '
    fmt4 = '{0:03o}  '
    fmt5 = '{0:03o}               : {1}'

    lst_file_name = change_ext(file_name, 'lst')
    tmp_file_name = change_ext(file_name, 'tmp')

    in_file = open(tmp_file_name, 'r')
    out_file = open(lst_file_name, 'w')

    line_no = 1

    for code in bin_code:
        while line_no < code[1]:
            sline = in_file.readline()
            str = fmt0.format(sline)
            out_file.write(str)
            line_no += 1

        sline = in_file.readline()
        if len(sline) == 0: break
        line_no += 1

        if code[0] == OP1:
            str = fmt1.format(code[2], code[3], sline)
        elif code[0] == OP2:
            str = fmt2.format(code[2], code[3], code[4], sline)
        elif code[0] == DSL:
            str = fmt5.format(code[2], sline)
        else:
            str = fmt0.format(sline)
            out_file.write(str)
            str = fmt3.format(code[2]) + ' '
            for i in range(3, len(code)):
                str += fmt4.format(code[i])
            str += '\n'

        out_file.write(str)

    while True:
        sline = in_file.readline()
        if not sline: break
        str = fmt0.format(sline)
        out_file.write(str)

    in_file.close()
    out_file.close()

#----------------------------------------------------------------------------
def read_file(file_name):
    global line_no

    if file_name in file_names:
        return

    file_names.append(file_name)

    if not isfile(file_name):
        raise Error('File not found: ' + file_name, 0)

    with open(file_name, 'r') as in_file:
        while True:
            sline = in_file.readline()
            if len(sline) == 0: break

            temp_file.write(sline)
            line_no += 1
            process_line(sline)

#----------------------------------------------------------------------------
def process_line(sline):
    global parts, parms, code_adrs, const_adrs

    parts = parse_line(sline)
    if len(parts) == 0: return

    if parts[0] == 'INCLUDE':
        file_name = parts[2][1:-1]
        read_file(file_name)
        return

    if parts[0] != None:
        if parts[0] in symbols:
            raise Error('Duplicate label: ' + parts[0], line_no)
        symbols[parts[0]] = code_adrs

    if len(parts[1]) == 0: return

    if parts[1] not in op_table:
        raise Error('Invalid opcode: ' + parts[1], line_no)

    parms = parts[2].split(',')

    op = op_table[parts[1]]
    bin_line = op[0](op[1])
    if bin_line != None:
        bin_code.append(bin_line)
        opc = bin_line[0]

        if opc == OP1 or opc == ADR:
            code_adrs += 1
        elif opc == OP2 or opc == IMM:
            code_adrs += 2

#----------------------------------------------------------------------------
def parse_line(sline):
    global parts

    idx = sline.find(";")
    if idx >= 0:
        sline = sline[0:idx]
    sline = sline.rstrip()

    parts = []
    if len(sline) == 0: return parts

    idx = 0

    # Parse the label
    if sline[0].isalpha():
        label = ''
        while idx < len(sline):
            ch = sline[idx]
            if not ch.isalnum() and ch not in '_.:':
                break
            label += ch
            idx += 1
        parts.append(label.upper())
    else:
        parts.append(None)

    # Skip whitespace
    while idx < len(sline) and sline[idx].isspace():
        idx += 1

    # Parse opcode
    label = ''
    while idx < len(sline) and sline[idx].isalpha():
        label += sline[idx]
        idx += 1
    parts.append(label.upper())

    # Skip whitespace
    while idx < len(sline) and sline[idx].isspace():
        idx += 1

    # Parse parameters
    label = ''
    while idx < len(sline):
        ch = sline[idx]

        if ch == "'" or ch == '"':
            while idx < len(sline):
                label += sline[idx]
                idx += 1
                if idx >= len(sline):
                    raise Error('Invalid string constant', line_no)
                if sline[idx] == ch:
                    label += ch
                    break

        elif not ch.isspace():
            label += ch.upper()
        idx += 1

    parts.append(label)

    return parts

#----------------------------------------------------------------------------
# ORG Address
#----------------------------------------------------------------------------
def _org(_):
    global code_adrs

    code_adrs = parse_int(parts[2])
    return None

#----------------------------------------------------------------------------
def _simple(opcode):
    return [OP1, line_no, code_adrs, opcode]

#----------------------------------------------------------------------------
def _clr(opcode):
    if len(parms) != 1:
        raise Error('Syntax error', line_no)

    regno = parse_reg(parms[0])
    return [OP1, line_no, code_adrs, 0o05_00_00 | regno]

#----------------------------------------------------------------------------
# LEVEL N - 00 05 0N
#----------------------------------------------------------------------------
def _level(_):
    if len(parms) != 1:
        raise Error('Syntax error', line_no)

    N = parse_int(parts[2])
    if N > 15 or N < 0:
        raise Error('LEVEL value out of range', line_no)

    return [OP1, line_no, code_adrs, 0o00_05_00 | N]

#----------------------------------------------------------------------------
# TIMER N - 05 NN NN
#----------------------------------------------------------------------------
def _timer(_):
    if len(parms) != 1:
        raise Error('Syntax error', line_no)

    N = parse_int(parts[2])
    if N > 0x0FFF or N < 0:
        raise Error('TIMER value out of range', line_no)

    return [OP1, line_no, code_adrs, 0o05_00_00 | N]

#----------------------------------------------------------------------------
# MOV D, S    - 41 SS DD
# MOV D, #N   - 00 07 DD   Value
# MOV D, #N   - 61 NN DD
#----------------------------------------------------------------------------
def _mov(_):
    if len(parms) != 2:
        raise Error('Syntax error', line_no)

    op = OP2
    dreg = parse_reg(parms[0])

    if parms[1][0] == '#':
        value = parms[1][1:]

        if value[0].isalpha():
            if value in symbols:
                value = symbols[value]
            else:
                raise Error('Symbol not defined: ' + value, line_no)
        else:
            value = parse_int(value)

        if op == OP2 and value >= 0 and value < 64:
            opcode = 0o61_00_00 | dreg | (value << 6)
            return [OP1, line_no, code_adrs, opcode]
        else:
            opcode = 0o00_07_00 | dreg
            return [op, line_no, code_adrs, opcode, value]

    else:
        sreg = parse_reg(parms[1])

        opcode = 0o42_00_00 | dreg | (sreg << 6)
        return [OP1, line_no, code_adrs, opcode]

#----------------------------------------------------------------------------
# CMP Rd, Rs - 04 SS DD
#----------------------------------------------------------------------------
def _cmp(_):
    if len(parms) != 2 or len(parms[0]) == 0 or len(parms[1]) == 0:
        raise Error('Syntax error', line_no)

    dreg = parse_reg(parms[0])
    sreg = parse_reg(parms[1])

    if sreg == 63:
        raise Error('Syntax error', line_no)

    opcode = 0o04_00_00 | dreg | (sreg << 6)
    return [OP1, line_no, code_adrs, opcode]

#----------------------------------------------------------------------------
# CPI Rd, VALUE - 04 77 DD   VALUE
#----------------------------------------------------------------------------
def _cpi(_):
    if len(parms) != 2 or len(parms[0]) == 0 or len(parms[1]) == 0:
        raise Error('Syntax error', line_no)

    dreg = parse_reg(parms[0])
    value = parms[1]

    if value[0].isalpha():
        if value not in symbols:
            raise Error('Symbol not defined: ' + value, line_no)
        value = symbols[value]
    else:
        value = parse_int(value)

    opcode = 0o04_77_00 | dreg
    return [OP2, line_no, code_adrs, opcode, value]

#----------------------------------------------------------------------------
# OP D  - 4M DD DD
#----------------------------------------------------------------------------
def _alu1(opcode):
    if len(parms) != 1:
        raise Error('Syntax error', line_no)

    dreg = parse_reg(parms[0])
    opcode |= dreg | (dreg << 6)

    return [OP1, line_no, code_adrs, opcode]

#----------------------------------------------------------------------------
# OP D, S  - 4M SS DD
# OP D, S  - 5M SS DD
# OP D, #N - 6M NN DD
# OP D, #N - 7M NN DD
#----------------------------------------------------------------------------
def _alu2(opcode):
    if len(parms) != 2:
        raise Error('Syntax error', line_no)

    dreg = parse_reg(parms[0])

    if parms[1][0] == '#':
        sreg = parse_int(parms[1][1:])
        opcode |= 0o20_00_00
        if sreg < 0 or sreg > 63:
            raise Error('Constant out of range', line_no)
    else:
        sreg = parse_reg(parms[1])

    opcode |= sreg << 6
    opcode |= dreg

    return [OP1, line_no, code_adrs, opcode]

#----------------------------------------------------------------------------
# BTST Bn - 30 00 NN
# BCLR Bn - 31 00 NN
# BSET Bn - 32 00 NN
#----------------------------------------------------------------------------
def _bit(opcode):
    if len(parms) != 1:
        raise Error('Syntax error', line_no)

    value = parse_int(parms[0])

    if value < 0 or value > 63:
        raise Error('Invalid bit number', line_no)

    opcode = opcode | value | (value << 6)
    return [OP1, line_no, code_adrs, opcode | value]

#----------------------------------------------------------------------------
# BCMP D, S - 30 SS DD
# BMOV D, S - 31 SS DD
# BNOT D, S - 32 SS DD
# BAND D, S - 35 SS DD
# BOR  D, S - 36 SS DD
# BXOR D, S - 37 SS DD
# IN   D, P - 06 PP DD
# OUT  P, S - 07 PP SS
#----------------------------------------------------------------------------
def _simple2(opcode):
    if len(parms) != 2:
        raise Error('Syntax error', line_no)

    dst = parse_int(parms[0])
    src = parse_int(parms[1])

    if src < 0 or src > 63 or dst < 0 or dst > 63:
        raise Error('Range error', line_no)

    opcode |= dst
    opcode |= src << 6
    return [OP1, line_no, code_adrs, opcode]

#----------------------------------------------------------------------------
# Sxx Bd - 00 1X DD
#----------------------------------------------------------------------------
def _sxx(opcode):
    if len(parms) != 1:
        raise Error('Syntax error', line_no)

    dst = parse_int(parms[0])
    if dst < 0 or dst > 63:
        raise Error('Invalid bit number', line_no)

    return [OP1, line_no, code_adrs, opcode | dst]

#----------------------------------------------------------------------------
# LDR  D, (S)   - 02 SS DD
# LDC  D, (S)   - 04 SS DD
#----------------------------------------------------------------------------
def _ldr(opcode):
    if len(parms) != 2:
        raise Error('Syntax error', line_no)

    if len(parms[1]) < 3 or \
       parms[1][0] != '(' or \
       parms[1][-1] != ')':
         raise Error('Syntax error', line_no)

    dreg = parse_reg(parms[0])
    sreg = parse_reg(parms[1][1:-1])

    opcode |= sreg << 6
    opcode |= dreg

    return [OP1, line_no, code_adrs, opcode]

#----------------------------------------------------------------------------
# STR   (D), S - 03 SS DD
#----------------------------------------------------------------------------
def _str(_):
    if len(parms) != 2:
        raise Error('Syntax error', line_no)

    if len(parms[0]) < 3 or \
       parms[0][0] != '(' or \
       parms[0][-1] != ')':
         raise Error('Syntax error', line_no)

    dreg = parse_reg(parms[0][1:-1])
    sreg = parse_reg(parms[1])

    opcode  = 0o03_00_00
    opcode |= sreg << 6
    opcode |= dreg

    return [OP1, line_no, code_adrs, opcode]

#----------------------------------------------------------------------------
# CALL LABEL - 01 AA AA
#----------------------------------------------------------------------------
def _call(_):
    if len(parms) != 1:
        raise Error('Syntax error', line_no)

    return [ADR, line_no, code_adrs, 0o01_00_00, parts[2]]

#----------------------------------------------------------------------------
# Jxx  LABEL - 2C AA AA
#----------------------------------------------------------------------------
def _jxx(opcode):
    if len(parms) != 1:
        raise Error('Syntax error', line_no)

    return [ADR, line_no, code_adrs, opcode, parts[2]]

#----------------------------------------------------------------------------
# DC ( VALUE | LABEL ) [ ',' ( VALUE | LABEL ) ]*
#----------------------------------------------------------------------------
def _dc(_):
    global const_adrs

    if parts[0] != None:
        symbols[parts[0]] = const_adrs

    line = [DWL, line_no, const_adrs]

    for x in parms:
        value = parse_int(x)
        line.append(value)

    const_adrs += len(line) - 3
    return line

#----------------------------------------------------------------------------
# DS SIZE
#----------------------------------------------------------------------------
def _ds(_):
    global data_adrs

    if parts[0] != None:
        symbols[parts[0]] = data_adrs
    else:
        raise Error('DS requires a label', line_no)

    if len(parms) != 1:
        raise Error('Syntax error', line_no)

    line = [DSL, line_no, data_adrs]
    data_adrs += parse_int(parms[0])

    return line

#----------------------------------------------------------------------------
# LABEL EQU VALUE
#----------------------------------------------------------------------------
def _equ(_):
    if parts[0] == None:
        raise Error('Error: EQU requires a label', line_no)

    symbols[parts[0]] = parse_int(parts[2])
    return None

#----------------------------------------------------------------------------
def parse_reg(str):

    if str[0].isalpha():
        if str not in symbols:
            raise Error('Invalid register: ' + str, line_no)
        reg = symbols[str]
    else:
        reg = int(str)

    if reg < 0 or reg > 63:
        raise Error('Invalid register: ' + str, line_no)

    return reg

#----------------------------------------------------------------------------
def parse_int(str):

    length = len(str)
    if length == 0:
        raise Error('Invalid integer', line_no)

    if str in symbols:
        return symbols[str]

    if length == 3 and str[0] == "'" and str[2] == "'":
        return ord(str[1])

    return ParseExpr(symbols, str, line_no)

    sign = 1
    if str[0] == '-':
        str = str[1:]
        sign = -1

    num_bases = {'0X':16, '0O':8, '0B':2}

    if len(str) > 2 and str[0:2] in num_bases:
        num_base = num_bases[str[0:2]]
        str = str[2:]
    else:
        num_base = 10

    alphabet = '_0123456789ABCDEF'[:num_base + 1]
    label = ''

    idx = 0
    while idx < len(str):
        c = str[idx]
        if c not in alphabet:
            break
        label += c
        idx += 1

    if len(label) == 0:
        raise Error('Invalid integer: ' + str, line_no)

    value = int(label, num_base) * sign
    if value > 0x3FFFF or value < -131071:
        raise Error('Value out of range: {0}'.format(value), line_no)

    return value

#----------------------------------------------------------------------------
def change_ext(file_name, ext):
    idx = file_name.find('.')
    return file_name[:idx] + '.' + ext

#----------------------------------------------------------------------------
op_table = {
    'DC'     : (_dc, 0),
    'DS'     : (_ds, 0),
    'EQU'    : (_equ, 0),
    'ORG'    : (_org, 0),

    'NOP'    : (_simple, 0o00_00_00),
    'HALT'   : (_simple, 0o00_01_00),
    'PAUSE'  : (_simple, 0o00_02_00),
    'RTS'    : (_simple, 0o00_03_00),
    'RTI'    : (_simple, 0o00_04_00),
    'RESET'  : (_simple, 0o00_06_00),
    'RESTART': (_simple, 0o20_00_00),

    'LEVEL'  : (_level, 0),
    'TIMER'  : (_timer, 0),

    'JMP'    : (_jxx, 0o20_00_00),
    'JEQ'    : (_jxx, 0o22_00_00),
    'JBS'    : (_jxx, 0o22_00_00),
    'JNE'    : (_jxx, 0o23_00_00),
    'JBC'    : (_jxx, 0o23_00_00),
    'JLT'    : (_jxx, 0o24_00_00),
    'JGT'    : (_jxx, 0o25_00_00),
    'JLE'    : (_jxx, 0o26_00_00),
    'JGE'    : (_jxx, 0o27_00_00),

    'SEQ'    : (_sxx, 0o12_00_00),
    'SNE'    : (_sxx, 0o13_00_00),
    'SLT'    : (_sxx, 0o14_00_00),
    'SGT'    : (_sxx, 0o15_00_00),
    'SLE'    : (_sxx, 0o16_00_00),
    'SGE'    : (_sxx, 0o17_00_00),

    'CALL'   : (_call, 0),
    'CMP'    : (_cmp, 0),
    'IN'     : (_simple2, 0o06_00_00),
    'OUT'    : (_simple2, 0o07_00_00),

    'MOV'    : (_mov, 0),

    'BCMP'   : (_simple2, 0o30_00_00),
    'BMOV'   : (_simple2, 0o31_00_00),
    'BTST'   : (_bit, 0o31_00_00),
    'BCLR'   : (_bit, 0o32_00_00),
    'BSET'   : (_bit, 0o33_00_00),
    'BNOT'   : (_simple2, 0o32_00_00),
    'BAND'   : (_simple2, 0o35_00_00),
    'BOR'    : (_simple2, 0o36_00_00),
    'BXOR'   : (_simple2, 0o37_00_00),

    'TST'    : (_alu1, 0o41_00_00),
    'NEG'    : (_alu1, 0o42_00_00),
    'INV'    : (_alu1, 0o43_00_00),
    'CLR'    : (_alu1, 0o45_00_00),

    'SHR'    : (_alu2, 0o44_00_00),
    'SHL'    : (_alu2, 0o45_00_00),
    'ADD'    : (_alu2, 0o50_00_00),
    'SUB'    : (_alu2, 0o51_00_00),
    'MUL'    : (_alu2, 0o52_00_00),
    'AND'    : (_alu2, 0o53_00_00),
    'OR'     : (_alu2, 0o54_00_00),
    'XOR'    : (_alu2, 0o55_00_00),

    'LDR'    : (_ldr, 0o02_00_00),
    'LDC'    : (_ldr, 0o04_00_00),
    'STR'    : (_str, 0)
}

#----------------------------------------------------------------------------
print("Proc18 ASM 2020")

if len(sys.argv) < 2:
    print("Usage: python proc18asm [options] <file name>")
    print("-d     Debug")
    print("-rs N  ROM Size")
    print("-cs N  CONST Size")
    exit()

try:
    debug_flag = False
    file_name = ''
    error_msg = ''
    rom_size = 4
    const_size = 4

    i = 1
    while i < len(sys.argv):
        if sys.argv[i] == '-d':
            debug_flag = True
        elif sys.argv[i] == '-rs':
            i += 1
            rom_size = int(sys.argv[i])
        elif sys.argv[i] == '-cs':
            i += 1
            const_size = int(sys.argv[i])
        else:
            file_name = sys.argv[i]
        i += 1

    rom_size *= 1024
    const_size *= 1024
    code_buffer = [None] * rom_size
    const_buffer = [None] * rom_size

    print(file_name, rom_size, const_size)

    global tmp_file_name
    tmp_file_name = change_ext(file_name, 'tmp')

    line_no = 0
    with open(tmp_file_name, 'wt') as temp_file:
        read_file(file_name)

    fill_symbols()
    fill_code_buffer()
    fill_const_buffer()

    dump_lst_file(file_name)
    dump_hex_file(file_name)

    sys.stderr.write('Success\n\n')
    sys.exit(0)

except Error as e:
    msg = '{0} {1}\n'.format(e.message(), file_name)
    sys.stderr.write(msg)
    sys.exit(-1)
