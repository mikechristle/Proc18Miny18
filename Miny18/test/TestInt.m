#----------------------------------------------------------
# Integer Expression Tests
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module TestInt
{
    int ia1[10]

    func main()
    {
        int i1 = 0
        int i2 = 3
        int i3 = 55

        i1 = 1_2_3
        Util.checki(0x300, i1, 123)
        i1 = -234
        Util.checki(0x301, i1, -234)
        i1 = 0b01_01
        Util.checki(0x303, i1, 5)
        i1 = 0o1_2_3
        Util.checki(0x304, i1, 83)
        i1 = 0xA_A
        Util.checki(0x305, i1, 170)

        i1 = 77 + 55
        Util.checki(0x310, i1, 132)
        i1 = 77 + i2
        Util.checki(0x311, i1, 80)
        i1 = i2 + 88
        Util.checki(0x312, i1, 91)
        i1 = i2 + i3
        Util.checki(0x313, i1, 58)
        i1 = i3 - 8000
        Util.checki(0x314, i1, -7945)

        i1 = 44 * 55
        Util.checki(0x320, i1, 2420)
        i1 = i2 * 100
        Util.checki(0x321, i1, 300)
        i1 = 44 * i2
        Util.checki(0x322, i1, 132)
        i1 = i2 * i3
        Util.checki(0x323, i1, 165)
        i1 = 22 + 33 * 44
        Util.checki(0x324, i1, 1474)
        i1 = 22 * 33 + 44
        Util.checki(0x325, i1, 770)

        i1 = +23
        Util.checki(0x330, i1, 23)
        i1 = -24
        Util.checki(0x331, i1, -24)
        i1 = ~5
        Util.checki(0x332, i1, ~5)
        i1 = +i2
        Util.checki(0x333, i1, 3)
        i1 = -i2
        Util.checki(0x334, i1, -3)
        i1 = ~i2
        Util.checki(0x335, i1, -4)
        i1 = 10 + +i2
        Util.checki(0x336, i1, 13)
        i1 = 10 + -i2
        Util.checki(0x337, i1, 7)
        i1 = 10 + ~i2
        Util.checki(0x338, i1, 6)
        i1 = ~i2 + 20
        Util.checki(0x338, i1, 16)

        i2 = 6
        i1 = i2 << 4
        Util.checki(0x340, i1, 96)
        i1 = i2 >> 2
        Util.checki(0x341, i1, 1)
        i1 = i3 * 5 >> i2 - 1
        Util.checki(0x342, i1, 8)
        i1 = i3 + i2 << i2 - 1
        Util.checki(0x343, i1, 1952)
        i1 = 40 - (80 >> i2) + i3
        Util.checki(0x344, i1, 94)
        i1 = i3 + (i2 << 4) - 1
        Util.checki(0x345, i1, 150)
        i1 = 5 + 6 - 7 + 8 - 9
        Util.checki(0x346, i1, 3)
        i1 = 5 * 12 / 3 * 6 / 5
        Util.checki(0x347, i1, 24)

        i1 = 4 & 12
        Util.checki(0x350, i1, 4)
        i1 = i2 & i3
        Util.checki(0x351, i1, 6)
        i1 = 0xFFFF & 0xFFF0 & 0x0FFF & 0xF0FF
        Util.checki(0x352, i1, 0x00F0)
        i1 = i2 & 0xF & i3
        Util.checki(0x353, i1, 6)
        
        i1 = 4 ^ 12
        Util.checki(0x354, i1, 8)
        i1 = i2 ^ i3
        Util.checki(0x355, i1, 49)
        i1 = 0xF ^ 0xA ^ 0x3 ^ 0xF
        Util.checki(0x356, i1, 9)
        i1 = i1 ^ i2 ^ 0x3 ^ i3
        Util.checki(0x357, i1, 59)

        i1 = 0xA | 0x1
        Util.checki(0x358, i1, 11)
        i1 = i2 | i3
        Util.checki(0x359, i1, 55)
        i1 = 0x01 | 0x02 | 0x04 | 0x08
        Util.checki(0x35A, i1, 15)
        i1 = i1 | i2 | 0x04 | i3
        Util.checki(0x35B, i1, 63)

        i1 = (i2 + 23) * i3
        Util.checki(0x360, i1, 1595)
        i1 = i2 * (23 + i3)
        Util.checki(0x361, i1, 468)

        i1 = 4
        ia1[2] = 22
        Util.checki(0x370, ia1[2], 22)
        ia1[i1] = 44
        Util.checki(0x371, ia1[4], 44)
        ia1[i1 - 1] = 33
        Util.checki(0x372, ia1[3], 33)
        ia1[i1] = ia1[2] + 4
        Util.checki(0x373, ia1[4], 26)

        i1 = 55
        ia1[5] = i1
        Util.checki(0x374, ia1[5], 55)
        TestInt.ia1[7] = TestInt.ia1[5]
        Util.checki(0x375, ia1[7], 55)
        ia1[1] = ia1[5] * 2
        Util.checki(0x376, ia1[1], 110)
    }
}
