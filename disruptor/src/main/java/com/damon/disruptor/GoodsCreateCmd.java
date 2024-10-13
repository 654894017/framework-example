package com.damon.disruptor;


public class GoodsCreateCmd extends Command {
    private Long goodsId;

    public GoodsCreateCmd(Long commandId, Long aggregateId) {
        this.goodsId = aggregateId;
    }
}