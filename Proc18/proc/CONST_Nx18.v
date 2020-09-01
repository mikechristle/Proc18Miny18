//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: CONST_Nx18 
//
// ROM Block for constants.
// Parameter ADRS_BITS sets the memory size by setting the number of address
// bits. Proc18 supports up to 18 bits, or 256K words. Default is 4K words.
// The file const.hex contains the data for this ROM. It should have the same
// number of hexadecimal words as the ROM size.
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

module CONST_Nx18(CLK, AD, DO);

    parameter ADRS_BITS = 12;
    localparam MEM_SIZE = (2 ** ADRS_BITS) - 1;

    input CLK;
    input [ADRS_BITS - 1:0] AD;

    output [17:0] DO;

    reg [ADRS_BITS - 1:0] adrs = 0;
    reg [17:0] mem_array [MEM_SIZE:0];

    initial begin
        $readmemh("const.hex", mem_array);
    end

    assign DO = mem_array[adrs];

    always @ (posedge CLK) begin
        adrs <= AD;
    end
endmodule
