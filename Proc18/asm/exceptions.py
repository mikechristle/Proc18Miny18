#----------------------------------------------------------------------------
# Company:     Christle Engineering
# Engineer:    Mike Christle
# Module Name: Error Exception
#
# History: 
# 0.1.0   08/16/2020   File Created
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------------------------
# Copyright 2020 Mike Christle
#
# Permission is hereby granted, free of charge, to any person
# obtaining a copy of this software and associated documentation
# files (the "Software"), to deal in the Software without
# restriction, including without limitation the rights to use,
# copy, modify, merge, publish, distribute, sublicense, and/or
# sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following
# conditions:
#
# The above copyright notice and this permission notice shall be
# included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
# EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
# OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
# NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
# HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
# WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
# OTHER DEALINGS IN THE SOFTWARE.
#----------------------------------------------------------------------------

class Error(Exception):

    def __init__(self, msg, line_no):
        self.error_msg = msg
        self.line_no = line_no

    def message(self):
        str = '{0} at line {1}'
        str = str.format(self.error_msg, self.line_no)
        return str

    def __str__(self):
        return self.error_msg

    def __repr__(self):
        return self.error_msg
