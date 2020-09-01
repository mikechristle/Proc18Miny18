#----------------------------------------------------------
# Func Call Tests
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module TestFunc
{
    int i1, ia1[10]
    const ca1 1, 2, 3, 4, 5, 6

    func f0()
    {
        i1 += 1
    }

    func f1(int i) int
    {
        f0()
        return i1 + i
    }

    func f2(int i, bool b) int
    {
        f0()
        if b return i + 1
        else return i - 1
    }

    func f3(rom arr) int
    {
        return arr[3]
    }

    func f4(ram arr) int
    {
        return arr[3]
    }

    func main()
    {
        int i = 10
        i1 = 11

        f0()
        Util.checki(0x700, i1, 12)

        i = f1(4)
        Util.checki(0x701, i, 17)

        i = f1(i)
        Util.checki(0x702, i, 31)

        i = f2(6, false)
        Util.checki(0x703, i, 5)

        i = f2(i1, true)
        Util.checki(0x704, i, 16)

        TestFunc.f0()
        Util.checki(0x707, i1, 17)

        i = f3(ca1)
        Util.checki(0x708, i, 4)

        ia1[3] = 55
        i = f4(ia1)
        Util.checki(0x709, i, 55)
    }
}
