package com.damon.disruptor;

import com.lmax.disruptor.EventFactory;

class RpcEventFactory implements EventFactory<RpcEvent> {
    @Override
    public RpcEvent newInstance() {
        return new RpcEvent();
    }
}
