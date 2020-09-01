#----------------------------------------------------------
# If Statement & Loop Statement Tests
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module TestIfLoop
{
    func main()
    {
        int i1 = 0
        int i2 = 0
        bool b1 = true
        bool b2 = true

        if b1 b2 = false
        Util.checkb(0x800, b2, false)

        b1 = false
        if not b1 b2 = true
        Util.checkb(0x801, b2, true)

        if i1 == 1 i2 = 5
        Util.checki(0x802, i2, 0)
        i1 = 1
        if i1 == 1 i2 = 6
        Util.checki(0x803, i2, 6)

        if i1 == 3 or i1 == 1 i2 = 7
        Util.checki(0x804, i2, 7)

        b1 = true
        if b1 i2 = 1
        else  i2 = 2
        Util.checki(0x805, i2, 1)
        b1 = false
        if b1 i2 = 1
        else  i2 = 2
        Util.checki(0x806, i2, 2)

        i1 = 0
        loop
        {
            i1 += 1
            if i1 == 1
            {
                Util.checki(0x807, i1, 1)
            }
            elif i1 == 3
            {
                Util.checki(0x808, i1, 3)
            }
            elif i1 == 5
            {
                Util.checki(0x809, i1, 5)
                continue
            }
            else
            {
                Util.checki(0x80A, i1, 6)
                break
            }
            i1 += 1
        }

        Util.checki(0x80B, i1, 6)
    }
}
