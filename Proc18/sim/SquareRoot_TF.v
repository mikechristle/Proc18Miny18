//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: SquareRoot_TF
//
// Test the SquareRoot utility module.
//
// History: 
// 1.0.0   09/09/2020   Initial release
//////////////////////////////////////////////////////////////////////////////

`timescale 1ns / 1ps

module SquareRoot_TF;

    // Inputs
    reg CLK;
    reg CS;
    reg WE;
    reg [17:0] DI;

    // Outputs
    wire DONE;
    wire [17:0] DO;

    // Instantiate the Unit Under Test (UUT)
    SquareRoot uut (
        .CLK(CLK),
        .CS(CS),
        .WE(WE),
        .DI(DI),
        .DO(DO),
        .DONE(DONE)
    );

    function [17:0] test_value;
        input [2:0] index;
        begin
            case (index)
            0: test_value = 18'd0;
            1: test_value = 18'd16;
            2: test_value = 18'd1000;
            3: test_value = 18'd2500;
            4: test_value = 18'd10000;
            5: test_value = 18'd25000;
            6: test_value = 18'd100000;
            7: test_value = 18'd250000;
            endcase
        end
    endfunction

    function [17:0] result_value;
        input [2:0] index;
        begin
            case (index)
            0: result_value = 18'd0;
            1: result_value = 18'd4;
            2: result_value = 18'd31;
            3: result_value = 18'd50;
            4: result_value = 18'd100;
            5: result_value = 18'd158;
            6: result_value = 18'd316;
            7: result_value = 18'd500;
            endcase
        end
    endfunction

    integer idx = 0;

    initial begin
        // Initialize Inputs
        CLK = 0;
        CS = 0;
        WE = 0;
        DI = 0;

        repeat (8) begin
            #10 CLK = 0; #10 CLK = 1;
            #10 CLK = 0; #10 CLK = 1;

            CS = 1;
            WE = 1;
            DI = test_value(idx);
            #10 CLK = 0; #10 CLK = 1;
            DI = 0;
            CS = 0;
            WE = 0;

            if (DONE != 1'b0) $display("Failed DONE LO %0d", idx);

            repeat (9) begin
                #10 CLK = 0; #10 CLK = 1;
            end

            if (DONE != 1'b1) $display("Failed DONE HI %0d", idx);
            if (DO != result_value(idx)) $display("Failed result %0d", idx);
            idx = idx + 1;
        end
    end
      
endmodule

