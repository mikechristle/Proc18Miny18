//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Status
//
// Store ALU status flags.
// Decode opcodes to generate branch signal.
//
// History: 
// 0.1.0   04/25/2018   File Created
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

module Status(
    input CLK,
    input [2:0] BRANCH_OP,
    input [1:0] LOAD,
    input BIT,
    input ZIn,
	input FIn,
    input [1:0] STATIN,

    output [1:0] STAT,
    output BRANCH
    );

    reg Z = 0;
    reg F = 0;
    
    assign STAT = {F, Z};

    assign BRANCH = (BRANCH_OP == 2) ?  Z      : // EQ BS
                    (BRANCH_OP == 3) ? !Z      : // NE BC
                    (BRANCH_OP == 4) ? !Z &  F : // LT
                    (BRANCH_OP == 5) ? !Z & !F : // GT
                    (BRANCH_OP == 6) ?  Z |  F : // LE
                    (BRANCH_OP == 7) ?  Z | !F : // GE
                                       1'd1;

    always @ (posedge CLK) begin
        casex (LOAD)
        2'b01: begin
            Z <= ZIn;
            F <= FIn;
            end
        2'b10: begin
            Z <= !BIT;
            F <= 0;
            end
        2'b11: begin
            Z <= STATIN[0];
            F <= STATIN[1];
            end
        endcase
    end

endmodule
