//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Proc18
//
// Proc18 System. Includes Core18, data RAM, code ROM and constants ROM.
// Parameters set the memory size of code ROM, constants ROM and RAM,
// by setting the number of address bits. Default is 4K words each.
// The constants ROM can have size 0, where the ROM is not instantiated
// and all reads return zero;
//
// History: 
// 0.1.0   05/10/2018   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/13/2020   Allow constants ROM to have size zero
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

module Proc18(CLK, RUN, DI, VECTOR, BITSIN, 
              RESET, CS, WE, AD, DO, BITSOUT);

    input CLK;
    input RUN;
    input [17:0] DI;
    input [3:0] VECTOR;
    input [63:0] BITSIN;

    output RESET;
    output CS;
    output WE;
    output [5:0] AD;
    output [17:0] DO;
    output [63:0] BITSOUT;

    parameter ROM_ADRS_BITS = 12;
    parameter RAM_ADRS_BITS = 12;
    parameter CON_ADRS_BITS = 12;

    wire CONST_RD;
    wire [17:0] ADRS;
    wire [11:0] PC;
    wire RAM_WR;
    wire [17:0] CONST_DATA;
    wire [17:0] RAM_DATA;
    wire [17:0] DI;
    wire [17:0] INST;
    wire PORT_RD;
    wire PORT_WR;
    wire [17:0] DO;
    wire [17:0] DATAIN;

    assign AD = ADRS[5:0];
    assign CS = PORT_WR | PORT_RD;
    assign WE = PORT_WR;

    Core18  Core18_ (
        .CLK(CLK),
        .RUN(RUN),
        .RESET(RESET),
        .BITSIN(BITSIN),
        .INST(INST),
        .DATAIN(DATAIN),
        .VECTOR(VECTOR),
        .BITSOUT(BITSOUT),
        .CONST_RD(CONST_RD),
        .ADRS(ADRS),
        .DATAOUT(DO),
        .PC(PC),
        .PORT_RD(PORT_RD),
        .PORT_WR(PORT_WR),
        .RAM_WR(RAM_WR));

    generate
        if (CON_ADRS_BITS == 0) begin
            DataInMux  DataInMux_ (
                .CLK(CLK),
                .CONST(18'd0),
                .CONST_RD(CONST_RD),
                .PORT(DI),
                .PORT_RD(PORT_RD),
                .RAM(RAM_DATA),
                .Z(DATAIN));
        end
        else begin
            CONST_Nx18 #(
                .ADRS_BITS(CON_ADRS_BITS))
            CONST_ROM_ (
                .CLK(CLK),
                .AD(ADRS[CON_ADRS_BITS - 1: 0]),
                .DO(CONST_DATA));

            DataInMux  DataInMux_ (
                .CLK(CLK),
                .CONST(CONST_DATA),
                .CONST_RD(CONST_RD),
                .PORT(DI),
                .PORT_RD(PORT_RD),
                .RAM(RAM_DATA),
                .Z(DATAIN));
        end

    endgenerate

    RAM_Nx18 #(
        .ADRS_BITS(RAM_ADRS_BITS))
    RAM_ (
        .CLK(CLK), 
        .WR(RAM_WR), 
        .AD(ADRS[RAM_ADRS_BITS - 1:0]), 
        .DI(DO), 
        .DO(RAM_DATA));

    ROM_Nx18 #(
        .ADRS_BITS(ROM_ADRS_BITS))
    ROM_ (
        .CLK(CLK),
        .AD(PC[ROM_ADRS_BITS - 1:0]), 
        .DO(INST));

endmodule
