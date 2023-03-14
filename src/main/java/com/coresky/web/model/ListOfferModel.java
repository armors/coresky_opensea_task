package com.coresky.web.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ListOfferModel {

    private List<OrderInfo> orders;

    @Data
    public static class OrderInfo {

        private String current_price;

        private String listing_time;

        private String expiration_time;

        private ProtocolDataModel protocol_data;
    }

    @Data
    public static class ProtocolDataModel {

        private ParametersModel parameters;
    }

    @Data
    public static class ParametersModel {

        private List<ConsiderationModel> consideration;
    }

    @Data
    public static class ConsiderationModel {

        private String token;
    }
}
