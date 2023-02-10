package com.coresky.web.model;

import com.coresky.web.utils.BigDecimalTool;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceModel {

    private BigDecimal basePrice;

    private BigDecimal listingTime;

    private BigDecimal expirationTime;

    public static PriceModel initModel(String br, String lt, String et) {
        try {
            PriceModel priceModel = new PriceModel();
            priceModel.setBasePrice(BigDecimalTool.BigDecimal(br));
            priceModel.setListingTime(BigDecimalTool.BigDecimal(lt));
            priceModel.setExpirationTime(BigDecimalTool.BigDecimal(et));
            return priceModel;
        } catch (Throwable e) {
            return nullModel();
        }
    }

    public static PriceModel nullModel() {
        PriceModel priceModel = new PriceModel();
        priceModel.setBasePrice(BigDecimal.ZERO);
        priceModel.setListingTime(BigDecimal.ZERO);
        priceModel.setExpirationTime(BigDecimal.ZERO);
        return priceModel;
    }
}
