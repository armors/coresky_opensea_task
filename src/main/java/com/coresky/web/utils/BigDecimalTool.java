package com.coresky.web.utils;

import com.coresky.web.model.PriceModel;

import java.math.BigDecimal;

public class BigDecimalTool {

    public static BigDecimal BigDecimal(String value) {
        try {
            return new BigDecimal(value);
        } catch (Throwable e) {
            return BigDecimal.ZERO;
        }
    }

    public static PriceModel minValue(PriceModel v1, PriceModel v2) {
        try {
            if(v1.getBasePrice().compareTo(BigDecimal.ZERO) == 0) {
                return v2;
            }
            if(v2.getBasePrice().compareTo(BigDecimal.ZERO) == 0) {
                return v1;
            }
            if(v1.getBasePrice().compareTo(v2.getBasePrice()) < 0) {
                return v1;
            } else {
                return v2;
            }
        } catch (Throwable e) {
            return PriceModel.nullModel();
        }
    }

    public static BigDecimal maxValue(BigDecimal b1, BigDecimal b2) {
        try {
            if(b1.compareTo(b2) > 0) {
                return b1;
            } else {
                return b2;
            }
        } catch (Throwable e) {
            return BigDecimal.ZERO;
        }
    }
}
