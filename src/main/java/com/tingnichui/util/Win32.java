package com.tingnichui.util;

import com.sun.jna.platform.win32.WinDef;

/**
 * @author  Geng Hui
 * @date  2022/8/25 9:34
 */
@FunctionalInterface
public interface Win32 {

    void filterHandle(WinDef.HWND hwnd,String title);
}
