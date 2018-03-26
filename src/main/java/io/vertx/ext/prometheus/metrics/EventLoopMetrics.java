package io.vertx.ext.prometheus.metrics;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableList;
import io.netty.channel.MultithreadEventLoopGroup;
import io.netty.channel.SingleThreadEventLoop;
import io.prometheus.client.CollectorRegistry;
import io.vertx.core.net.impl.transport.Transport;
import org.jetbrains.annotations.NotNull;

/**
 * @author marcus
 * @since 1.0.0
 */
public class EventLoopMetrics extends AbstractCollectedMetrics {

    public static EventLoopMetrics createAndRegister(final @NotNull CollectorRegistry registry) {
        final EventLoopMetrics metrics = new EventLoopMetrics();
        registry.register(metrics);
        return metrics;
    }

    public void register(final Class<? extends Transport> transport, final String name, final MultithreadEventLoopGroup eventLoopGroup, final SingleThreadEventLoop eventLoop) {
        register("vertx_" + CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE,transport.getSimpleName() + "_" + name.replace("-", "_") + "_pending_tasks"),
                "Number of pending tasks for a specific event loop executor",
                () -> (double)eventLoop.pendingTasks(),
                ImmutableList.of("instance"),
                ImmutableList.of(() -> eventLoop.toString()));
    }
}
