package io.vertx.ext.prometheus.metrics;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.prometheus.client.Collector;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author marcus
 * @since 1.0.0
 */
public class AbstractCollectedMetrics extends Collector {

    private final Multimap<CollectedMetric, CollectedMetricValue> metrics = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    @Override
    public List<MetricFamilySamples> collect() {
        return metrics.asMap().entrySet().stream().map(this::convertMetricFamilySamples).collect(ImmutableList.toImmutableList());
    }

    private MetricFamilySamples convertMetricFamilySamples(final Map.Entry<CollectedMetric, Collection<CollectedMetricValue>> metric) {
        final CollectedMetric collectedMetric = metric.getKey();

        final String metricName = collectedMetric.getMetricName();
        final List<MetricFamilySamples.Sample> samples = metric.getValue().stream().map(entry -> toSample(metricName, entry)).collect(ImmutableList.toImmutableList());

        return new MetricFamilySamples(metricName, Type.GAUGE, collectedMetric.getHelp(), samples);
    }

    private MetricFamilySamples.Sample toSample(final String metricName, final CollectedMetricValue value) {
        return new MetricFamilySamples.Sample(metricName,
                value.getLabelNames(), value.getLabelValues(),
                value.getMetricValue().get());
    }

    protected void register(final String metricName, final String help, final Supplier<Double> metricValue, final List<String> labelNames, final List<Supplier<String>> labelValues) {
        metrics.put(new CollectedMetric(metricName, help), new CollectedMetricValue(metricValue, ImmutableList.copyOf(labelNames), ImmutableList.copyOf(labelValues)));
    }

    private static class CollectedMetric {
        private final String metricName;
        private final String help;

        public CollectedMetric(final String metricName, final String help) {
            this.metricName = metricName;
            this.help = help;
        }

        public String getMetricName() {
            return metricName;
        }

        public String getHelp() {
            return help;
        }

        @Override
        public int hashCode() {
            return Objects.hash(metricName, help);
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            final CollectedMetric other = (CollectedMetric) obj;
            return Objects.equals(this.metricName, other.metricName)
                    && Objects.equals(this.help, other.help);
        }
    }

    private static class CollectedMetricValue {

        private final Supplier<Double> metricValue;
        private final List<String> labelNames;
        private final List<Supplier<String>> labelValueSuppliers;

        private CollectedMetricValue(final Supplier<Double> value,
                                     final List<String> labelNames,
                                     final List<Supplier<String>> labelValueSuppliers) {
            this.metricValue = value;
            this.labelNames = ImmutableList.copyOf(labelNames);
            this.labelValueSuppliers = ImmutableList.copyOf(labelValueSuppliers);

            checkArgument(labelNames.size() == labelValueSuppliers.size(), "Label names must be the same size as provided value suppliers");
        }

        public Supplier<Double> getMetricValue() {
            return metricValue;
        }

        public List<String> getLabelNames() {
            return labelNames;
        }

        public List<String> getLabelValues() {
            return labelValueSuppliers.stream().map(Supplier::get).collect(ImmutableList.toImmutableList());
        }
    }


}
