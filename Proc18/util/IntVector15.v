//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Interrupt Vector 15
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

module IntVector15(
    input CLK,
    input RESET,
    input CS,
    input WE,
    input AD,
    input [14:0] DI,

    input I1,
    input I2,
    input I3,
    input I4,
    input I5,
    input I6,
    input I7,
    input I8,
    input I9,
    input I10,
    input I11,
    input I12,
    input I13,
    input I14,
    input I15,

    output reg [3:0] V
    );

    initial begin
        V = 0;
    end

    reg [14:0] mreg;

    wire [14:0] interrupts = mreg & {I15, I14, I13, I12, I11, I10, I9,
                                     I8, I7, I6, I5, I4, I3, I2, I1};

    always @ (posedge CLK) begin
        if (RESET) begin
            mreg <= 15'd0;
            V    <= 4'd0;
        end
        else begin
            if (CS & WE & !AD) mreg <= mreg & ~DI;
            if (CS & WE &  AD) mreg <= mreg |  DI;

            casex (interrupts)
            15'b1XXXXXXXXXXXXXX : V <= 4'd15;
            15'b01XXXXXXXXXXXXX : V <= 4'd14;
            15'b001XXXXXXXXXXXX : V <= 4'd13;
            15'b0001XXXXXXXXXXX : V <= 4'd12;
            15'b00001XXXXXXXXXX : V <= 4'd11;
            15'b000001XXXXXXXXX : V <= 4'd10;
            15'b0000001XXXXXXXX : V <= 4'd9;
            15'b00000001XXXXXXX : V <= 4'd8;
            15'b000000001XXXXXX : V <= 4'd7;
            15'b0000000001XXXXX : V <= 4'd6;
            15'b00000000001XXXX : V <= 4'd5;
            15'b000000000001XXX : V <= 4'd4;
            15'b0000000000001XX : V <= 4'd3;
            15'b00000000000001X : V <= 4'd2;
            15'b000000000000001 : V <= 4'd1;
            15'b000000000000000 : V <= 4'd0;
            endcase
        end
    end

endmodule
