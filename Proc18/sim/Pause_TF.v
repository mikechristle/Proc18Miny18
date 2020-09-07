//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Pause_TF
//
// Test the TIMER and PAUSE opcodes.
//
// History: 
// 1.0.0   09/07/2020   Initial release
//////////////////////////////////////////////////////////////////////////////

`timescale 1ns / 1ps

module Pause_TF;

    // Inputs
    reg CLK;
    reg RUN;
    reg [17:0] INST;
    reg [3:0] VECTOR;
    reg [17:0] DATAIN;

    // Outputs
    wire [63:0] BITS;
    wire CONST_RD;
    wire PORT_RD;
    wire PORT_WR;
    wire RAM_WR;
    wire RESET;
    wire [17:0] DATAOUT;
    wire [17:0] ADRS;
    wire [11:0] PC;

    // Instantiate the Unit Under Test (UUT)
    Core18 uut (
        .CLK(CLK),
        .RUN(RUN),
        .INST(INST),
        .VECTOR(VECTOR),
        .DATAIN(DATAIN),
        .BITSIN(BITS),

        .BITSOUT(BITS),
        .CONST_RD(CONST_RD),
        .PORT_RD(PORT_RD),
        .PORT_WR(PORT_WR),
        .RESET(RESET),
        .RAM_WR(RAM_WR),
        .DATAOUT(DATAOUT),
        .ADRS(ADRS),
        .PC(PC)
    );

    initial begin
        // Initialize Inputs
        CLK = 0;
        RUN = 0;
        INST = 0;
        VECTOR = 0;
        DATAIN = 0;

        #15;
        
        //------------------------------------------------------------------------------------------
        #5 CLK = 0; #10 CLK = 1; #5;
        #5 CLK = 0; #10 CLK = 1; #5;

        RUN = 1;

        INST = 18'o050010; // TIMER 8
        #5 CLK = 0; #10 CLK = 1; #5;
        #5 CLK = 0; #10 CLK = 1; #5;
        if (uut.Timer_.counter != 12'o0010) $display("Timer counter error");
        if (uut.Timer_.ZERO != 1'b0) $display("Timer ZERO error");

        INST = 18'o000200; // PAUSE
        #5 CLK = 0; #10 CLK = 1; #5;
        #5 CLK = 0; #10 CLK = 1; #5;
        #5 CLK = 0; #10 CLK = 1; #5;
        #5 CLK = 0; #10 CLK = 1; #5;
        #5 CLK = 0; #10 CLK = 1; #5;
        if (uut.PC != 12'o0001) $display("PC error");
        if (uut.Timer_.counter != 12'o0003) $display("Timer counter error");
        if (uut.Timer_.ZERO != 1'b0) $display("Timer ZERO error");
        #5 CLK = 0; #10 CLK = 1; #5;
        if (uut.PC != 12'o0001) $display("PC error");
        if (uut.Timer_.counter != 12'o0002) $display("Timer counter error");
        if (uut.Timer_.ZERO != 1'b0) $display("Timer ZERO error");
        #5 CLK = 0; #10 CLK = 1; #5;
        if (uut.PC != 12'o0001) $display("PC error");
        if (uut.Timer_.counter != 12'o0001) $display("Timer counter error");
        if (uut.Timer_.ZERO != 1'b0) $display("Timer ZERO error");
        #5 CLK = 0; #10 CLK = 1; #5;
        if (uut.PC != 12'o0001) $display("PC error");
        if (uut.Timer_.counter != 12'o0000) $display("Timer counter error");
        if (uut.Timer_.ZERO != 1'b1) $display("Timer ZERO error");
        #5 CLK = 0; #10 CLK = 1; #5;
        if (uut.PC != 12'o0002) $display("PC error");
        if (uut.Timer_.counter != 12'o0000) $display("Timer counter error");
        if (uut.Timer_.ZERO != 1'b1) $display("Timer ZERO error");
        #5 CLK = 0; #10 CLK = 1; #5;
        if (uut.PC != 12'o0003) $display("PC error");
        #5 CLK = 0; #10 CLK = 1; #5;
        #5 CLK = 0; #10 CLK = 1; #5;

        INST = 18'o000000; // NOP
        #5 CLK = 0; #10 CLK = 1; #5;

    end
      
endmodule

