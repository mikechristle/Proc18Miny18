//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: ALU
//
// Arithmetic Logic Unit.
//
// History: 
// 0.1.0   04/23/2018   File Created
// 1.0.0   09/01/2020   Initial release
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

module ALU(
    input [3:0] Op,
    input [17:0] SData,
    input [17:0] DData,

    output ZOut,
    output FOut,
    output [17:0] Result
    );

    function [17:0] trunc18(input [31:0] val32);
        trunc18 = val32[17:0];
    endfunction

    assign ZOut = !(|Result);

    wire [4:0] NData = SData[4:0];
 
    assign {FOut, Result} =
     /* CMP */  (Op == 4'O00)? {Result[17], trunc18(DData - SData)} :
     /* NEG */  (Op == 4'O02)? {Result[17], trunc18(18'd0 - SData)} :
     /* INV */  (Op == 4'O03)? {Result[17], ~SData} :
     /* SHR */  (Op == 4'O04)? {DData[0],  trunc18(DData >> NData)} :
     /* SHL */  (Op == 4'O05)? {DData[17], trunc18(DData << NData)} :

     /* ADD */  (Op == 4'O11)? {Result[17], trunc18(DData + SData)} :
     /* SUB */  (Op == 4'O12)? {Result[17], trunc18(DData - SData)} :
     /* MUL */  (Op == 4'O13)? {Result[17], trunc18(DData * SData)} :
     /* AND */  (Op == 4'O14)? {Result[17], DData & SData} :
     /* OR  */  (Op == 4'O15)? {Result[17], DData | SData} :
     /* XOR */  (Op == 4'O16)? {Result[17], DData ^ SData} :

     /* PAS */                 {Result[17], SData};

endmodule
