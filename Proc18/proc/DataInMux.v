//////////////////////////////////////////////////////////////////////////////
// Company:     Christle Engineering
// Engineer:    Mike Christle
// Module Name: DataInMux
//
// Mux for data paths into the core.
//
// History: 
// 0.1.0   05/07/2018   File Created
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

module DataInMux(
    input CLK,
    input CONST_RD,
    input PORT_RD,
    input [17:0] RAM,
    input [17:0] CONST,
    input [17:0] PORT,

    output [17:0] Z
    );

    assign Z = ( CONST_RD & !PORT_RD) ? CONST :
               (!CONST_RD &  PORT_RD) ? PORT :
                                        RAM;

endmodule
