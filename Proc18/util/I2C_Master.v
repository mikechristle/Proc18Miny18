//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: I2C_Master
//
// I2C serial bus master.
// For all operations wait for DONE to go HI before the next operation.
// For start and stop cycles just write the command to the command register.
// For writes write the data value to the data register,
// then write the Write Data command to the command register.
// For reads write the Read Data command to the command register,
// wait for DONE to go HI then read the data value. 
//
// Adrs     Write           Read
//  0       Data            Data
//  1       Command         Status
//
// Cmnd
//  1       Send Start
//  2       Send Stop
//  4       Read Data Ack
//  5       Read Data Nack
//  6       Write Data
//
// History: 
// 0.1.0   10/01/2019   File Created
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

module I2C_Master(
    input CLK,
    input RESET,
    input CS,
    input WE,
    input AD,
    input [7:0] DI,
    input SDA_I,

    output reg [7:0] DO,
    output reg DONE,
    output reg ERROR,

    output reg SCL,

    output reg SDA_E,
    output reg SDA_O,
    output SDA
    );

    initial begin
        DO = 0;
        SCL = 1;
        DONE = 0;
        ERROR = 0;
        SDA_E = 0;
        SDA_O = 1;
    end

    parameter CLK_FREQ = 50.0E6;
    localparam CLK_LOAD = (CLK_FREQ / 2.0E6) - 1;

    reg [5:0] clk_cntr = 0;
    reg state_clk = 0;
    reg [2:0] cmnd = 0;
    reg [3:0] state = 0;
    reg [2:0] bit_cnt = 0;

    assign SDA = (SDA_E) ? SDA_O : 1'bz;

    always @ (posedge CLK) begin

        // Assert state_clk once every 500 nSec
        if (clk_cntr == 0) begin
            clk_cntr <= CLK_LOAD;
            state_clk <= 1;
        end
        else begin
            clk_cntr <= clk_cntr - 6'd1;
            state_clk <= 0;
        end

        // Write to registers
        casex ({RESET, CS, WE, AD})

        // Reset
        4'b1_XXX:
            begin
            cmnd <= 0;
            state <= 0;
            SCL <= 1;
            ERROR <= 0;
            DONE <= 1;
            SDA_E <= 0;
            SDA_O <= 1;
            end

        // Write data reg
        4'b0_110:
            begin
            DO <= DI[7:0];
            ERROR <= 0;
            end

        // Write command reg
        4'b0_111:
            begin
            cmnd <= DI[2:0];
            state <= 0;
            ERROR <= 0;
            end

        // Read any reg
        4'b0_10X:
            begin
            ERROR <= 0;
            end

        endcase

        // Decode command
        casex ({state_clk, cmnd})

            // Send start/restart command
            4'b1_001:
                begin
                case (state)
                0:  begin
                    SDA_E <= 1;
                    SDA_O <= 1;
                    DONE <= 0;
                    end

                1:  begin
                    SCL <= 1;
                    end

                2:  begin
                    SDA_O <= 0;
                    end

                3:  begin
                    SCL <= 0;
                    end

                4:  begin
                    cmnd <= 0;
                    DONE <= 1;
                    end
                endcase
                state <= state + 4'd1;
                end

            // Send stop command
            4'b1_010:
                begin
                case (state)
                0:  begin
                    SCL <= 1;
                    DONE <= 0;
                    end

                2:  begin
                    SDA_O <= 1;
                    end

                4:  begin
                    SDA_E <= 0;
                    cmnd <= 0;
                    DONE <= 1;
                    end
                endcase
                state <= state + 4'd1;
                end

            // Write data
            4'b1_110:
                begin
                case (state)
                0:  begin
                    bit_cnt <= 8;
                    DONE <= 0;
                    end

                1:  begin
                    SDA_O <= DO[7];
                    DO <= {DO[6:0], 1'b0};
                    end

                3:  begin
                    SCL <= 1;
                    bit_cnt <= bit_cnt - 1;
                    end

                5:  begin
                    SCL <= 0;
                    end

                6:  begin
                    SDA_E <= 0;
                    end

                8:  begin
                    SCL <= 1;
                    SDA_O <= 0;
                    end

                10: begin
                    SCL <= 0;
                    if (SDA_I) ERROR <= 1; 
                    end

                12: begin
                    SDA_E <= 1;
                    cmnd <= 0;
                    DONE <= 1;
                    end

                endcase

                if (state == 5 && bit_cnt != 0) state <= 4'd1;
                else state <= state + 4'd1;
                end

            // Read Data
            4'b1_100,
            4'b1_101:
                begin
                case (state)

                0:  begin
                    bit_cnt <= 8;
                    SDA_E <= 0;
                    DONE <= 0;
                    end

                2:  begin
                    SCL <= 1;
                    bit_cnt <= bit_cnt - 1;
                    end

                4:  begin
                    SCL <= 0;
                    DO <= {DO[6:0], SDA_I};
                    end

                7:  begin
                    SDA_O <= cmnd[0];
                    SDA_E <= 1;
                    end

                8:  begin
                    SCL <= 1;
                    end

                10: begin
                    SCL <= 0;
                    end

                11: begin
                    SDA_O <= 0;
                    DONE <= 1;
                    cmnd <= 0;
                    end

                endcase
                if (state == 6 && bit_cnt != 0) state <= 4'd2;
                else state <= state + 4'd1;
                end

        endcase
    end

endmodule
