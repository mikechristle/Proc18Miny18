//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: SegDisplays
//
// Drive 8 hexadecimal digits.
// LEDs are assumed to be active LO.
//
// Input bits
// Bit 3-0  Hex Value
// Bit 4    Blank Digit
// Bit 5    Decimal Point
// Bit 7-6  Not Used
//
// Output bits
//     0 
//    ---
//   |   |
//  5|   |1
//   | 6 |
//    ---
//   |   |
//  4|   |2
//   | 3 |
//    ---   7
// 7 is the decimal point.
//
// History: 
// 0.1.0   01/04/2018   File Created
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

module SegDisplays(
    input CLK,
    input RESET,
    input CS,
    input WE,
    input [2:0] AD,
    input [7:0] DI,

    output reg [7:0] SD0,
    output reg [7:0] SD1,
    output reg [7:0] SD2,
    output reg [7:0] SD3,
    output reg [7:0] SD4,
    output reg [7:0] SD5,
    output reg [7:0] SD6,
    output reg [7:0] SD7
    );

    wire [6:0] segments = (DI[4:0] == 5'h00) ? 7'b1000000 :
                          (DI[4:0] == 5'h01) ? 7'b1111001 :
                          (DI[4:0] == 5'h02) ? 7'b0100100 :
                          (DI[4:0] == 5'h03) ? 7'b0110000 :
                          (DI[4:0] == 5'h04) ? 7'b0011001 :
                          (DI[4:0] == 5'h05) ? 7'b0010010 :
                          (DI[4:0] == 5'h06) ? 7'b0000010 :
                          (DI[4:0] == 5'h07) ? 7'b1111000 :
                          (DI[4:0] == 5'h08) ? 7'b0000000 :
                          (DI[4:0] == 5'h09) ? 7'b0010000 :
                          (DI[4:0] == 5'h0A) ? 7'b0001000 :
                          (DI[4:0] == 5'h0B) ? 7'b0000011 :
                          (DI[4:0] == 5'h0C) ? 7'b1000110 :
                          (DI[4:0] == 5'h0D) ? 7'b0100001 :
                          (DI[4:0] == 5'h0E) ? 7'b0000110 :
                          (DI[4:0] == 5'h0E) ? 7'b0000110 :
                          (DI[4:0] == 5'h0F) ? 7'b0001110 :
                                               7'b1111111 ;

    always @ (posedge CLK) begin
        casex ({RESET, CS, WE, AD})
        6'b0_11_000 : SD0 <= {~DI[5], segments};
        6'b0_11_001 : SD1 <= {~DI[5], segments};
        6'b0_11_010 : SD2 <= {~DI[5], segments};
        6'b0_11_011 : SD3 <= {~DI[5], segments};
        6'b0_11_100 : SD4 <= {~DI[5], segments};
        6'b0_11_101 : SD5 <= {~DI[5], segments};
        6'b0_11_110 : SD6 <= {~DI[5], segments};
        6'b0_11_111 : SD7 <= {~DI[5], segments};
        6'b1_XX_XXX :
            begin
                SD0 <= 8'hFF;
                SD1 <= 8'hFF;
                SD2 <= 8'hFF;
                SD3 <= 8'hFF;
                SD4 <= 8'hFF;
                SD5 <= 8'hFF;
                SD6 <= 8'hFF;
                SD7 <= 8'hFF;
            end
        endcase
    end

endmodule
