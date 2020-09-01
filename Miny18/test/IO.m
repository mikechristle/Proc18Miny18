#----------------------------------------------------------
# Test board IO port definitions
#
# History: 
# 1.0.0   09/01/2020   Initial release
#----------------------------------------------------------

module IO
{
    port INT_CLR   0o00
    port INT_SET   0o01

    port INT_TIMER 0o20

    port BAUD_RATE 0o40

    port UART_DATA 0o60

    bool TEST 10

    bool LED0      0o64
    bool LED1      0o65
    bool LED2      0o66
    bool LED3      0o67
    bool LED4      0o70
    bool LED5      0o71
    bool LED6      0o72
    bool LED7      0o73
    bool LED8      0o74
    bool LED9      0o75

    bool RX_READY  0o76
    bool TX_EMPTY  0o77
}
