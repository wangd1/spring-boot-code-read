/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.metrics.web.reactive.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import org.springframework.boot.actuate.metrics.AutoTimer;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

/**
 * {@link ExchangeFilterFunction} applied through a {@link MetricsWebClientCustomizer} to
 * record metrics.
 *
 * @author Brian Clozel
 * @author Tadaya Tsuyukubo
 * @author Scott Frederick
 * @since 2.1.0
 */
public class MetricsWebClientFilterFunction implements ExchangeFilterFunction {

	private static final String METRICS_WEBCLIENT_START_TIME = MetricsWebClientFilterFunction.class.getName()
			+ ".START_TIME";

	private static final Log logger = LogFactory.getLog(MetricsWebClientFilterFunction.class);

	private final MeterRegistry meterRegistry;

	private final WebClientExchangeTagsProvider tagProvider;

	private final String metricName;

	private final AutoTimer autoTimer;

	/**
	 * Create a new {@code MetricsWebClientFilterFunction}.
	 * @param meterRegistry the registry to which metrics are recorded
	 * @param tagProvider provider for metrics tags
	 * @param metricName name of the metric to record
	 * @param autoTimer the auto-timer configuration or {@code null} to disable
	 * @since 2.2.0
	 */
	public MetricsWebClientFilterFunction(MeterRegistry meterRegistry, WebClientExchangeTagsProvider tagProvider,
			String metricName, AutoTimer autoTimer) {
		this.meterRegistry = meterRegistry;
		this.tagProvider = tagProvider;
		this.metricName = metricName;
		this.autoTimer = (autoTimer != null) ? autoTimer : AutoTimer.DISABLED;
	}

	@Override
	public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
		if (!this.autoTimer.isEnabled()) {
			return next.exchange(request);
		}
		return next.exchange(request)
			.as((responseMono) -> instrumentResponse(request, responseMono))
			.contextWrite(this::putStartTime);
	}

	private Mono<ClientResponse> instrumentResponse(ClientRequest request, Mono<ClientResponse> responseMono) {
		final AtomicBoolean responseReceived = new AtomicBoolean();
		return Mono.deferContextual((ctx) -> responseMono.doOnEach((signal) -> {
			if (signal.isOnNext() || signal.isOnError()) {
				responseReceived.set(true);
				recordTimer(request, signal.get(), signal.getThrowable(), getStartTime(ctx));
			}
		}).doFinally((signalType) -> {
			if (!responseReceived.get() && SignalType.CANCEL.equals(signalType)) {
				recordTimer(request, null, null, getStartTime(ctx));
			}
		}));
	}

	private void recordTimer(ClientRequest request, ClientResponse response, Throwable error, Long startTime) {
		try {
			Iterable<Tag> tags = this.tagProvider.tags(request, response, error);
			this.autoTimer.builder(this.metricName)
				.tags(tags)
				.description("Timer of WebClient operation")
				.register(this.meterRegistry)
				.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
		}
		catch (Exception ex) {
			logger.warn("Failed to record timer metrics", ex);
			// Allow request-response exchange to continue, unaffected by metrics problem
		}
	}

	private Long getStartTime(ContextView context) {
		return context.get(METRICS_WEBCLIENT_START_TIME);
	}

	private Context putStartTime(Context context) {
		return context.put(METRICS_WEBCLIENT_START_TIME, System.nanoTime());
	}

}
