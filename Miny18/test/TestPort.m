#----------------------------------------------------------
# Integer Port Tests
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module TestPort
{
    int ia1[20]

    func main()
    {
        int i1 = 44
        ia1[3] = 66

        IO.INT_TIMER = 55
        Util.checki(0x900, IO.INT_TIMER, 55)

        IO.INT_TIMER = i1 * 4
        Util.checki(0x901, IO.INT_TIMER, 176)

        IO.INT_TIMER = ia1[3]
        Util.checki(0x902, IO.INT_TIMER, 66)

        i1 = IO.INT_TIMER
        Util.checki(0x903, i1, 66)

        i1 = IO.INT_TIMER * 2
        Util.checki(0x904, i1, 132)

        ia1[4] = IO.INT_TIMER
        Util.checki(0x905, ia1[4], 66)

        IO.INT_TIMER = 0
    }
}
