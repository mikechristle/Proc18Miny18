#----------------------------------------------------------
# Test Utilities
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module Util
{
    int fail_count, test_count

    func init()
    {
        fail_count = 0
        test_count = 0
        Console.init()
    }

    func results()
    {
        Console.putc(Console.CR)
        Console.print_hex(test_count)
        Console.putc(' ')
        Console.print_hex(fail_count)
        Console.putc(Console.CR)
    }

    func checki(int tn, int value, int expect)
    {
        test_count += 1
        Console.print_hex(tn)
        Console.putc(' ')

        if value == expect
            Console.putc('P')
        else
        {
            fail_count += 1
            Console.putc('F')
        }

        Console.putc(' ')
        Console.print_hex(value)
        Console.putc(' ')
        Console.putc(Console.CR)
    }

    func checkb(int tn, bool value, bool expect)
    {
        test_count += 1
        Console.print_hex(tn)
        Console.putc(' ')

        if value == expect
            Console.putc('P')
        else
        {
            fail_count += 1
            Console.putc('F')
        }

        Console.putc(' ')
        Console.print_bool(value)
        Console.putc(' ')
        Console.putc(Console.CR)
    }
}
