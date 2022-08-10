package com.tingnichui.common;

public enum KeyCodeEnum {
    TAB("Tab 键",0x09),
    F1("F1 键",0x70),
    F2("F2 键",0x71),
    F3("F3 键",0x72),
    F4("F4 键",0x73),
    ENTER("Enter 键",0x73),
    PERIOD("PERIOD 键",0xBE),
    DOWN("Down 键",0x28);

    private String keyName;
    private long keyCode;

    public String getKeyName() {
        return keyName;
    }

    public void setKeyName(String keyName) {
        this.keyName = keyName;
    }

    public long getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(long keyCode) {
        this.keyCode = keyCode;
    }

    KeyCodeEnum(String keyName, long keyCode) {
        this.keyName = keyName;
        this.keyCode = keyCode;
    }
}
