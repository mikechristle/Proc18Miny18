//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: NEC_IR
//
// NEC protocal IR receiver.
// The READY signal is asserted on any device address. If you only want
// a specific address then update the validity check at state 3. Also, 
// I've found some remotes that do not strictly follow the protocal. To 
// fix this just comment out the validity check at state 3.
//
// History: 
// 1.0.0   09/12/2020   Initial release
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

module NEC_IR(CLK, CLK_10U, RESET, CS, IR, READY, DO);

    input CLK;
    input CLK_10U;
    input RESET;
    input CS;
    input IR;

    output reg READY = 0;
    output reg [15:0] DO = 0;

    reg [2:0] state = 0;
    reg ir0 = 1;
    reg ir1 = 1;
    reg active_edge = 0;
    reg [13:0] time_out = 0;
    reg [5:0] bit_cnt = 0;
    reg [31:0] shift = 0;

    always @ (posedge CLK) begin
        if (CS) begin
            READY <= 0;
        end

        if (RESET) begin
            READY <= 0;
            state <= 0;
        end
        else if (CLK_10U) begin

            // Syncronize input signal
            ir0 <= IR;
            ir1 <= ir0;
            if (ir1 & ~ir0) active_edge <= 1;
            else            active_edge <= 0;

            // Decode state
            casex (state)

            // State 0, wait for start pulse
            3'd0: begin
                if (active_edge) begin
                    state <= 1;
                    time_out <= 850;
                end
            end
              
            // State 1, wait for end of start pulse
            3'd1: begin
                time_out <= time_out - 14'd1;
                if (ir1) state <= 6;
                else if (time_out == 0) begin
                    bit_cnt <= 32;
                    time_out <= 600;
                    state <= 2;
                end
            end

            // State 2, wait for active edge
            3'd2: begin
                time_out <= time_out - 14'd1;
                if (time_out == 0) state <= 6;
                else if (active_edge) begin
                    shift <= {time_out[7], shift[31:1]};
                    time_out <= 425;
                    bit_cnt <= bit_cnt - 3'd1;
                    if (bit_cnt == 0) state <= 3;
                end
            end

            // State 3, save valid data
            3'd3: begin
                if (shift[31:24] == ~shift[23:16] &&
                    shift[15:8] == ~shift[7:0]) begin
                        READY <= 1;
                        DO <= {shift[7:0], shift[23:16]};
                end
                state <= 7;
                time_out <= 100;
            end

            // State 6, ignore input for 100mSec
            3'd6: begin
                state <= 7;
                time_out <= 10000;
            end

            // State 7, ignore input for 100mSec
            3'd7: begin
                if (time_out == 0) state <= 0;
                else time_out <= time_out - 14'd1;
            end

            endcase
        end
    end
endmodule
