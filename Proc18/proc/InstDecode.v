//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: InstDecode 
//
// Description: Decode instruction opcodes and generate control signals.
//
// History: 
// 0.1.0   04/29/2018   File Created
// 1.0.0   09/01/2020   Initial release
// 1.1.0   09/07/2020   Add one clock delay on RUN input to sync to CLK
// 1.2.0   09/08/2020   Optimize interrupt processing and cleanup
//////////////////////////////////////////////////////////////////////////////
// Copyright 2020 Mike Christle
//
// Permission is hereby granted, free of charge, to any person
// obtaining a copy of this software and associated documentation
// files (the "Software"), to deal in the Software without
// restriction, including without limitation the rights to use,
// copy, modify, merge, publish, distribute, sublicense, and/or
// sell copies of the Software, and to permit persons to whom the
// Software is furnished to do so, subject to the following
// conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
// OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
// HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
// WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
// FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
// OTHER DEALINGS IN THE SOFTWARE.
//////////////////////////////////////////////////////////////////////////////

module InstDecode(
    CLK, RUN, VECTOR, INST,
    DATAIN, TIMER_ZERO, BRANCH,
    CONST_RD, PORT_WR, PORT_RD, RAM_WR,
    RESET, RUNS, PC, SP, PCO,
    DATA_SL, TIMER_LD, NDATA, BRANCH_OP,
    BIT_LD, STAT_LD, ALU_OP, ALUS_SL,
    ADRS_SL, REG_WE, DADRS, SADRS
    );

    input CLK;
    input RUN;
    input [3:0] VECTOR;
    input [17:0] INST;
    input [17:0] DATAIN;
    input TIMER_ZERO;
    input BRANCH;

    output reg CONST_RD = 0;
    output reg PORT_WR = 0;
    output reg PORT_RD = 0;
    output reg RAM_WR = 0;
    output reg RESET = 1;
    output reg RUNS = 0;

    output reg [11:0] PC = 0;
    output reg [11:0] PCO = 0;
    output reg [17:0] SP = 0;

    output reg DATA_SL = 0;
    output reg [2:0] ADRS_SL = 0;
    output     TIMER_LD;
    output reg [1:0] BIT_LD = 0;
    output reg [1:0] STAT_LD = 0;
    output reg [3:0] ALU_OP = 0;
    output reg [1:0] ALUS_SL = 0;
    output reg REG_WE = 0;
    output reg [5:0] DADRS = 0;
    output reg [5:0] SADRS = 0;
    output reg [11:0] NDATA = 0;
    output reg [2:0] BRANCH_OP = 0;

//-------------------------------------------------------------------
    localparam STATE_0 = 10'b0000000001, // 0001 Normal
               STATE_1 = 10'b0000000010, // 0002 LDI
               STATE_2 = 10'b0000000100, // 0004 LDR
               STATE_3 = 10'b0000001000, // 0010 LDC
               STATE_4 = 10'b0000010000, // 0020 STOP
               STATE_5 = 10'b0000100000, // 0040 RTS, RTI
               STATE_6 = 10'b0001000000, // 0100 RTS, RTI
               STATE_7 = 10'b0010000000, // 0200 JXX
               STATE_8 = 10'b0100000000, // 0400 
               STATE_9 = 10'b1000000000; // 1000 Interrupt

    reg [9:0] state = 0;
    reg [5:0] dadrs_temp = 0;
    reg [3:0] level = 15;
    reg stat_flag = 0;

    wire [11:0] pc_plus_one = PC + 12'd1;
    wire interrupt = (VECTOR > level) && (state[0] || state[4]);

    wire op_halt  = INST[17:6] == 12'o0001;
    wire op_pause = INST[17:6] == 12'o0002;
    wire op_rts   = INST[17:6] == 12'o0003;
    wire op_rti   = INST[17:6] == 12'o0004;
    wire op_level = INST[17:6] == 12'o0005;
    wire op_reset = INST[17:6] == 12'o0006;
    wire op_ldi   = INST[17:6] == 12'o0007;

    wire op_call  = INST[17:12] == 6'o01;
    wire op_ldr   = INST[17:12] == 6'o02;
    wire op_str   = INST[17:12] == 6'o03;
    wire op_ldc   = INST[17:12] == 6'o04;
    wire op_timer = INST[17:12] == 6'o05;
    wire op_in    = INST[17:12] == 6'o06;
    wire op_out   = INST[17:12] == 6'o07;
    wire op_sxx   = INST[17:15] == 3'o1;
    wire op_jxx   = INST[17:15] == 3'o2;
    wire op_alub  = INST[17:15] == 3'o3;

    wire op_alu   = INST[17] && INST[15:12] != 0;
    wire op_cmp   = INST[17] && INST[15:12] == 0;
    wire op_alui  = INST[17:16] == 2'o3;

    assign TIMER_LD = state[0] && op_timer;   // TIMER

