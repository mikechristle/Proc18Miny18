//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: I2C_Master_TF
//
// Test the I2C_Master utility module.
//
// History: 
// 1.0.0   09/04/2020   Initial release
//////////////////////////////////////////////////////////////////////////////

`timescale 1ns / 1ps

module I2C_Master_TF;

    // Inputs
    reg CLK;
    reg RESET;
    reg CS;
    reg WE;
    reg AD;
    reg [7:0] DI;
    reg SDA_I;

    // Outputs
    wire [7:0] DO;
    wire DONE;
    wire ERROR;
    wire SCL;
    wire SDA_E;
    wire SDA_O;

    // InOut
    wire SDA;

    // Instantiate UUT
    I2C_Master #(
        .CLK_FREQ(4.0E6))
    uut (
        .CLK(CLK), 
        .RESET(RESET), 
        .CS(CS), 
        .WE(WE), 
        .AD(AD), 
        .DI(DI), 
        .DO(DO), 
        .DONE(DONE), 
        .ERROR(ERROR), 
        .SCL(SCL), 
        .SDA(SDA), 
        .SDA_E(SDA_E),
        .SDA_O(SDA_O),
        .SDA_I(SDA_I)
    );

    always #10 CLK = ~CLK;

    initial begin
        CLK = 0;
        RESET = 1;
        CS = 0;
        WE = 0;
        AD = 0;
        DI = 0;
        SDA_I = 1;

        #100;
        RESET = 0;
        #100;

        // Send Start
        AD = 1;
        DI = 1;
        CS = 1;
        WE = 1;
        #20;
        AD = 0;
        DI = 0;
        CS = 0;
        WE = 0;
        #300;

        // Write data reg
        AD = 0;
        DI = 8'hAA;
        CS = 1;
        WE = 1;
        #20;
        AD = 0;
        DI = 0;
        CS = 0;
        WE = 0;
        #200;

        // Send write command
        AD = 1;
        DI = 6;
        CS = 1;
        WE = 1;
        #20;
        AD = 0;
        DI = 0;
        CS = 0;
        WE = 0;
        #2500;

        // Send Re-Start
        AD = 1;
        DI = 1;
        CS = 1;
        WE = 1;
        #20;
        AD = 0;
        DI = 0;
        CS = 0;
        WE = 0;
        #300;

        // Send read ack cmnd
        AD = 1;
        DI = 4;
        CS = 1;
        WE = 1;
        #20;
        AD = 0;
        DI = 0;
        CS = 0;
        WE = 0;
        #2500;

        // Send read nack cmnd
        AD = 1;
        DI = 5;
        CS = 1;
        WE = 1;
        #20;
        AD = 0;
        DI = 0;
        CS = 0;
        WE = 0;
        
        #2500;

        // Send Stop
        AD = 1;
        DI = 2;
        CS = 1;
        WE = 1;
        #20;
        AD = 0;
        DI = 0;
        CS = 0;
        WE = 0;

        #400;
        $stop;
    end
      
endmodule

