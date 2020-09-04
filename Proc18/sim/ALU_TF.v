`timescale 1ns / 1ps

module ALU_TF;

    // Inputs
    reg [3:0] Op;
    reg [17:0] SData;
    reg [17:0] DData;

    // Outputs
    wire ZOut;
    wire FOut;
    wire [17:0] Result;

    // Instantiate the Unit Under Test (UUT)
    ALU uut (
        .Op(Op),
        .SData(SData),
        .DData(DData),
        .ZOut(ZOut),
        .FOut(FOut),
        .Result(Result)
    );

    initial begin

        // CMP
        Op = 4'o00;
        DData = 23;
        SData = 5;
        #10;
        if (Result != 18'd18) $display("Fail CMP Result");
        if (ZOut != 1'b0) $display("Fail CMP ZOut");
        if (FOut != 1'b0) $display("Fail CMP FOut");

        DData = 5;
        SData = 23;
        #10;
        if (Result != -(18'd18)) $display("Fail CMP Result");
        if (ZOut != 1'b0) $display("Fail CMP ZOut");
        if (FOut != 1'b1) $display("Fail CMP FOut");

        DData = 23;
        SData = 23;
        #10;
        if (Result != 18'd0) $display("Fail CMP Result");
        if (ZOut != 1'b1) $display("Fail CMP ZOut");
        if (FOut != 1'b0) $display("Fail CMP FOut");

        // PAS
        Op = 4'o01;
        SData = 18'o252525;
        #10;
        if (Result != 18'o252525) $display("Fail PAS Result");
        if (ZOut != 1'b0) $display("Fail PAS ZOut");
        if (FOut != 1'b0) $display("Fail PAS FOut");

        SData = 18'o525252;
        #10;
        if (Result != 18'o525252) $display("Fail PAS Result");
        if (ZOut != 1'b0) $display("Fail PAS ZOut");
        if (FOut != 1'b1) $display("Fail PAS FOut");

        SData = 18'o000000;
        #10;
        if (Result != 18'd0) $display("Fail PAS Result");
        if (ZOut != 1'b1) $display("Fail PAS ZOut");
        if (FOut != 1'b0) $display("Fail PAS FOut");

        // NEG
        Op = 4'o02;
        SData = 23;
        #10;
        if (Result != -(18'd23)) $display("Fail NEG Result");
        if (ZOut != 1'b0) $display("Fail NEG ZOut");
        if (FOut != 1'b1) $display("Fail NEG FOut");

        SData = -23;
        #10;
        if (Result != 18'd23) $display("Fail NEG Result");
        if (ZOut != 1'b0) $display("Fail NEG ZOut");
        if (FOut != 1'b0) $display("Fail NEG FOut");

        SData = 0;
        #10;
        if (Result != 18'd0) $display("Fail NEG Result");
        if (ZOut != 1'b1) $display("Fail NEG ZOut");
        if (FOut != 1'b0) $display("Fail NEG FOut");

        // INV
        Op = 4'o03;
        SData = 18'o252525;
        #10;
        if (Result != 18'o525252) $display("Fail INV Result");
        if (ZOut != 1'b0) $display("Fail INV ZOut");
        if (FOut != 1'b1) $display("Fail INV FOut");

        SData = 18'o525252;
        #10;
        if (Result != 18'o252525) $display("Fail INV Result");
        if (ZOut != 1'b0) $display("Fail INV ZOut");
        if (FOut != 1'b0) $display("Fail INV FOut");

        SData = 18'o000000;
        #10;
        if (Result != 18'o777777) $display("Fail INV Result");
        if (ZOut != 1'b0) $display("Fail INV ZOut");
        if (FOut != 1'b1) $display("Fail INV FOut");

        SData = 18'o777777;
        #10;
        if (Result != 18'o000000) $display("Fail INV Result");
        if (ZOut != 1'b1) $display("Fail INV ZOut");
        if (FOut != 1'b0) $display("Fail INV FOut");

        // SHR
        Op = 4'o04;
        DData = 18'o252525;
        SData = 18'o000003;
        #10;
        if (Result != 18'o025252) $display("Fail SHR Result");
        if (ZOut != 1'b0) $display("Fail SHR ZOut");
        if (FOut != 1'b1) $display("Fail SHR FOut");

        DData = 18'o525252;
        SData = 18'o000003;
        #10;
        if (Result != 18'o052525) $display("Fail SHR Result");
        if (ZOut != 1'b0) $display("Fail SHR ZOut");
        if (FOut != 1'b0) $display("Fail SHR FOut");

        // SHL
        Op = 4'o05;
        DData = 18'o252525;
        SData = 18'o000003;
        #10;
        if (Result != 18'o525250) $display("Fail SHL Result");
        if (ZOut != 1'b0) $display("Fail SHL ZOut");
        if (FOut != 1'b0) $display("Fail SHL FOut");

        DData = 18'o525252;
        SData = 18'o000003;
        #10;
        if (Result != 18'o252520) $display("Fail SHL Result");
        if (ZOut != 1'b0) $display("Fail SHL ZOut");
        if (FOut != 1'b1) $display("Fail SHL FOut");

        // ADD
        Op = 4'o11;
        DData = 5;
        SData = 23;
        #10;
        if (Result != 18'd28) $display("Fail ADD Result");
        if (ZOut != 1'b0) $display("Fail ADD ZOut");
        if (FOut != 1'b0) $display("Fail ADD FOut");

        DData = -5;
        SData = 23;
        #10;
        if (Result != 18'd18) $display("Fail ADD Result");
        if (ZOut != 1'b0) $display("Fail ADD ZOut");
        if (FOut != 1'b0) $display("Fail ADD FOut");

        DData = 5;
        SData = -23;
        #10;
        if (Result != -(18'd18)) $display("Fail ADD Result");
        if (ZOut != 1'b0) $display("Fail ADD ZOut");
        if (FOut != 1'b1) $display("Fail ADD FOut");

        DData = -5;
        SData = -23;
        #10;
        if (Result != -(18'd28)) $display("Fail ADD Result");
        if (ZOut != 1'b0) $display("Fail ADD ZOut");
        if (FOut != 1'b1) $display("Fail ADD FOut");

        // SUB
        Op = 4'o12;
        DData = 5;
        SData = 23;
        #10;
        if (Result != -(18'd18)) $display("Fail SUB Result");
        if (ZOut != 1'b0) $display("Fail SUB ZOut");
        if (FOut != 1'b1) $display("Fail SUB FOut");

        DData = -5;
        SData = 23;
        #10;
        if (Result != -(18'd28)) $display("Fail SUB Result");
        if (ZOut != 1'b0) $display("Fail SUB ZOut");
        if (FOut != 1'b1) $display("Fail SUB FOut");

        DData = 5;
        SData = -23;
        #10;
        if (Result != 18'd28) $display("Fail SUB Result");
        if (ZOut != 1'b0) $display("Fail SUB ZOut");
        if (FOut != 1'b0) $display("Fail SUB FOut");

        DData = -5;
        SData = -23;
        #10;
        if (Result != 18'd18) $display("Fail SUB Result");
        if (ZOut != 1'b0) $display("Fail SUB ZOut");
        if (FOut != 1'b0) $display("Fail SUB FOut");

        // MUL
        Op = 4'o13;
        DData = 5;
        SData = 23;
        #10;
        if (Result != 18'd115) $display("Fail MUL Result");
        if (ZOut != 1'b0) $display("Fail MUL ZOut");
        if (FOut != 1'b0) $display("Fail MUL FOut");

        DData = -5;
        SData = 23;
        #10;
        if (Result != -(18'd115)) $display("Fail MUL Result");
        if (ZOut != 1'b0) $display("Fail MUL ZOut");
        if (FOut != 1'b1) $display("Fail MUL FOut");

        DData = 5;
        SData = -23;
        #10;
        if (Result != -(18'd115)) $display("Fail MUL Result");
        if (ZOut != 1'b0) $display("Fail MUL ZOut");
        if (FOut != 1'b1) $display("Fail MUL FOut");

        DData = -5;
        SData = -23;
        #10;
        if (Result != 18'd115) $display("Fail MUL Result");
        if (ZOut != 1'b0) $display("Fail MUL ZOut");
        if (FOut != 1'b0) $display("Fail MUL FOut");

        // AND
        Op = 4'o14;
        DData = 18'o543210;
        SData = 18'o222222;
        #10;
        if (Result != 18'o002200) $display("Fail AND Result");
        if (ZOut != 1'b0) $display("Fail AND ZOut");
        if (FOut != 1'b0) $display("Fail AND FOut");

        // OR
        Op = 4'o15;
        DData = 18'o543210;
        SData = 18'o222222;
        #10;
        if (Result != 18'o763232) $display("Fail OR Result");
        if (ZOut != 1'b0) $display("Fail OR ZOut");
        if (FOut != 1'b1) $display("Fail OR FOut");

        // XOR
        Op = 4'o16;
        DData = 18'o543210;
        SData = 18'o222222;
        #10;
        if (Result != 18'o761032) $display("Fail XOR Result");
        if (ZOut != 1'b0) $display("Fail XOR ZOut");
        if (FOut != 1'b1) $display("Fail XOR FOut");



    end
      
endmodule

