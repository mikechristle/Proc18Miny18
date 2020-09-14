//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Rotary Encoder
//
// Decode a rotary encoder and maintain a counter. Requires a 10 uSec clock
// from the IntTimer module. Samples the input signals every 5 mSec.
//
// History: 
// 0.1.0   08/31/2020   File Created
// 1.0.0   09/01/2020   Initial release
// 2.0.0   09/08/2020   Add CS input & INT output to support interrupts
//                      Added system clock
// 2.1.0   09/10/2020   Reduce sample time by reducing divider to 8 bits
// 2.1.1   09/10/2020   Correct initial states
// 2.1.2   09/12/2020   Specify bit size on add equations
// 2.2.0   09/14/2020   Rename COUNTER_BITS parameter
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

module RotaryEncoder(CLK, CLK_10U, CS, WE, A, B, DI, DO, INT);

    parameter BITS = 8;

    input CLK;
    input CLK_10U;
    input CS;
    input WE;
    input A;
    input B;
    input [BITS - 1:0] DI;

    output reg [BITS - 1:0] DO = 0;
    output reg INT = 0;

    reg last_a = 1;
    reg [7:0] divider = 0;

    always @(posedge CLK) begin
        if (CS & WE) begin
            DO <= DI;
        end
        else if (CS) begin
            INT <= 0;
        end
        else if (CLK_10U) begin
            divider <= divider + 8'd1;
            if (divider == 0) begin
                last_a <= A;
                if (A & !last_a) begin
                    if (B) DO <= DO + 8'd1;
                    else   DO <= DO - 8'd1;
                    INT <= 1;
                end
            end
        end
    end

endmodule
