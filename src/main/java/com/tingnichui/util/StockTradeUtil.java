package com.tingnichui.util;

import com.tingnichui.common.KeyCodeEnum;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;

import java.util.*;
import java.util.List;


/**
 * @author  Geng Hui
 * @date  2022/8/25 9:34
 */
public class StockTradeUtil {

    static final User32 USER = User32.INSTANCE;

    private StockTradeUtil() {
    }

    public static void main(String[] args) throws Exception {
        /** StockUtilTest */
//        System.err.println(buy(Arrays.asList("601288","3.123456","300")));
//        System.err.println(sell(Arrays.asList("601288","3.21","300")));
//        System.err.println(cancel());
        System.err.println(getBalance());
//        System.err.println(getBalanceDetail());
        /** RobotsUtilTest */
//        WinDef.HWND primaryHandle = Win32Util.getPrimaryHandle("WeChatMainWndForPC", "微信");
//        USER.SetForegroundWindow(primaryHandle);
//        RobotUtil.sendDel(10);//删除
//        RobotUtil.sendKey("0123456798");//按键
//        WinDef.HWND primaryHandle = Win32Util.getPrimaryHandle(null, "网上股票交易系统5.0");
//        Win32Util.moveWindow(primaryHandle,100,100);
//        Thread.sleep(1000);
//        RobotUtil.screenShot(new Rectangle(100,100,1200,600));

    }

    /**
     * @param buyList [证券代码，买入价格，买入数量]
     * @return
     * @throws Exception
     */
    public static String buy(List<String> buyList) throws Exception {
        if (buyList.size() != 3 || buyList.get(0).length() != 6) return "参数异常";
        //获取句柄
        WinDef.HWND primaryHandle = Win32Util.getPrimaryHandle(null, "网上股票交易系统5.0");
        Thread.sleep(1000);
        User32.INSTANCE.PostMessage(primaryHandle, WinUser.WM_KEYDOWN, new WinDef.WPARAM(KeyCodeEnum.F1.getKeyCode()), null);
        List<WinDef.HWND> handleList = new ArrayList<>();
        Win32Util.getChildHandle(primaryHandle, "Edit", "", (hwmd, title) -> {
            handleList.add(hwmd);
        });
        if (handleList.size() != 3) return "句柄异常";
        //填写
        for (int i = 0; i < buyList.size(); i++) {
            Thread.sleep(1000);
            USER.SetForegroundWindow(handleList.get(i));
            RobotUtil.sendDel(10);
            RobotUtil.sendDel(3);
            RobotUtil.sendKey(buyList.get(i));
        }
        return "success";
    }

    /**
     * @param sellList [证券代码，买入价格，买入数量]
     * @return
     * @throws Exception
     */
    public static String sell(List<String> sellList) throws Exception {
        if (sellList.size() != 3 || sellList.get(0).length() != 6) return "参数异常";
        //获取句柄
        WinDef.HWND primaryHandle = Win32Util.getPrimaryHandle(null, "网上股票交易系统5.0");
        Thread.sleep(1000);
        User32.INSTANCE.PostMessage(primaryHandle, WinUser.WM_KEYDOWN, new WinDef.WPARAM(KeyCodeEnum.F2.getKeyCode()), null);
        List<WinDef.HWND> handleList = new ArrayList<>();
        Win32Util.getChildHandle(primaryHandle, "Edit", "", (hwmd, title) -> {
            handleList.add(hwmd);
        });
        if (handleList.size() != 3) return "句柄异常";
        //填写
        for (int i = 0; i < sellList.size(); i++) {
            Thread.sleep(1000);
            USER.SetForegroundWindow(handleList.get(i));
            RobotUtil.sendDel(10);
            RobotUtil.sendDel(3);
            RobotUtil.sendKey(sellList.get(i));
        }
        return "";

    }

    public static String cancel() {
        WinDef.HWND primaryHandle = Win32Util.getPrimaryHandle(null, "网上股票交易系统5.0");
        User32.INSTANCE.PostMessage(primaryHandle, WinUser.WM_KEYDOWN, new WinDef.WPARAM(KeyCodeEnum.F3.getKeyCode()), null);
        return "";
    }


    /**
     * 获取账户资金详情
     */
    public static Map<String, String> getBalance() throws Exception {

        WinDef.HWND primaryHandle = Win32Util.getPrimaryHandle(null, "网上股票交易系统5.0");
        Thread.sleep(1000);
        User32.INSTANCE.PostMessage(primaryHandle, WinUser.WM_KEYDOWN, new WinDef.WPARAM(KeyCodeEnum.F4.getKeyCode()), null);
        Thread.sleep(2 * 1000);
        List<String> handleList = new ArrayList<>();
        List<String> temp = Arrays.asList("资金余额", "冻结金额", "可用金额", "可取金额", "参考总资产", "港股通可用资金");
        Win32Util.getChildHandle(primaryHandle, "Static", "", (hwmd, title) -> {
            //获取窗口矩形
            WinDef.RECT winRect = new WinDef.RECT();
            USER.GetClientRect(hwmd, winRect);
            if ("96".equals(String.valueOf(winRect.right))) {
                handleList.add(title);
            }
        });
        if (handleList.size() != 6) return new HashMap<String,String>(){{put("msg","句柄异常");}};
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < handleList.size(); i++) {
            map.put(temp.get(i), handleList.get(i));
        }
        return map;
    }


    public static Map<String, String> getBalanceDetail() throws Exception {

        WinDef.HWND primaryHandle = Win32Util.getPrimaryHandle(null, "网上股票交易系统5.0");
        Thread.sleep(1000);
        USER.PostMessage(primaryHandle, WinUser.WM_KEYDOWN, new WinDef.WPARAM(KeyCodeEnum.F4.getKeyCode()), null);
        Thread.sleep(1000);
        RobotUtil.sendCtrlWith('s');
        Thread.sleep(1000);
        Win32Util.getChildHandle(Win32Util.getPrimaryHandle(null, "另存为"), "Button", "保存(&S)", ((hwnd, title) -> {
            USER.SetForegroundWindow(hwnd);
        }));
        RobotUtil.sendKey('s');
        Thread.sleep(1000);
        RobotUtil.sendKey('y');
        return null;
    }

}
