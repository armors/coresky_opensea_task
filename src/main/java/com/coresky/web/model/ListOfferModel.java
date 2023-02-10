package com.coresky.web.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ListOfferModel {

    private List<OrderInfo> orders;

    @Data
    @NoArgsConstructor
    public static class OrderInfo {

        private String current_price;

        private String listing_time;

        private String expiration_time;
    }
}
