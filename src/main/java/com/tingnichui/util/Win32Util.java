package com.tingnichui.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.ptr.IntByReference;
import org.apache.commons.lang3.StringUtils;

public class Win32Util {

    static final User32 USER = User32.INSTANCE;

    private Win32Util() {
    }

    public static WinDef.HWND getPrimaryHandle(String className, String title){
        WinDef.HWND primaryHandle = USER.FindWindow(className, title);
        // 窗口显示
        USER.ShowWindow(primaryHandle, 9);
        // 窗口放到最前端
        USER.SetForegroundWindow(primaryHandle);
        return primaryHandle;
    }

    public static void moveWindow(WinDef.HWND hwnd,int x,int y){
        //获取窗口大小
        WinDef.RECT htwin_rect = new WinDef.RECT();
        USER.GetWindowRect(hwnd, htwin_rect);
        int htwin_width = htwin_rect.right - htwin_rect.left;
        int htwin_height = htwin_rect.bottom - htwin_rect.top;
        //移动窗口
        USER.MoveWindow(hwnd, x, y, htwin_width, htwin_height, true);
    }


    public static void getChildHandle(WinDef.HWND parent, String className, String title,Win32 win32){
        //获取进程id
        IntByReference intByReference = new IntByReference();
        USER.GetWindowThreadProcessId(parent, intByReference);
        int threadProcessId = intByReference.getValue();
        //遍历子窗口句柄
        USER.EnumChildWindows(parent,(WinDef.HWND hwnd, Pointer pointer) -> {
            //获取窗口类命
            char[] winClass = new char[512];
            USER.GetClassName(hwnd, winClass, 512);
            //获取窗口标题
            char[] winCaption=new char[512];
            USER.GetWindowText(hwnd, winCaption, 512);
            String winTitle = Native.toString(winCaption);
            if (USER.IsWindowVisible(hwnd) && (StringUtils.isBlank(className) || className.equals(Native.toString(winClass))) && (StringUtils.isBlank(title) || title.equals(winTitle))) {
                //
                WinUser.GUITHREADINFO guithreadinfo = new WinUser.GUITHREADINFO();
                USER.GetGUIThreadInfo(threadProcessId, guithreadinfo);
                //
                WinUser.WINDOWINFO windowinfo = new WinUser.WINDOWINFO();
                USER.GetWindowInfo(hwnd, windowinfo);
                win32.filterHandle(hwnd,winTitle);
            }else {
            }
            return true;
        },Pointer.NULL);
    }

    public static void sendMsg(String str) {
        WinUser.INPUT input = new WinUser.INPUT();
        for(Character ch : str.toCharArray()) {
            input.type = new WinDef.DWORD(WinUser.INPUT.INPUT_KEYBOARD);
            input.input.setType("ki");
            input.input.ki.wScan = new WinDef.WORD(0);
            input.input.ki.time = new WinDef.DWORD(0);
            input.input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);
            // Press
            input.input.ki.wVk = new WinDef.WORD(Character.toUpperCase(ch)); // 0x41
            input.input.ki.dwFlags = new WinDef.DWORD(0); // keydown
            User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
            // Release
            input.input.ki.wVk = new WinDef.WORD(Character.toUpperCase(ch)); // 0x41
            input.input.ki.dwFlags = new WinDef.DWORD(2); // keyup
            User32.INSTANCE.SendInput(new WinDef.DWORD(1), (WinUser.INPUT[]) input.toArray(1), input.size());
        }
    }

}
