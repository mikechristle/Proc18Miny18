//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Divider_TF
//
// Test the Divider utility module.
//
// History: 
// 1.0.0   09/04/2020   Initial release
//////////////////////////////////////////////////////////////////////////////

`timescale 1ns / 1ps

module Divider_TF;

    // Inputs
    reg CLK;
    reg CS;
    reg WE;
    reg SN;
    reg AD;
    reg [17:0] DI;

    // Outputs
    wire DONE;
    wire [17:0] DO;

    // Instantiate the Unit Under Test (UUT)
    Divider uut (
        .CLK(CLK),
        .CS(CS),
        .WE(WE),
        .SN(SN),
        .AD(AD),
        .DI(DI),
        .DO(DO),
        .DONE(DONE)
    );

    initial begin
        // Initialize Inputs
        CLK = 0;
        CS = 0;
        WE = 0;
        SN = 1;
        AD = 0;
        DI = 0;

        // Wait 100 ns for global reset to finish
        #15;
        
        //------------------------------------------------------------------------------------------
        #5 CLK = 0; #10 CLK = 1; #5;
        #5 CLK = 0; #10 CLK = 1; #5;

        // 23 / 5 -> 4R3
        CS = 1;
        WE = 1;
        AD = 0;
        DI = 18'd23;
        #5 CLK = 0; #10 CLK = 1; #5;
        AD = 1;
        DI = 18'd5;
        #5 CLK = 0; #10 CLK = 1; #5;
        DI = 0;
        CS = 0;
        WE = 0;

        repeat (18) begin
            #5 CLK = 0; #10 CLK = 1; #5;
        end

        if (DO != 18'o000003) $display("Rem error 1");
        AD = 0;
        #5 CLK = 0; #10 CLK = 1; #5;
        if (DO != 18'o000004) $display("Quo error 1");

        // 23 / -5 -> -4R3
        CS = 1;
        WE = 1;
        AD = 0;
        DI = 18'd23;
        #5 CLK = 0; #10 CLK = 1; #5;
        AD = 1;
        DI = -(18'd5);
        #5 CLK = 0; #10 CLK = 1; #5;
        DI = 0;
        CS = 0;
        WE = 0;

        repeat (18) begin
            #5 CLK = 0; #10 CLK = 1; #5;
        end

        if (DO != 18'o000003) $display("Rem error 2");
        AD = 0;
        #5 CLK = 0; #10 CLK = 1; #5;
        if (DO != 18'o777774) $display("Quo error 2");

        // // -23 / 5 -> -4R-3
        CS = 1;
        WE = 1;
        AD = 0;
        DI = -(18'd23);
        #5 CLK = 0; #10 CLK = 1; #5;
        AD = 1;
        DI = 18'd5;
        #5 CLK = 0; #10 CLK = 1; #5;
        DI = 0;
        CS = 0;
        WE = 0;

        repeat (18) begin
            #5 CLK = 0; #10 CLK = 1; #5;
        end

        if (DO != 18'o777775) $display("Rem error 3");
        AD = 0;
        #5 CLK = 0; #10 CLK = 1; #5;
        if (DO != 18'o777774) $display("Quo error 3");

        // -23 / -5 -> 4R-3
        CS = 1;
        WE = 1;
        AD = 0;
        DI = -(18'd23);
        #5 CLK = 0; #10 CLK = 1; #5;
        AD = 1;
        DI = -(18'd5);
        #5 CLK = 0; #10 CLK = 1; #5;
        DI = 0;
        CS = 0;
        WE = 0;

        repeat (18) begin
            #5 CLK = 0; #10 CLK = 1; #5;
        end

        if (DO != 18'o777775) $display("Rem error 4");
        AD = 0;
        #5 CLK = 0; #10 CLK = 1; #5;
        if (DO != 18'o000004) $display("Quo error 4");

        // 200050 / 500 -> 400R50
        SN = 0;
        CS = 1;
        WE = 1;
        AD = 0;
        DI = 18'd200050;
        #5 CLK = 0; #10 CLK = 1; #5;
        AD = 1;
        DI = 18'd500;
        #5 CLK = 0; #10 CLK = 1; #5;
        DI = 0;
        CS = 0;
        WE = 0;

        repeat (18) begin
            #5 CLK = 0; #10 CLK = 1; #5;
        end

        if (DO != 18'd50) $display("Rem error 5");
        AD = 0;
        #5 CLK = 0; #10 CLK = 1; #5;
        if (DO != 18'd400) $display("Quo error 5");

        // 500 / 200050 -> 0R500
        SN = 0;
        CS = 1;
        WE = 1;
        AD = 0;
        DI = 18'd500;
        #5 CLK = 0; #10 CLK = 1; #5;
        AD = 1;
        DI = 18'd200050;
        #5 CLK = 0; #10 CLK = 1; #5;
        DI = 0;
        CS = 0;
        WE = 0;

        repeat (18) begin
            #5 CLK = 0; #10 CLK = 1; #5;
        end

        if (DO != 18'd500) $display("Rem error 6");
        AD = 0;
        #5 CLK = 0; #10 CLK = 1; #5;
        if (DO != 18'd0) $display("Quo error 6");

    end
      
endmodule