//-------------------------------------------------------------------
    always @ (posedge CLK) begin
        RUNS <= RUN;

        if (!RUN || !RUNS)
            begin
            PC <= 0;
            state <= STATE_0;
            RESET <= 1;
            end
        else
            begin

            RESET <= state[0] & op_reset;  // RESET

            PC <= (interrupt)                        ? {14'd0, VECTOR} : // Interrupt
                  (state[9])                         ? PC :              // Interrupt
                  (state[7] && BRANCH)               ? INST[11:0] :      // JXX
                  (state[0] && op_jxx)               ? PC :              // JXX
                  (state[2] || state[3] || state[4]) ? PC :              // LDR, LDC, STOP
                  (state[0] && op_call)              ? INST[11:0] :      // CALL
                  (op_pause && !TIMER_ZERO)          ? PC :              // PAUSE
                  (state[6])                         ? DATAIN :          // RTS, RTI 
                                                       pc_plus_one;      // Default

            PCO <= (interrupt)             ? PC :          // Interrupt
                   (state[0] && op_call)   ? pc_plus_one : // CALL
                                             PCO;          // Default

            SP <= (state[9])                ? SP - 1 : // Interrupt
                  (state[0] && op_call)     ? SP - 1 : // CALL
                  (state[6])                ? SP + 1 : // RTS, RTI
                  (state[0] && PC == 12'd0) ? 18'd0  : // Restart
                                              SP;      // Default

            state <= (interrupt)           ? STATE_9 : // Interrupt
                     (state[0] && op_ldi)  ? STATE_1 : // LDI
                     (state[0] && op_ldr)  ? STATE_2 : // LDR 
                     (state[0] && op_ldc)  ? STATE_3 : // LDC
                     (state[0] && op_halt) ? STATE_4 : // HALt
                     (state[4])            ? STATE_4 : // HALT
                     (state[0] && op_rts)  ? STATE_5 : // RTS
                     (state[0] && op_rti)  ? STATE_5 : // RTI
                     (state[5])            ? STATE_6 : // RTS, RTI
                     (state[0] && op_jxx)  ? STATE_7 : // JXX
                                             STATE_0;  // Default

            DATA_SL  <= state[9] ||           // Interrupt
                       (state[0] && op_call); // CALL

            level <= (interrupt)               ? VECTOR :        // Interrupt
                     (state[0] && op_level)    ? INST[3:0] :     // LEVEL
                     (state[6] && stat_flag)   ? DATAIN[15:12] : // RTI
                     (state[0] && PC == 12'd0) ? 4'd15 :         // RESTART
                                                 level;          // Default 

            RAM_WR   <= (state[9])            || // Interrupt
                        (state[0] && op_call) || // CALL
                        (state[0] && op_str);    // STR

            CONST_RD <=  state[3]; // LDC

            PORT_RD  <=  state[0] && op_in;   // IN

            PORT_WR  <=  state[0] && op_out;  // OUT

            ADRS_SL <= (state[0] && op_str) ? 3'd2 : // STR
                       (state[0] && op_in)  ? 3'd3 : // IN
                       (state[0] && op_out) ? 3'd3 : // OUT
                       (state[0] && op_ldc) ? 3'd0 : // LDC
                       (state[0] && op_ldr) ? 3'd0 : // LDR
                                              3'd1;  // Int, RTS, RTI, CALL

            BIT_LD <= (state[0] && op_alub && INST[14:12] != 0) ? 2'd1 : // ALUB
                      (state[0] && op_sxx)                      ? 2'd2 : // SXX
                                                                  2'd0 ; // Default

            dadrs_temp <= INST[5:0];
            DADRS <= (state[1] || state[2] || state[3]) ? dadrs_temp : // LDI, LDR, LDC
                                                          INST[5:0];   // ALU
            SADRS <= INST[11:6];

            REG_WE <= (state[0] && op_ldi) || // LDI
                      (state[2])           || // LDR
                      (state[3])           || // LDC
                      (state[0] && op_in)  || // IN
                      (state[0] && op_alu);   // ALU, ALUI

            ALUS_SL <= (state[0] && op_ldi)   ? 2'd2 : // LDI
                       (state[2] || state[3]) ? 2'd1 : // LDR, LDC
                       (state[0] && op_in)    ? 2'd1 : // IN
                       (state[0] && op_alui)  ? 2'd3 : // ALUI
                                                2'd0;  // ALU

            ALU_OP <= (state[2] || state[3]) ? 4'd1 :       // LDR, LDC
                      (state[0] && op_ldi)   ? 4'd1 :       // LDI
                      (state[0] && op_in)    ? 4'd1 :       // IN
                                               INST[15:12]; // ALU, ALUB

            if (state[0]) stat_flag <= op_rti;

            STAT_LD <= (state[5] && stat_flag) ? 2'd3 : // RTI
                       (state[0] && op_cmp)    ? 2'd1 : // CMP, CMPI
                       (state[0] && op_alub)   ? 2'd2 : // ALUB
                                                 2'd0;  // Default

            NDATA <= (interrupt)           ? {8'd0, level} :      // Interrupt
                     (state[0] && op_alui) ? {6'd0, INST[11:6]} : // ALUI
                     (state[0] && op_in)   ? {6'd0, INST[11:6]} : // IN
                     (state[0] && op_out)  ? {6'd0, INST[5:0]} :  // OUT
                                             NDATA;               // Default

            BRANCH_OP <= INST[14:12];

            end
    end

endmodule
