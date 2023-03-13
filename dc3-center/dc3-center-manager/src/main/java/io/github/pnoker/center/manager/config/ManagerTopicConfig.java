/*
 * Copyright 2016-present Pnoker All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pnoker.center.manager.config;

import io.github.pnoker.common.config.TopicConfig;
import io.github.pnoker.common.constant.common.SymbolConstant;
import io.github.pnoker.common.constant.driver.RabbitConstant;
import io.github.pnoker.common.utils.EnvironmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author pnoker
 * @since 2022.1.0
 */
@Slf4j
@Configuration
@ConditionalOnClass(TopicConfig.class)
public class ManagerTopicConfig {
    @Value("${spring.env:}")
    private String env;

    @Resource
    private TopicExchange eventExchange;

    @Bean
    Queue driverEventQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // 15秒：15 * 1000 = 15000L
        arguments.put(RabbitConstant.MESSAGE_TTL, 15000L);
        return new Queue(RabbitConstant.QUEUE_DRIVER_EVENT, true, false, EnvironmentUtil.isDev(env), arguments);
    }

    @Bean
    Queue deviceEventQueue() {
        Map<String, Object> arguments = new HashMap<>();
        // 15秒：15 * 1000 = 15000L
        arguments.put(RabbitConstant.MESSAGE_TTL, 15000L);
        return new Queue(RabbitConstant.QUEUE_DEVICE_EVENT, true, false, EnvironmentUtil.isDev(env), arguments);
    }

    @Bean
    Binding driverEventBinding(Queue driverEventQueue) {
        return BindingBuilder
                .bind(driverEventQueue)
                .to(eventExchange)
                .with(RabbitConstant.ROUTING_DRIVER_EVENT_PREFIX + SymbolConstant.ASTERISK);
    }

    @Bean
    Binding deviceEventBinding(TopicExchange eventExchange, Queue deviceEventQueue) {
        return BindingBuilder
                .bind(deviceEventQueue)
                .to(eventExchange)
                .with(RabbitConstant.ROUTING_DEVICE_EVENT_PREFIX + SymbolConstant.ASTERISK);
    }

}