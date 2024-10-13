package com.damon.disruptor;

import cn.hutool.core.thread.NamedThreadFactory;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class DisruptorRpc {

    private final Disruptor<RpcEvent> disruptor;
    private final RingBuffer<RpcEvent> ringBuffer;

    public DisruptorRpc(Handler handler) {
        RpcEventFactory factory = new RpcEventFactory();
        NamedThreadFactory namedThreadFactory = new NamedThreadFactory("disruptor-thread-pool-", false);
        disruptor = new Disruptor<>(factory, 1024 * 1024, namedThreadFactory,
                ProducerType.SINGLE, new com.lmax.disruptor.BlockingWaitStrategy()
        );
        // 创建 Disruptor
        disruptor.handleEventsWith(new RpcProcessor(handler));
        disruptor.start();
        // 获取 RingBuffer
        ringBuffer = disruptor.getRingBuffer();
    }

    public static void main(String[] args) throws Exception {
        DisruptorRpc disruptorRpc = new DisruptorRpc(new Handler() {
            @Override
            public Object invoke(Command command, Function function) {
                return 1;
            }
        });
        List<CompletableFuture> list = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        // 模拟多个 RPC 调用
        for (int i = 0; i < 50000000; i++) {
            GoodsCreateCmd cmd = new GoodsCreateCmd(1L, 1L);
            CompletableFuture<Object> responseFuture = disruptorRpc.call(cmd, new Function() {
                @Override
                public Object apply(Object object) {
                    return 1;
                }
            });
            responseFuture.thenAccept(response -> {
              //  System.out.println("Response: " + response);
            });
            list.add(responseFuture);
        }
        CompletableFuture.allOf(list.toArray(new CompletableFuture[0])).join();
        System.out.println("Total time: " + (System.currentTimeMillis() - startTime));
        // 关闭 Disruptor
        disruptorRpc.disruptor.shutdown();
    }

    public <R> CompletableFuture<R> call(Command request, Function function) {
        // 创建 CompletableFuture
        CompletableFuture<R> future = new CompletableFuture<>();
        // 获取下一个可用的序列号
        long sequence = ringBuffer.next();
        try {
            // 获取该序列号对应的事件
            RpcEvent event = ringBuffer.get(sequence);
            // 设置请求内容
            event.setRequest(request);
            event.setFunction(function);
            // 设置响应的 CompletableFuture
            event.setResponseFuture(future);
        } finally {
            // 发布事件
            ringBuffer.publish(sequence);
        }
        return future;
    }
}