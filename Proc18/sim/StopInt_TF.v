//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: StopInt_TF
//
// Test the Stop opcode and interrupt processing.
//
// History: 
// 1.0.0   09/08/2020   Initial release
//////////////////////////////////////////////////////////////////////////////

`timescale 1ns / 1ps

module StopInt_TF;

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
        .RAM_WR(RAM_WR),
        .RESET(RESET),
        .DATAOUT(DATAOUT),
        .ADRS(ADRS),
        .PC(PC)
    );

    initial begin
        CLK = 0;
        RUN = 0;
        INST = 0;
        VECTOR = 0;
        DATAIN = 0;

        //------------------------------------------------------------------------------------------
        #10;
        #10 CLK = 1; #10 CLK = 0;
        #10 CLK = 1; #10 CLK = 0;

        RUN = 1;

        INST = 18'o000505; // LEVEL
        #10 CLK = 1; #10 CLK = 0;
        if (uut.InstDecode_.level != 4'd15) $display("01 Level not initialized");
        #10 CLK = 1; #10 CLK = 0;
        if (uut.InstDecode_.level != 4'd5) $display("02 Level not set");

        INST = 18'o000000; // NOP
        #10 CLK = 1; #10 CLK = 0;
        #10 CLK = 1; #10 CLK = 0;

        INST = 18'o000100; // STOP
        #10 CLK = 1; #10 CLK = 0;
        if (uut.PC != 12'o0004) $display("03 PC error");
        if (uut.InstDecode_.state != 10'o0020) $display("03 State error");

        INST = 18'o000000; // NOP
        #10 CLK = 1; #10 CLK = 0;
        if (uut.PC != 12'o0004) $display("04 PC error");
        if (uut.InstDecode_.state != 10'o0020) $display("04 State error");

        #10 CLK = 1; #10 CLK = 0;
        if (uut.PC != 12'o0004) $display("05 PC error");
        if (uut.InstDecode_.state != 10'o0020) $display("05 State error");

        #10 CLK = 1; #10 CLK = 0;
        if (uut.PC != 12'o0004) $display("06 PC error");
        if (uut.InstDecode_.state != 10'o0020) $display("06 State error");

        VECTOR = 4'd7;
        #10 CLK = 1; #10 CLK = 0;
        if (uut.InstDecode_.level != 4'd7) $display("07 Level error");
        if (uut.InstDecode_.state != 10'o1000) $display("07 State error");

        VECTOR = 4'd0;
        #10 CLK = 1; #10 CLK = 0;
        if (uut.InstDecode_.level != 4'd7) $display("08 Level error");
        if (uut.InstDecode_.state != 10'o0001) $display("08 State error");
        if (uut.ADRS != 18'o777777) $display("08 Stack ADRS error");
        if (uut.DATAOUT != 18'o050004) $display("08 Stack DATA error");
        if (uut.RAM_WR != 1'd1) $display("08 RAM_WR error");
        if (uut.PC != 12'o0007) $display("08 PC error");

        #10 CLK = 1; #10 CLK = 0;
        if (uut.InstDecode_.state != 10'o0001) $display("09 State error");
        if (uut.ADRS != 18'o777777) $display("09 Stack ADRS error");


        INST = 18'o000400; // RTI
        #10 CLK = 1; #10 CLK = 0;
        if (uut.InstDecode_.state != 10'o0040) $display("10 State error");
        INST = 18'o000000; // NOP

        #10 CLK = 1; #10 CLK = 0;
        if (uut.InstDecode_.state != 10'o0100) $display("11 State error");

        DATAIN = 18'o651234;
        #10 CLK = 1; #10 CLK = 0;
        if (uut.InstDecode_.state != 10'o0001) $display("12 State error");
        if (uut.PC != 12'o1234) $display("12 PC error");
        if (uut.InstDecode_.level != 4'd5) $display("12 Level error");
        if (uut.Status_.Z != 1'b1) $display("12 Z flag error");
        if (uut.Status_.F != 1'b1) $display("12 F flag error");

        DATAIN = 18'o000000;
        #10 CLK = 1; #10 CLK = 0;
        if (uut.InstDecode_.state != 10'o0001) $display("13 State error");
        if (uut.PC != 12'o1235) $display("13 PC error");

    end
      
endmodule

