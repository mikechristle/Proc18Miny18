//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: SonyIR
//
// Sony SIRC receiver. Supports 12 and 15 bit count versions.
// The 20 bit version does not fit on the Proc18 18 bit bus.
//
// History: 
// 0.1.0   07/14/2019   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/01/2020   Add parameter to set bit count
// 1.1.1   09/12/2020   Delete shift register
//                      Correct bit size on state numbers
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

module SonyIR(CLK, CLK_10U, RESET, CS, IR, READY, DO);

    parameter BIT_CNT = 12;

    input CLK;
    input CLK_10U;
    input RESET;
    input CS;
    input IR;

    output reg READY = 0;
    output reg [BIT_CNT - 1:0] DO = 0;

    reg [2:0] state = 0;
    reg ir0, ir1, active_edge;
    reg [15:0] time_out = 0;
    reg [3:0] bit_cnt = 0;

    always @ (posedge CLK) begin
        if (RESET) begin
            DO <= 0;
            READY <= 0;
            state <= 0;
            active_edge <= 0;
            bit_cnt <= 0;
            ir0 <= 0;
            ir1 <= 0;
        end

        if (~RESET & CS) begin
            READY <= 0;
        end

        if (~RESET & CLK_10U) begin

            // Syncronize input signal
            ir0 <= IR;
            ir1 <= ir0;
            if (ir1 & ~ir0) active_edge <= 1;
            else            active_edge <= 0;

            // Decode state
            casex ({active_edge, state})

            // State 0, wait for start pulse
            4'b1_000: begin
                state <= 1;
                time_out <= 250;
                end
              
            // State 1, verify start pulse
            4'b0_001: begin
                time_out <= time_out - 1;
                if (time_out == 0) begin
                    bit_cnt <= BIT_CNT - 1;
                    time_out <= 120;
                    state <= 2;
                    end
                end

            4'b1_001: begin
                state <= 6;
                end

            // State 2, wait for active edge
            4'b1_010: begin
                state <= 3;
                time_out <= 90;
                end

            4'b0_010: begin
                time_out <= time_out - 1;
                if (time_out == 0) state <= 30;
                end


            // State 3, start data bit loop
            4'b0_011: begin
                time_out <= time_out - 1;
                if (time_out == 0) begin
                    DO <= {~ir1, DO[BIT_CNT - 1:1]};
                    bit_cnt <= bit_cnt - 1;
                    time_out <= 120;
                    if (bit_cnt == 0) state <= 4;
                    else              state <= 2;
                    end
                end

            4'b1_011: begin
                state <= 6;
                end

            // State 4, save valid data
            4'bX_100: begin
                state <= 6;
                READY <= 1;
                end

            // State 6, ignore input for 10mSec
            4'bX_110: begin
                state <= 7;
                time_out <= 3000;
                end

            // State 7, ignore input for 10mSec
            4'bX_111: begin
                if (time_out == 0) state <= 0;
                else time_out <= time_out - 1;
                end

            // Default, do nothing
            default: begin end
            endcase
        end
    end
endmodule
