#----------------------------------------------------------
# Assignment Statements Tests
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module TestAssign
{
    int ia1[20]

    func main()
    {
        int i1 = 4
        int i2 = 5

        i1 += 4
        Util.checki(0x500, i1, 8)
        i1 -= 2
        Util.checki(0x501, i1, 6)
        i1 *= 66
        Util.checki(0x502, i1, 396)
        i1 = 6
        i1 |= 9
        Util.checki(0x505, i1, 15)
        i1 &= 12
        Util.checki(0x506, i1, 12)
        i1 ^= 5
        Util.checki(0x507, i1, 9)
        i1 <<= 2
        Util.checki(0x508, i1, 36)
        i1 >>= 2
        Util.checki(0x509, i1, 9)

        i1 += i2
        Util.checki(0x510, i1, 14)
        i1 -= i2
        Util.checki(0x511, i1, 9)
        i1 *= i2
        Util.checki(0x512, i1, 45)

        i1 = 15
        i1 &= i2
        Util.checki(0x515, i1, 5)
        i1 = 0
        i1 |= i2
        Util.checki(0x516, i1, 5)
        i1 = 12
        i1 ^= i2
        Util.checki(0x517, i1, 9)

        i1 <<= i2
        Util.checki(0x518, i1, 288)
        i1 >>= i2
        Util.checki(0x519, i1, 9)
        i1 = -i2
        Util.checki(0x51A, i1, -5)
        i1 = ~i2
        Util.checki(0x51B, i1, ~5)

        i1 = 6
        ia1[2] = 22
        Util.checki(0x520, ia1[2], 22)
        ia1[3] = 3
        Util.checki(0x521, ia1[3], 3)
        ia1[2] += 4
        Util.checki(0x522, ia1[2], 26)
        ia1[2] -= i1
        Util.checki(0x523, ia1[2], 20)
        ia1[2] *= ia1[3]
        Util.checki(0x524, ia1[2], 60)
    }
}
