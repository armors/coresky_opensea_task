package com.coresky.web.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StringTools {

    public static SimpleDateFormat sdfYmdHis = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static SimpleDateFormat sdfYmd = new SimpleDateFormat("yyyy-MM-dd");

    public static SimpleDateFormat sdfHis = new SimpleDateFormat("HH:mm:ss");

    public static String concat(String str, String str2) {
        return str.toLowerCase() + "+" + str2;
    }

    public static String toDateYmd(long time) {
        return sdfYmd.format(time*1000L);
    }

    public static String toDateYmdHis(long time) {
        return sdfYmdHis.format(time*1000L);
    }

    public static String toDateHis(long time) {
        return sdfHis.format(time*1000L);
    }

    public static String toFixed(BigDecimal bigDecimal) {
        return bigDecimal.setScale(2, RoundingMode.HALF_UP).toString();
    }
    public static String toFixed(Float value) {
        return new BigDecimal(value).setScale(0, RoundingMode.HALF_UP).toString();
    }

    public static boolean isGtZero(String value) {
        if(null == value || StringUtils.isEmpty(value) || value.equals("0")) {
            return false;
        }
        return true;
    }

    public static String minValue(String v1, String v2, String v3) {
        List<BigDecimal> list = new ArrayList<>();
        if(isGtZero(v1)) {
            list.add(new BigDecimal(v1));
        }
        if(isGtZero(v2)) {
            list.add(new BigDecimal(v2));
        }
        if(isGtZero(v3)) {
            list.add(new BigDecimal(v3));
        }
        System.out.println(list);
        if (list.size() == 0) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0).toString();
        }
        list.sort((o1, o2) -> {
            return o1.compareTo(o2);
        });
        System.out.println(list);
        return list.get(0).toString();
    }

    public static String maxValue(String v1, String v2, String v3) {
        List<BigDecimal> list = new ArrayList<>();
        if(isGtZero(v1)) {
            list.add(new BigDecimal(v1));
        }
        if(isGtZero(v2)) {
            list.add(new BigDecimal(v2));
        }
        if(isGtZero(v3)) {
            list.add(new BigDecimal(v3));
        }
        System.out.println(list);
        if (list.size() == 0) {
            return null;
        }
        if (list.size() == 1) {
            return list.get(0).toString();
        }
        list.sort((o1, o2) -> {
            return 0 - o1.compareTo(o2);
        });
        System.out.println(list);
        return list.get(0).toString();
    }
}
