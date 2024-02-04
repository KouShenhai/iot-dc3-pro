/*
 * Copyright 2016-present the IoT DC3 original author or authors.
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

package io.github.pnoker.center.data.repository.impl;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import io.github.pnoker.center.data.entity.bo.PointValueBO;
import io.github.pnoker.center.data.entity.point.TsPointValue;
import io.github.pnoker.center.data.repository.RepositoryService;
import io.github.pnoker.center.data.strategy.RepositoryStrategyFactory;
import io.github.pnoker.common.constant.driver.StorageConstant;
import io.github.pnoker.common.constant.driver.StrategyConstant;
import io.github.pnoker.common.utils.JsonUtil;
import io.github.pnoker.common.utils.LocalDateTimeUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.http.entity.ContentType;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author pnoker
 * @since 2022.1.0
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "data.point.sava.opentsdb.enable", havingValue = "true")
public class OpentsdbServiceImpl implements RepositoryService, InitializingBean {

    @Value("${data.point.sava.opentsdb.host}")
    private String host;
    @Value("${data.point.sava.opentsdb.port}")
    private Integer port;

    @Resource
    private OkHttpClient okHttpClient;

    @Override
    public String getRepositoryName() {
        return StrategyConstant.Storage.STRATEGY_OPENTSDB;
    }

    @Override
    public void savePointValue(PointValueBO pointValueBO) {
        if (!ObjectUtil.isAllNotEmpty(pointValueBO.getDeviceId(), pointValueBO.getPointId())) {
            return;
        }

        savePointValues(pointValueBO.getDeviceId(), Collections.singletonList(pointValueBO));
    }

    @Override
    public void savePointValues(Long deviceId, List<PointValueBO> pointValueBOS) {
        if (ObjectUtil.isEmpty(deviceId)) {
            return;
        }

        List<TsPointValue> tsPointValues = pointValueBOS.stream()
                .filter(pointValue -> ObjectUtil.isNotEmpty(pointValue.getPointId()))
                .map(pointValue -> convertPointValues(StorageConstant.POINT_VALUE_PREFIX + deviceId, pointValue))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        List<List<TsPointValue>> partition = Lists.partition(tsPointValues, 100);
        partition.forEach(this::putPointValues);
    }

    @Override
    public void afterPropertiesSet() {
        RepositoryStrategyFactory.put(StrategyConstant.Storage.STRATEGY_OPENTSDB, this);
    }

    private List<TsPointValue> convertPointValues(String metric, PointValueBO pointValueBO) {
        Long point = pointValueBO.getPointId();
        String value = pointValueBO.getValue();
        long timestamp = LocalDateTimeUtil.milliSeconds(pointValueBO.getOriginTime());

        List<TsPointValue> tsPointValues = new ArrayList<>(2);

        TsPointValue tsValue = new TsPointValue(metric, value);
        tsValue.setTimestamp(timestamp);
        tsValue.addTag("point", point.toString()).addTag("valueType", "value");
        tsPointValues.add(tsValue);

        TsPointValue tsRawValue = new TsPointValue(metric, value);
        tsRawValue.setTimestamp(timestamp);
        tsValue.addTag("point", point.toString()).addTag("valueType", "rawValue");
        tsPointValues.add(tsRawValue);

        return tsPointValues;
    }

    private void putPointValues(List<TsPointValue> tsPointValues) {
        String putUrl = String.format("http://%s:%s/api/put?details", host, port);
        RequestBody requestBody = RequestBody.create(JsonUtil.toJsonString(tsPointValues), MediaType.parse(ContentType.APPLICATION_JSON.toString()));
        Request request = new Request.Builder()
                .url(putUrl)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                log.error("Send pointValues to opentsdb error: {}", e.getMessage(), e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (ObjectUtil.isNotNull(response.body())) {
                    log.debug("Send pointValues to opentsdb, Response: {}", response.message());
                }
            }
        });
    }
}