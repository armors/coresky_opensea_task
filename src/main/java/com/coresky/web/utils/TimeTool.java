package com.coresky.web.utils;

import java.text.SimpleDateFormat;
import java.util.logging.SimpleFormatter;

public class TimeTool {

    public static long timestamp() {
        return System.currentTimeMillis() / 1000;
    }

    public static String datetime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(System.currentTimeMillis());
    }

    public static String dateDir() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MMdd/");
        return sdf.format(System.currentTimeMillis());
    }
}
