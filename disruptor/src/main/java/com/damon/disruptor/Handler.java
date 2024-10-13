package com.damon.disruptor;


import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface Handler {
    CompletableFuture invoke(final Command command, final Function function);
}
