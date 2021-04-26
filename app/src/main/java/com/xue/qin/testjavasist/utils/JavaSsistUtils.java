package com.xue.qin.testjavasist.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class JavaSsistUtils {
    private static class SingleHolder {
        private static final JavaSsistUtils instance = new JavaSsistUtils();
    }

    public static JavaSsistUtils getInstance() {
        return SingleHolder.instance;
    }

    public String getCurTime() {
        long cur = System.currentTimeMillis();
        Date nowTime = new Date(cur);
        SimpleDateFormat time = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return time.format(nowTime) + " stamp: " + cur;
    }
}
