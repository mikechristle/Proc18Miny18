//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: NEC_IR_TF
//
// NEC IR receiver tester.
//
// History: 
// 1.0.0   09/12/2020   Initial release
//////////////////////////////////////////////////////////////////////////////

`timescale 1ns / 1ps

module NEC_IR_TF;

    // Inputs
    reg CLK;
    reg CLK_10U;
    reg RESET;
    reg CS;
    reg IR;

    // Outputs
    wire READY;
    wire [15:0] DO;

    // Instantiate the Unit Under Test (UUT)
    NEC_IR uut (
        .CLK(CLK),
        .CLK_10U(CLK_10U),
        .RESET(RESET),
        .CS(CS),
        .IR(IR),
        .READY(READY),
        .DO(DO)
    );

    task delay;
        input [9:0] clks;
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

    task output_word;
        input [31:0] data;
        begin
            repeat (33) begin
                IR = 0;
                delay(56);
                IR = 1;
                if (data[0]) delay(169);
                else         delay(56);
                data = data >> 1;
            end
        end
    endtask

    integer val;

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
        delay(900);
        IR = 1;
        delay(450);
        output_word(32'hAA5533CC);

        delay(20);
        if (READY != 1'b1) $display("READY HI fail");
        if (DO != 16'hCC55) $display("DO fail");

        CS = 1;
        #10 CLK = 0; #10 CLK = 1;
        CS = 0;
        #10 CLK = 0; #10 CLK = 1;

        if (READY != 1'b0) $display("READY LO fail");
    end
      
endmodule

