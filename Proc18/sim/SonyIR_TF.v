//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: SonyIR_TF
//
// Sony SIRC receiver tester.
//
// History: 
// 1.0.0   09/09/2020   Initial release
//////////////////////////////////////////////////////////////////////////////

`timescale 1ns / 1ps

module SonyIR_TF;

    // Inputs
    reg CLK;
    reg CLK_10U;
    reg RESET;
    reg CS;
    reg IR;

    // Outputs
    wire READY;
    wire [11:0] DO;

    // Instantiate the Unit Under Test (UUT)
    SonyIR uut (
        .CLK(CLK),
        .CLK_10U(CLK_10U),
        .RESET(RESET),
        .CS(CS),
        .IR(IR),
        .READY(READY),
        .DO(DO)
    );

    task delay;
        input [8:0] clks;
        begin
            repeat (clks) begin
                #10 CLK = 0; #10 CLK = 1;
                #10 CLK = 0; #10 CLK = 1;
                #10 CLK = 0; #10 CLK = 1;
                CLK_10U = 1;
                #10 CLK = 0; #10 CLK = 1;
                CLK_10U = 0;
            end
        end
    endtask

    initial begin
        CLK = 0;
        CLK_10U = 0;
        RESET = 1;
        CS = 0;
        IR = 1;

        #10 CLK = 0; #10 CLK = 1;
        #10 CLK = 0; #10 CLK = 1;

        RESET = 0;
        #10 CLK = 0; #10 CLK = 1;
        delay(4);

        // Start pulse
        IR = 0;
        delay(260);
        IR = 1;
        delay(60);

        // Send data word
        repeat (6) begin
            IR = 0;
            delay(60);
            IR = 1;
            delay(60);
            IR = 0;
            delay(120);
            IR = 1;
            delay(60);
        end

        delay(20);
        if (READY != 1'b1) $display("READY HI fail");
        if (DO != 12'hAAA) $display("DO fail");

        CS = 1;
        #10 CLK = 0; #10 CLK = 1;
        CS = 0;
        #10 CLK = 0; #10 CLK = 1;

        if (READY != 1'b0) $display("READY LO fail");

    end
      
endmodule

