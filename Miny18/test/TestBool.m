#----------------------------------------------------------
# Boolean expression tests
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module TestBool
{
    func main()
    {
        int i1 = 5
        int i2 = 4

        bool b1 = true
        bool b2 = false
        bool b3 = true
        bool b4 = false

        Util.checkb(0x200, b1, true)
        Util.checkb(0x201, b2, false)
        Util.checkb(0x202, b3, true)
        Util.checkb(0x203, b4, false)

        b1 = i1 == 5
        Util.checkb(0x210, b1, true)
        b1 = 4 == i2
        Util.checkb(0x211, b1, true)
        b1 = i1 == i2
        Util.checkb(0x212, b1, false)

        b1 = 4 != i1
        Util.checkb(0x216, b1, true)
        b1 = 4 != i2
        Util.checkb(0x218, b1, false)

        b1 = true == b2
        Util.checkb(0x220, b1, false)
        b1 = true != b2
        Util.checkb(0x221, b1, true)
        b1 = b2 == true
        Util.checkb(0x222, b1, false)
        b1 = b2 != true
        Util.checkb(0x223, b1, true)
        b1 = false == b2
        Util.checkb(0x224, b1, true)
        b1 = false != b2
        Util.checkb(0x225, b1, false)
        b1 = b2 == false
        Util.checkb(0x226, b1, true)
        b1 = b2 != false
        Util.checkb(0x227, b1, false)

        b1 = i2 >= 3
        Util.checkb(0x230, b1, true)
        b1 = i2 >= 4
        Util.checkb(0x231, b1, true)
        b1 = i2 >= 5
        Util.checkb(0x232, b1, false)
        b1 = 3 >= i2
        Util.checkb(0x236, b1, false)
        b1 = 4 >= i2
        Util.checkb(0x237, b1, true)
        b1 = 5 >= i2
        Util.checkb(0x238, b1, true)

        b1 = i2 <= 3
        Util.checkb(0x240, b1, false)
        b1 = i2 <= 4
        Util.checkb(0x241, b1, true)
        b1 = i2 <= 5
        Util.checkb(0x242, b1, true)
        b1 = 3 <= i2
        Util.checkb(0x246, b1, true)
        b1 = 4 <= i2
        Util.checkb(0x247, b1, true)
        b1 = 5 <= i2
        Util.checkb(0x248, b1, false)

        b1 = i2 < 3
        Util.checkb(0x250, b1, false)
        b1 = i2 < 4
        Util.checkb(0x251, b1, false)
        b1 = i2 < 5
        Util.checkb(0x252, b1, true)
        b1 = 3 < i2
        Util.checkb(0x246, b1, true)
        b1 = 4 < i2
        Util.checkb(0x247, b1, false)
        b1 = 5 < i2
        Util.checkb(0x248, b1, false)

        b1 = i2 > 3
        Util.checkb(0x250, b1, true)
        b1 = i2 > 4
        Util.checkb(0x251, b1, false)
        b1 = i2 > 5
        Util.checkb(0x252, b1, false)
        b1 = 3 > i2
        Util.checkb(0x256, b1, false)
        b1 = 4 > i2
        Util.checkb(0x257, b1, false)
        b1 = 5 > i2
        Util.checkb(0x258, b1, true)

        b1 = b2 != true
        Util.checkb(0x260, b1, true)
        b1 = b2 != false
        Util.checkb(0x261, b1, false)
        b1 = b3 == true
        Util.checkb(0x262, b1, true)
        b1 = b3 == false
        Util.checkb(0x263, b1, false)

        b1 = b2 and b3 and b4
        Util.checkb(0x264, b1, false)
        b1 = b2 and b3 or b4
        Util.checkb(0x265, b1, false)
        b1 = b2 or b3 and b4
        Util.checkb(0x266, b1, false)
        b1 = b2 or b3 or b4
        Util.checkb(0x267, b1, true)

        b1 = not b2
        Util.checkb(0x268, b1, true)
        b1 = not b3
        Util.checkb(0x269, b1, false)

        IO.LED0 = true
        Util.checkb(0x270, IO.LED0, true)
        IO.LED1 = false
        Util.checkb(0x271, IO.LED1, false)
        IO.LED0 = b1
        Util.checkb(0x272, IO.LED0, false)
        IO.LED0 = not b1
        Util.checkb(0x273, IO.LED0, true)
        IO.LED0 = IO.LED1
        Util.checkb(0x274, IO.LED0, false)
        IO.LED0 = not IO.LED1
        Util.checkb(0x275, IO.LED0, true)
        IO.LED0 = 1 == i1
        Util.checkb(0x276, IO.LED0, false)
    }
}
