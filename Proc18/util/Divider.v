//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: Divider
//
// Signed or unsigned integer division.
// If only unsigned devision is needed, tie SN to gnd.
// If only signed devision is needed, tie SN to Vcc.
// If both are needed tie SN to AD[1].
//
// Write the numerator followed by the denominator.
// Wait for DONE to go HI, or 18 clock cycles.
// Read the results as needed.
//
// Write
// Reg 0 Unsigned Numerator
// Reg 1 Unsigned Denominator
// Reg 2 Signed Numerator
// Reg 3 Signed Denominator
//
// Read
// Reg 0 Unsigned Quotient
// Reg 1 Unsigned Remainder
// Reg 2 Signed Remainder
// Reg 3 Signed denominator
//
// History: 
// 0.1.0   08/03/2020   File Created
// 1.0.0   09/01/2020   Initial release
//////////////////////////////////////////////////////////////////////////////
// SDIV     mov     s, #0
//          cmps    num, #0  wc
//    if_c  neg     num, num
//    if_c  mov     s, #3
//          cmps    den, #0  wc
//    if_c  neg     den, den
//    if_c  xor     s, #1
//          call    UDIV
//          shr     s, #1  wc
//    if_c  neg     quo, quo
//          shr     s, #1  wc
//    if_c  neg     rem, rem
//          ret
//
// UDIV     mov     quo, #0
//          mov     rem, #0
//          mov     t, #18
// UDIV1    shl     num, #1  wc
//          rcl     rem, #1
//          cmpsub  rem, den  wc
//          rcl     quo, #1
//          djnz    t, UDIV1
//          ret
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

module Divider(CLK, CS, WE, SN, AD, DI, DO, DONE);

    input CLK;
    input CS;
    input WE;
    input SN;
    input AD;
    input [17:0] DI;

    output DONE;
    output [17:0] DO;

    reg [17:0] num = 0; // Numerator
    reg [17:0] den = 0; // Denominator
    reg [17:0] quo = 0; // Quotient
    reg [17:0] rem = 0; // Remainder

    reg neg_quo = 0; // Negate Quotient
    reg neg_rem = 0; // Negate Remainder

    reg [4:0] cnt = 0;

    assign DONE = cnt == 0;

    assign DO = (!AD & !neg_quo) ? quo :
                (!AD &  neg_quo) ? (~quo) + 1 :
                ( AD & !neg_rem) ? rem :
              /*( AD &  neg_rem)*/ (~rem) + 1;

    always @ (posedge CLK) begin

        casex ({CS, WE, AD})
            3'b110: begin
                if (SN && DI[17]) begin
                    neg_quo = 1;
                    neg_rem = 1;
                    num = (~DI) + 1;
                end
                else begin
                    neg_quo = 0;
                    neg_rem = 0;
                    num = DI;
                end
            end
            3'b111: begin
                if (SN && DI[17]) begin
                    neg_quo = !neg_quo;
                    den = (~DI) + 1;
                end
                else begin
                    den = DI;
                end
                quo = 0;
                rem = 0;
                cnt = 18;
            end
        endcase

        if (!DONE) begin
            rem = {rem[16:0], num[17]};
            num = {num[16:0], 1'd0};

            if (rem >= den) begin
                rem = rem - den;
                quo = {quo[16:0], 1'd1};
            end
            else begin
                quo = {quo[16:0], 1'd0};
            end
            cnt = cnt - 1;
        end
    end

endmodule
