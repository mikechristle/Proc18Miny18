#----------------------------------------------------------
# Interrupt Service Routines Tests
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module TestISR
{
    int i1, i2

    func main()
    {
        i1 = 0
        IO.INT_TIMER = 2
        IO.INT_SET = 0x10
        level 0
        
        timer 3500
        pause
        Util.checki(0x100, i1, 3)

        level 15
        i1 = 0

        timer 3500
        pause
        nop
        Util.checki(0x102, i1, 0)

        IO.INT_TIMER = 0
        IO.INT_CLR = 0x10
    }

    func timer_isr() isr 5
    {
        i2 = IO.INT_TIMER
        i1 += 1
    }
}
