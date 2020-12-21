//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Jxx_Int_TF
//
// Test interrupt processing during Jxx opcodes.
//
// History: 
// 1.0.0   12/21/2020   Initial release
//////////////////////////////////////////////////////////////////////////////

`timescale 1ns / 1ps

module Jxx_Int_TF;

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

    reg [17:0] mem [7:0];
    integer idx = 0;

    function [17:0] RAM;
    input we;
    input [11:0] adrs;
    input [17:0] data;
    begin
        if (we) mem[adrs[2:0]] = data;
        RAM = mem[adrs[2:0]];
    end
    endfunction


    function [17:0] ROM;
    input [11:0] adrs;
    begin
        case (adrs)
        12'o0000: ROM = 18'o000503; // LEVEL 3
        12'o0001: ROM = 18'o200500; // JMP 0o500

        12'o0005: ROM = 18'o710103; // ADD 3, #1
        12'o0006: ROM = 18'o000400; // RTI

        12'o0500: ROM = 18'o610005; // LDI 5, #0
        12'o0501: ROM = 18'o230510; // JNE 510
        12'o0502: ROM = 18'o710105; // ADD 5, #1
        12'o0503: ROM = 18'o230510; // JNE 510
        12'o0504: ROM = 18'o000000; // NOP
        12'o0505: ROM = 18'o000000; // NOP
        12'o0506: ROM = 18'o000000; // NOP
        12'o0507: ROM = 18'o000000; // NOP
        12'o0510: ROM = 18'o000100; // HALT
        12'o0511: ROM = 18'o200510; // JMP 510
        endcase
    end
    endfunction

    initial begin
        CLK = 0;
        RUN = 0;
        INST = 0;
        VECTOR = 0;
        DATAIN = 0;
        uut.RegBank_.reg_array[5] = 18'd5;
        uut.RegBank_.reg_array[6] = 18'd0;
        uut.RegBank_.reg_array[3] = 18'd0;

        mem[0] = 1;
        mem[1] = 2;
        mem[2] = 3;
        mem[3] = 18'o123456;
        mem[4] = 5;
        mem[5] = 6;
        mem[6] = 7;
        mem[7] = 4;

        //------------------------------------------------------------------------------------------
        #10;
        #10 CLK = 1; #10 CLK = 0;
        #10 CLK = 1; #10 CLK = 0;

        RUN = 1;

        repeat (22) begin
            INST = ROM(uut.PC);
            DATAIN = RAM(uut.RAM_WR, uut.ADRS, uut.DATAOUT);

            if (idx ==  9) VECTOR = 4'd5;
            if (idx == 12) VECTOR = 4'd0;

            #10 CLK = 1; #10 CLK = 0;

            idx = idx + 1;
        end
    end
      
endmodule

