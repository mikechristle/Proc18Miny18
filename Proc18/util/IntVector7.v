//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Interrupt Vector 7
//
// Decode interrupt signals and generate prioritized interrupt vectors.
// Each bit in the mask register corresponds to an interrupt input. 
// A one in the mask register will allow the interrupt to be generated.
//
// Write Address
// 0 = Clear mask register bits
// 1 = Set mask register bits
//
// History: 
// 0.1.0   07/20/2017   File Created
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

module IntVector7(
    input CLK,
    input RESET,
    input CS,
    input WE,
    input AD,
    input [6:0] DI,

    input I1,
    input I2,
    input I3,
    input I4,
    input I5,
    input I6,
    input I7,

    output reg [3:0] V
    );

    initial begin
        V = 0;
    end

    reg [6:0] mreg;

    wire [6:0] interrupts = mreg & {I7, I6, I5, I4, I3, I2, I1};

    always @ (posedge CLK) begin
        if (RESET) begin
            mreg <= 7'd0;
            V    <= 4'd0;
        end
        else begin
            if (CS & WE & !AD) mreg <= mreg & ~DI;
            if (CS & WE &  AD) mreg <= mreg |  DI;

            casex (interrupts)
            7'b1XXXXXX : V <= 4'd7;
            7'b01XXXXX : V <= 4'd6;
            7'b001XXXX : V <= 4'd5;
            7'b0001XXX : V <= 4'd4;
            7'b00001XX : V <= 4'd3;
            7'b000001X : V <= 4'd2;
            7'b0000001 : V <= 4'd1;
            7'b0000000 : V <= 4'd0;
            endcase
        end
    end

endmodule
