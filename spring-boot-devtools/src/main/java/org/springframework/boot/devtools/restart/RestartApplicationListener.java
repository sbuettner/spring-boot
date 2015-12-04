/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.devtools.restart;

import java.util.List;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * {@link ApplicationListener} to initialize the {@link Restarter}.
 *
 * @author Phillip Webb
 * @since 1.3.0
 * @see Restarter
 */
public class RestartApplicationListener
		implements ApplicationListener<ApplicationEvent>, Ordered {

	private int order = HIGHEST_PRECEDENCE;

	private static final String ENABLED_PROPERTY = "spring.devtools.restart.enabled";

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartedEvent) {
			onApplicationStartedEvent((ApplicationStartedEvent) event);
		}
		if (event instanceof ApplicationReadyEvent
				|| event instanceof ApplicationFailedEvent) {
			Restarter.getInstance().finish();
		}
	}

	private void onApplicationStartedEvent(ApplicationStartedEvent event) {
		// It's too early to use the Spring environment but we should still allow
		// users to disable restart using a System property.
		String enabled = System.getProperty(ENABLED_PROPERTY);
		if (enabled == null || Boolean.parseBoolean(enabled)) {
			String[] args = event.getArgs();
			DefaultRestartInitializer initializer = new DefaultRestartInitializer();
			boolean restartOnInitialize = !AgentReloader.isActive();
			List<RestartListener> restartListeners = SpringFactoriesLoader
					.loadFactories(RestartListener.class, getClass().getClassLoader());
			Restarter.initialize(args, false, initializer, restartOnInitialize,
					restartListeners
							.toArray(new RestartListener[restartListeners.size()]));
		}
		else {
			Restarter.disable();
		}
	}

	@Override
	public int getOrder() {
		return this.order;
	}

	/**
	 * Set the order of the listener.
	 * @param order the order of the listener
	 */
	public void setOrder(int order) {
		this.order = order;
	}

}
