package com.tingnichui.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Field;

/**
 * @author  Geng Hui
 * @date  2022/8/25 9:34
 */
public class RobotUtil {

    static Robot robot;

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }


    private RobotUtil() {
    }

    public static void copy(String text) {
        Clipboard sysc = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable tText = new StringSelection(text);
        sysc.setContents(tText, null);
        System.out.println("已复制");
    }

    public static void sendCtrlWith(char c) {
        robot.keyPress(KeyEvent.VK_CONTROL);
        sendKey(c);
        robot.keyRelease(KeyEvent.VK_CONTROL);
    }

    public static void sendDel(int times) {
        do {
            robot.keyPress(KeyEvent.VK_BACK_SPACE);
            robot.keyRelease(KeyEvent.VK_BACK_SPACE);
        } while (--times > 0);
    }

    public static void sendKey(char c) {
        try {
            String KeyName = (c + "").toUpperCase();
            Field f = KeyEvent.class.getDeclaredField("VK_" + KeyName);
            Object o = f.get(null);
            int keyCode = Integer.parseInt(o + "");
            robot.keyPress(keyCode);
            robot.keyRelease(keyCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendKey(String message) {
        try {
            for (Character c : message.toCharArray()) {
                if ('.' == c) {
                    robot.keyPress(KeyEvent.VK_PERIOD);
                    robot.keyRelease(KeyEvent.VK_PERIOD);
                    continue;
                }
                Field field = KeyEvent.class.getDeclaredField("VK_" + c.toString().toUpperCase());
                Object o = field.get(null);
                int keyCode = Integer.parseInt(o.toString());
                robot.keyPress(keyCode);
                robot.keyRelease(keyCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void screenShot(Rectangle rectangle) throws Exception {
        //获取屏幕分辨率
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        //创建该分辨率的矩形对象
        rectangle.setSize(d);
//        Rectangle screenRect = new Rectangle(d);
        //根据这个矩形截图
//        screenRect.setRect(110,110,1200,600);
        BufferedImage bufferedImage = robot.createScreenCapture(rectangle);
        //保存截图
        File file = new File("C:\\Users\\abc\\Desktop\\pic\\" + Math.random() + ".png");
        if (!file.exists()) file.createNewFile();
        ImageIO.write(bufferedImage, "png", file);
    }

}
