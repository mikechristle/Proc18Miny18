//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: BaudRateClk 
//
// Outputs a single clock cycle pulse at 16 times the desired baud rate.
// If the system clock is 50MHz and the desired baud rate is 19200.
// Pulse rate = 19200 * 16 = 307200.
// Counter value = 50M / 307200 = 163.
//
// History: 
// 0.1.0   07/27/2017   File Created
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

module BaudRateClk(
    input CLK,
    input CS,
    input WE,
    input [11:0] DI,

    output reg B_CLK
    );

    reg [11:0] cntr = 0;
    reg [11:0] load = 0;

    always @ (posedge CLK) begin
        if (CS & WE) load <= DI;
        if (cntr == 12'd0) begin
            cntr <= load;
            B_CLK <= 1'b1;
        end
        else begin
            cntr <= cntr - 12'd1;
            B_CLK <= 1'b0;
        end
    end

endmodule
