package com.tingnichui.util;

import com.sun.jna.platform.win32.WinDef;


@FunctionalInterface
public interface Win32 {

    void filterHandle(WinDef.HWND hwnd,String title);
}
