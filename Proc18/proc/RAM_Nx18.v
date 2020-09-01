//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: RAM_Nx18 
//
// RAM Block.
// Parameter ADRS_BITS sets the memory size by setting the number of address
// bits. Proc18 supports up to 18 bits, or 256K words. Default is 4K words.
//
// History: 
// 0.1.0   05/01/2018   File Created
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
module RAM_Nx18(CLK, WR, AD, DI, DO);

    parameter ADRS_BITS = 12;
    localparam MEM_SIZE = (2 ** ADRS_BITS) - 1;

    input CLK;
    input WR;
    input [ADRS_BITS - 1:0] AD;
    input [17:0] DI;

    output reg [17:0] DO;

    reg [17:0] mem_array [MEM_SIZE:0];

    always @ (posedge CLK) begin
        if (WR) mem_array[AD] <= DI;
        DO <= mem_array[AD];
    end

endmodule
