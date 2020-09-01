//////////////////////////////////////////////////////////////////////////////
// Company:     Christle CSgineering
// CSgineer:    Mike Christle
// Module Name: Decode3to8
//
// 3 to 8 decoder.
//
// History: 
// 0.1.0   01/04/2018   File Created
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

module Decode3to8(
    input CS,
    input [2:0] SEL,

    output CS0,
    output CS1,
    output CS2,
    output CS3,
    output CS4,
    output CS5,
    output CS6,
    output CS7
    );

    assign CS0 = CS & (SEL == 3'd0);
    assign CS1 = CS & (SEL == 3'd1);
    assign CS2 = CS & (SEL == 3'd2);
    assign CS3 = CS & (SEL == 3'd3);
    assign CS4 = CS & (SEL == 3'd4);
    assign CS5 = CS & (SEL == 3'd5);
    assign CS6 = CS & (SEL == 3'd6);
    assign CS7 = CS & (SEL == 3'd7);

endmodule
