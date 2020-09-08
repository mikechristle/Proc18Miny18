//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Rotary Encoder
//
// Description: Decode a rotary encoder and maintain a counter.
//
// History: 
// 0.1.0   08/31/2020   File Created
// 1.0.0   09/01/2020   Initial release
// 2.0.0   09/08/2020   Add CS input & INT output to support interrupts
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

module RotaryEncoder(CLK, CS, WE, A, B, DI, DO, INT);

    parameter DIVIDER_BITS = 3;
    parameter COUNTER_BITS = 8;

    input CLK;
    input CS;
    input WE;
    input A;
    input B;
    input [COUNTER_BITS - 1:0] DI;

    output reg [COUNTER_BITS - 1:0] DO = 0;
    output reg INT;

    reg last_a = 0;
    reg [DIVIDER_BITS - 1:0] divider = 0;

    initial begin
        DO = 0;
        INT = 0;
    end
    
    always @(posedge CLK) begin

        divider <= divider + 1;

        if (CS & WE) begin
            DO <= DI;
        end
        else if (CS) begin
            INT <= 0;
        end
        else if (divider == 0) begin
            last_a <= A;
            if (A & !last_a) begin
                if (B) DO <= DO + 1;
                else   DO <= DO - 1;
                INT <= 1;
            end
        end
    end

endmodule
