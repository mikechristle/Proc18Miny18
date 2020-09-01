//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: UART
//
// UART with one byte TX and RX hold registers.
// RX_READY indicates that a byte has been received.
// TX_EMPTY indicates that transmitter is idle and ready to send.
// BREAK indicates that a BREAK condition is active.
// BCLK must be a single clock cycle pulse at 16 times the baud rate.
// Use the BaudRateClk module to generate BCLK.
//
// History: 
// 0.1.0   07/30/2018   File Created
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

module UART
(
    input CLK,
    input RESET,
    input BCLK,
    input WE,
    input CS,
    input [7:0] DI,
    input RXD,
    
    output reg BREAK,
    output reg RX_READY,
    output reg TX_EMPTY,
    output [7:0] DO,
    output reg TXD
);

    localparam CLK_PER_BIT = 4'd15;
    localparam CLK_PER_HBIT = 4'd7;

    localparam ONE = 1'b1;
    localparam ZERO = 1'b0;

    initial begin
        TXD = 1;
        BREAK = 0;
        RX_READY = 0;
        TX_EMPTY = 0;
    end

    reg rx_d1, rx_d2;

    reg TX_BUSY = ZERO;
    reg tx_ready = ZERO;
    reg RX_BUSY = ZERO;

    reg [7:0] tx_sreg = 0;
    reg [7:0] tx_data = 0;

    reg [7:0] rx_sreg = 0;
    reg [7:0] rx_data = 0;

    reg [3:0] tx_bit_cntr = 0;
    reg [3:0] tx_clk_cntr = 0;
    reg [3:0] rx_bit_cntr = 0;
    reg [3:0] rx_clk_cntr = 0;

    assign DO = rx_data;

    //---- Transmit ---------------------------------------
    always @ (posedge CLK) begin
    
        if (RESET) begin
            TX_EMPTY <= ONE;
            TX_BUSY <= ZERO;
        end
        else begin

            // Write to TX buffer
            if (CS & WE & TX_EMPTY) begin
                tx_data <= DI;
                tx_ready <= ONE;
                TX_EMPTY <= ZERO;
            end

            // Start a transmit
            if (BCLK & !TX_BUSY & tx_ready) begin
                tx_bit_cntr <= 4'd10;
                tx_clk_cntr <= CLK_PER_BIT;
                tx_sreg <= tx_data;
                TXD <= ZERO;
                TX_BUSY <= ONE;
                TX_EMPTY <= ONE;
                tx_ready <= ZERO;
            end

            // Transmit
            if (BCLK & TX_BUSY) begin
                if (tx_clk_cntr == 4'd0) begin
                    tx_clk_cntr <= CLK_PER_BIT;
                    if (tx_bit_cntr == 4'h0) begin
                        TX_BUSY <= ZERO;
                    end
                    else begin
                        tx_bit_cntr <= tx_bit_cntr - 4'd1;
                        TXD <= tx_sreg[0];
                        tx_sreg <= {1'b1, tx_sreg[7:1]};
                    end
                end
                else
                    tx_clk_cntr <= tx_clk_cntr - 4'd1;
            end
        end
    end

    //---- Receive ----------------------------------------
    always @ (posedge CLK) begin

        rx_d1 <= RXD;
        rx_d2 <= rx_d1;

        if (RESET) begin
            RX_READY <= ZERO;
            RX_BUSY <= ZERO;
            if (rx_d2) BREAK <= ZERO;
        end
        else begin

            // Clear RX ready flag on data read
            if (CS & !WE) begin
                RX_READY <= ZERO;
            end

            // Detect a RX start bit
            if (BCLK & !RX_BUSY & !BREAK & !rx_d2) begin
                RX_BUSY <= ONE;
                rx_bit_cntr <= 4'h9;
                rx_clk_cntr <= CLK_PER_HBIT;
            end

            // Receive
            if (BCLK & RX_BUSY) begin

                if (rx_clk_cntr == 4'd0) begin

                    // If last bit
                    if (rx_bit_cntr == 4'h0) begin
                        RX_BUSY <= ZERO;
                        if (rx_d2) begin
                            rx_data <= rx_sreg;
                            RX_READY <= ONE;
                        end
                        else
                            BREAK <= ONE;
                    end

                    // Else receive another bit
                    else begin
                        rx_sreg <= {rx_d2, rx_sreg[7:1]};
                        rx_bit_cntr <= rx_bit_cntr - 4'h1;
                        rx_clk_cntr <= CLK_PER_BIT;
                    end
                end
                else
                    rx_clk_cntr <= rx_clk_cntr - 4'd1;
            end

            // Clear break state
            if (BREAK & rx_d2) begin
                BREAK <= ZERO;
            end
        end
    end

endmodule
