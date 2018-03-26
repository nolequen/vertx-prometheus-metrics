package io.vertx.ext.prometheus.metrics;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import io.netty.channel.MultithreadEventLoopGroup;
import io.prometheus.client.CollectorRegistry;
import io.vertx.core.net.impl.transport.Transport;
import org.jetbrains.annotations.NotNull;

/**
 * @author marcus
 * @since 1.0.0
 */
public class EventLoopGroupMetrics extends AbstractCollectedMetrics {

    public static EventLoopGroupMetrics createAndRegister(final @NotNull CollectorRegistry registry) {
        final EventLoopGroupMetrics metrics = new EventLoopGroupMetrics();
        registry.register(metrics);
        return metrics;
    }

    public void register(final Class<? extends Transport> transport, final String name, final MultithreadEventLoopGroup eventLoopGroup) {
        register("vertx_" + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,transport.getSimpleName() + "_" + name.replace("-", "_") + "_executor_count"),
                "Event loop executor count, number of executors in the event loop group",
                () -> (double)eventLoopGroup.executorCount(),
                ImmutableList.of("instance"),
                ImmutableList.of((() -> eventLoopGroup.toString())));
    }
}
