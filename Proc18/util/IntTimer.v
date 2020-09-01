//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: IntTimer 
//
// Generate a periodic interrupt with 10uSec increments.
// The CLK_FREQ parameter must be set to the frequency of the CLK input.
// If CLK is 50MHz and an interrupt is needed every 10mSec,
// 10mSec / 10uSec = 1000.
//
// History: 
// 0.1.0   11/14/2017   File Created
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

module IntTimer(
    input CLK,
    input RESET,
    input CS,
    input WE,
    input [17:0] DI,
    
    output reg CLK_10U,
    output reg INT,
    output [17:0] DO
    );
   
    parameter CLK_FREQ = 50E6;

    localparam I_ZERO = 1'b0;
    localparam I_ONE  = 1'b1;

    initial INT = I_ZERO;

    localparam C_ZERO = 18'd0;
    localparam C_ONE  = 18'd1;

    localparam P_ZERO  = 10'd0;
    localparam P_ONE   = 10'd1;

    // Prescaler = CLK_FREQ / 1E5 -> 10uSec Increments
    localparam P_COUNT = (CLK_FREQ / 1E5) - 1;

    function [9:0] trunc10(input [31:0] val32);
        trunc10 = val32[9:0];
    endfunction

    reg [9:0] prescale = P_ZERO;
    reg [17:0] counter = C_ZERO;
    reg [17:0] cnt_c   = C_ZERO;

    assign DO = counter;

    always @ (posedge CLK) begin
        if (RESET) begin
            INT      <= I_ZERO;
            counter  <= C_ZERO;
            cnt_c    <= C_ZERO;
            prescale <= P_ZERO;
        end
        else begin

            if (CS & !WE) INT <= I_ZERO;

            if (CS & WE) begin
                counter <= DI;
                cnt_c <= DI;
                prescale <= trunc10(P_COUNT);
            end

            if (counter != C_ZERO) begin
                if (prescale != P_ZERO) begin
                    prescale <= prescale - P_ONE;
                    CLK_10U <= 0;
                end
                else begin
                    CLK_10U <= 1;
                    prescale <= trunc10(P_COUNT);
                    if (cnt_c == C_ONE) begin
                        cnt_c <= counter;
                        INT <= I_ONE;
                    end
                    else
                        cnt_c <= cnt_c - C_ONE;
                end
            end
        end
    end

endmodule
