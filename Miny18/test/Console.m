#----------------------------------------------------------
# Serial port console routines
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module Console
{
    const CR 10

    func init()
    {
        IO.BAUD_RATE = 326 # 9600
    }

    func putc(int c)
    {
        loop if IO.TX_EMPTY break
        IO.UART_DATA = c
    }

    func getc() int
    {
        if IO.RX_READY
            return IO.UART_DATA
        else
            return 0
    }

    func print_bool(bool b)
    {
        if b putc('T')
        else putc('F')
    }

    func print_hex(int i)
    {
        int shift = 12
        loop
        {
            int nibble = (i >> shift) & 15
            if (nibble < 10)
                putc('0' + nibble)
            else
                putc(55 + nibble)

            shift -= 4
            if shift == -4 break
        }
    }
}
