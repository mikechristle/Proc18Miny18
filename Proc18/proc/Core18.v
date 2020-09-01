//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Core18
//
// Proc18 Core.
//
// History: 
// 0.1.0   05/10/2018   File Created
// 1.0.0   09/01/2020   Initial release
//////////////////////////////////////////////////////////////////////////////
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
//////////////////////////////////////////////////////////////////////////////

module Core18(CLK, RUN, VECTOR, BITSIN, INST, DATAIN, 
              PORT_RD, PORT_WR, CONST_RD, RAM_WR, RESET, 
              BITSOUT, PC, DATAOUT, ADRS); 

    input CLK;
    input RUN;
    input [17:0] INST;
    input [3:0] VECTOR;
    input [17:0] DATAIN;
    input [63:0] BITSIN;

    output [63:0] BITSOUT;
    output CONST_RD;
    output PORT_RD;
    output PORT_WR;
    output RAM_WR;
    output RESET;
    output [17:0] DATAOUT;
    output [17:0] ADRS;
    output [11:0] PC;

    wire TIMER_LD;
    wire TIMER_ZERO;
    wire BRANCH;
    wire DATA_SL;
    wire [3:0] ALU_OP;
    wire [1:0] ALUS_SL;
    wire [2:0] ADRS_SL;
    wire [1:0] BIT_LD;
    wire REG_WE;
    wire [5:0] SADRS;
    wire [5:0] DADRS;
    wire [11:0] NDATA;
    wire [2:0] BRANCH_OP;
    wire [17:0] SP;
    wire [11:0] PCO;
    wire [1:0] STAT;
    wire [1:0] STAT_LD;
    wire BIT;

    wire [17:0] DREG;
    wire [17:0] SREG;
    wire [17:0] SDATA;
    wire [17:0] RESULT;
    wire ZFLAG;
    wire FFLAG;

    AluSMux  AluSMux_ (
        .SREG(SREG), 
        .DATA(DATAIN), 
        .INST(INST), 
        .NDATA(NDATA[5:0]), 
        .S(ALUS_SL), 
        .Z(SDATA));

    ALU  ALU_ (
        .Op(ALU_OP), 
        .DData(DREG), 
        .SData(SDATA), 
        .Result(RESULT), 
        .FOut(FFLAG), 
        .ZOut(ZFLAG));

    BitMem  BitMem_ (
        .CLK(CLK), 
        .RESET(RESET), 
        .LOAD(BIT_LD), 
        .OP(ALU_OP[2:0]),
        .SADRS(SADRS), 
        .DADRS(DADRS), 
        .BRANCH(BRANCH),
        .BITS_IN(BITSIN), 
        .BITS_OUT(BITSOUT), 
        .BIT(BIT));

    AdrsMux  AdrsMux_ (
        .SEL(ADRS_SL), 
        .SREG(SREG), 
        .DREG(DREG), 
        .NDATA(NDATA), 
        .SP(SP), 
        .Z(ADRS));

    DataMux  DataMux_ (
        .SEL(DATA_SL), 
        .PCO(PCO), 
        .STAT(STAT), 
        .LEVEL(NDATA[3:0]), 
        .SREG(SREG), 
        .Z(DATAOUT));

    InstDecode  InstDecode_ (
        .CLK(CLK), 
        .RUN(RUN), 
        .VECTOR(VECTOR), 
        .INST(INST), 
        .DATAIN(DATAIN), 
        .TIMER_ZERO(TIMER_ZERO), 
        .BRANCH(BRANCH), 
        .CONST_RD(CONST_RD), 
        .PORT_WR(PORT_WR), 
        .PORT_RD(PORT_RD), 
        .RAM_WR(RAM_WR), 
        .RESET(RESET), 
        .DATA_SL(DATA_SL), 
        .TIMER_LD(TIMER_LD),
        .BIT_LD(BIT_LD), 
        .STAT_LD(STAT_LD), 
        .ALU_OP(ALU_OP), 
        .ALUS_SL(ALUS_SL), 
        .ADRS_SL(ADRS_SL), 
        .REG_WE(REG_WE), 
        .DADRS(DADRS), 
        .SADRS(SADRS), 
        .NDATA(NDATA),
        .BRANCH_OP(BRANCH_OP),
        .PC(PC), 
        .PCO(PCO), 
        .SP(SP));

    RegBank  RegBank_ (
        .CLK(CLK), 
        .WE(REG_WE), 
        .DA(DADRS), 
        .SA(SADRS), 
        .WD(RESULT), 
        .DD(DREG), 
        .SD(SREG));

    Status  Status_ (
        .CLK(CLK), 
        .BRANCH_OP(BRANCH_OP), 
        .LOAD(STAT_LD), 
        .STATIN(DATAIN[17:16]), 
        .BIT(BIT), 
        .ZIn(ZFLAG), 
        .FIn(FFLAG), 
        .BRANCH(BRANCH), 
        .STAT(STAT));

    Timer  Timer_ (
        .CLK(CLK), 
        .RESET(RESET), 
        .LOAD(TIMER_LD), 
        .DATA(INST[11:0]), 
        .ZERO(TIMER_ZERO));

endmodule
