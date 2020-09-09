//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: RotaryEncoder_TF
//
// Rotary encoder tests.
//
// History: 
// 0.1.0   08/31/2020   File Created
// 1.0.0   09/08/2020   Initial release
// 1.1.0   09/08/2020   Added CLK_10U input
//////////////////////////////////////////////////////////////////////////////

`timescale 1ns / 1ps

module RotaryEncoder_TF;

    // Inputs
    reg CLK;
    reg CLK_10U;
    reg CS;
    reg WE;
    reg A;
    reg B;
    reg [3:0] DI;

    // Outputs
    wire [3:0] DO;
    wire INT;

    // Instantiate the Unit Under Test (UUT)
    RotaryEncoder#(
        .COUNTER_BITS(4))
    uut (
        .CLK(CLK),
        .CLK_10U(CLK_10U),
        .CS(CS),
        .WE(WE),
        .A(A),
        .B(B),
        .DI(DI),
        .DO(DO),
        .INT(INT)
    );

    reg [1:0] cntr;

    initial begin

        CLK = 0;
        CLK_10U = 0;
        CS = 0;
        WE = 0;
        A = 0;
        B = 0;
        DI = 0;
        cntr = 0;

        #10;
        #10 CLK = 0; #10 CLK = 1;
        #10 CLK = 0; #10 CLK = 1;
        if (DO != 4'd0) $display("Initial count fail");
        if (INT != 1'b0) $display("INT fail 1");

        repeat (4) begin
            repeat (512) begin
                #10 CLK = 0; #10 CLK = 1;
                #10 CLK = 0; #10 CLK = 1;
                CLK_10U = 1;
                #10 CLK = 0; #10 CLK = 1;
                CLK_10U = 0;
                #10 CLK = 0; #10 CLK = 1;
            end
            A = cntr[1];
            B = cntr[0] ^ cntr[1];
            cntr = cntr + 1;
        end
        A = cntr[1];
        B = cntr[0] ^ cntr[1];
        cntr = cntr + 1;
        if (DO != 4'd1) $display("Count up fail");
        if (INT != 1'b1) $display("INT fail 2");

        DI = 4'd5;
        CS = 1;
        WE = 1;
        #10 CLK = 0; #10 CLK = 1;
        DI = 4'd0;
        CS = 0;
        WE = 0;
        #10 CLK = 0; #10 CLK = 1;
        if (DO != 4'd5) $display("Write counter fail");

        CS = 1;
        #10 CLK = 0; #10 CLK = 1;
        CS = 0;
        if (INT != 1'b0) $display("INT fail");

        repeat (20) begin
            repeat (512) begin
                #10 CLK = 0; #10 CLK = 1;
                #10 CLK = 0; #10 CLK = 1;
                CLK_10U = 1;
                #10 CLK = 0; #10 CLK = 1;
                CLK_10U = 0;
                #10 CLK = 0; #10 CLK = 1;
            end
            A = cntr[1];
            B = cntr[0] ^ cntr[1];
            cntr = cntr + 1;
        end
        if (DO != 4'd10) $display("Count up fail");

        repeat (20) begin
            repeat (512) begin
                #10 CLK = 0; #10 CLK = 1;
                #10 CLK = 0; #10 CLK = 1;
                CLK_10U = 1;
                #10 CLK = 0; #10 CLK = 1;
                CLK_10U = 0;
                #10 CLK = 0; #10 CLK = 1;
            end
            A = cntr[1];
            B = cntr[0] ^ cntr[1];
            cntr = cntr - 1;
        end
        if (DO != 4'd5) $display("Count down fail");

    end
      
endmodule

