package com.xue.qin.buildsrc.log;

import com.xue.qin.buildsrc.QinPlugin;

public class LogUtil {
    public static void i(String tag, String message) {
        QinPlugin.logger.quiet("[" + tag + "]" + " " + message);
    }
}
