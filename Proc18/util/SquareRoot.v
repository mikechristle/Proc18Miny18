//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: SquareRoot
//
// Calculate the square root of an unsigned integer.
//
// Write an integer.
// Wait for DONE to go hi, or 9 clock cycles.
// Read the result.
//
// History: 
// 0.1.0   08/11/2020   File Created
// 1.0.0   09/01/2020   Initial release
//////////////////////////////////////////////////////////////////////////////
// Compute square-root of y[31..0] into x[15..0]
//
// root  mov a, #0         'reset accumulator
//       mov x, #0         'reset root
//       mov c, #16        'ready for 16 root bits
//
// root1 shl y, #1  wc     'rotate top two bits of y to accumulator
//       rcl a, #1
//       shl y, #1  wc
//       rcl a, #1
//       shl x, #2         'determine next bit of root
//       or x, #1
//       cmpsub a, x  wc
//       shr x, #2
//       rcl x, #1
//       djnz c, #root1    'loop until done
//       ret               'square root in x[15..0]
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

module SquareRoot(
    input CLK,
    input CS,
    input WE,
    input [17:0] DI,

    output DONE,
    output [17:0] DO
    );

    reg [17:0] y = 0;
    reg [17:0] x = 0;
    reg [17:0] a = 0;
    reg [3:0] c = 0;

    assign DONE = c == 0;
    assign DO = x;

    always @ (posedge CLK) begin
        if (CS && WE) begin
            y = DI;
            x = 0;
            a = 0;
            c = 9;
        end

        if (!DONE) begin
            a = {a[15:0], y[17:16]};
            y = {y[15:0], 2'b00};
            x = {x[15:0], 2'b01};
            if (a >= x) begin
                a = a - x;
                x = {x[17:2], 1'b1};
            end
            else begin
                x = {x[17:2], 1'b0};
            end
            c = c - 1;
        end
    end

endmodule
