#----------------------------------------------------------
# Main function to run all test modules
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module TestAll
{
    func main()
    {
        Util.init()

        TestISR.main()      # 0x1xx
        TestBool.main()     # 0x2xx
        TestInt.main()      # 0x3xx
        TestAssign.main()   # 0x5xx
        TestConst.main()    # 0x6xx
        TestFunc.main()     # 0x7xx
        TestIfLoop.main()   # 0x8xx
        TestPort.main()     # 0x9xx

        Util.results()

        loop halt
    }
}
