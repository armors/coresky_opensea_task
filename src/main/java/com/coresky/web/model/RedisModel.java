package com.coresky.web.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RedisModel {

    private String source;

    private String contract;

    private String tokenId;

    private String event;
}
