//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: BitMem
//
// Memory and ALU for discrete bits.
//
// History: 
// 0.1.0   04/24/2018   File Created
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

module BitMem(
    input CLK,
    input RESET,
    input [1:0] LOAD,
    input [2:0] OP,
    input [5:0] SADRS,
    input [5:0] DADRS,
    input BRANCH,
    input [63:0] BITS_IN,

    output BIT,
    output [63:0] BITS_OUT
    );

    reg [63:0] bit_mem = 0;

    assign BIT = (OP == 0) ?  BITS_IN[DADRS] ^ BITS_IN[SADRS] :
                 (OP == 1) ?  BITS_IN[SADRS] :
                 (OP == 2) ? !BITS_IN[SADRS] :
                 (OP == 3) ?  1'd0 :
                 (OP == 4) ?  1'd1 :
                 (OP == 5) ?  BITS_IN[DADRS] & BITS_IN[SADRS] :
                 (OP == 6) ?  BITS_IN[DADRS] | BITS_IN[SADRS] :
               /*(OP == 7)*/  BITS_IN[DADRS] ^ BITS_IN[SADRS];

    assign BITS_OUT = bit_mem;

    always @ (posedge CLK) begin
        casex ( {RESET, LOAD} )
        3'b1XX: bit_mem <= 0;
        3'b001: bit_mem[DADRS] <= BIT;
        3'b010: bit_mem[DADRS] <= BRANCH;
        endcase
    end

endmodule
