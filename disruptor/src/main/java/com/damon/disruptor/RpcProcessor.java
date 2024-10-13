package com.damon.disruptor;

import com.lmax.disruptor.EventHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

class RpcProcessor implements EventHandler<RpcEvent> {
    private final Handler handler;

    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    public RpcProcessor(Handler handler) {
        this.handler = handler;
    }

    public void pause() {
        isRunning.compareAndSet(true, false);
    }

    public boolean run() {
        return isRunning.compareAndSet(false, true);
    }

    @Override
    public void onEvent(RpcEvent event, long sequence, boolean endOfBatch) {
        CompletableFuture future = handler.invoke(event.getRequest(), event.getFunction());
        future.thenApply(result-> event.getResponseFuture().complete(result));
    }

}
