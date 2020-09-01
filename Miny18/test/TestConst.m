#----------------------------------------------------------
# Test constant statements
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module TestConst
{
    const WIDTH  50
    const HEIGHT 100 + WIDTH
    const SIZE   WIDTH * HEIGHT

    const TABLE1 1, 2, SIZE, 4 * 5
    const MSG    "Hello world."

    func main()
    {
        int i1 = WIDTH * HEIGHT
        int i2 = SIZE / 10
        int i4 = 5 * 6
        bool b1 = true

        Util.checki(0x600, i1, 7500)
        Util.checki(0x601, i2, 750)
        Util.checki(0x602, i4, 30)

        Util.checki(0x604, TABLE1[0], 1)
        Util.checki(0x605, TABLE1[1], 2)
        Util.checki(0x606, TABLE1[2], 7500)
        Util.checki(0x607, TABLE1[3], 20)
        Util.checki(0x608, MSG[4], ('l' << 9) | 'r')

        b1 = not false
        Util.checkb(0x610, b1, true)
        b1 = not true
        Util.checkb(0x611, b1, false)

        b1 = true == true
        Util.checkb(0x620, b1, true)
        b1 = true == false
        Util.checkb(0x621, b1, false)
        b1 = true != true
        Util.checkb(0x622, b1, false)
        b1 = true != false
        Util.checkb(0x623, b1, true)

        b1 = 4 == 5
        Util.checkb(0x630, b1, false)
        b1 = 4 == 4
        Util.checkb(0x631, b1, true)
        b1 = 4 != 5
        Util.checkb(0x632, b1, true)
        b1 = 4 != 4
        Util.checkb(0x633, b1, false)

        b1 = 4 > 3
        Util.checkb(0x640, b1, true)
        b1 = 4 > 4
        Util.checkb(0x641, b1, false)
        b1 = 4 > 5
        Util.checkb(0x642, b1, false)
        b1 = 4 < 3
        Util.checkb(0x643, b1, false)
        b1 = 4 < 4
        Util.checkb(0x644, b1, false)
        b1 = 4 < 5
        Util.checkb(0x645, b1, true)

        b1 = 4 >= 3
        Util.checkb(0x650, b1, true)
        b1 = 4 >= 4
        Util.checkb(0x651, b1, true)
        b1 = 4 >= 5
        Util.checkb(0x652, b1, false)
        b1 = 4 <= 3
        Util.checkb(0x653, b1, false)
        b1 = 4 <= 4
        Util.checkb(0x654, b1, true)
        b1 = 4 <= 5
        Util.checkb(0x655, b1, true)
    }
}
