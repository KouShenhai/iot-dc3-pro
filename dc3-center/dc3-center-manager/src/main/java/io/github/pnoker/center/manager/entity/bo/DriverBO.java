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

package io.github.pnoker.center.manager.entity.bo;

import io.github.pnoker.common.base.BaseBO;
import io.github.pnoker.common.enums.DriverTypeFlagEnum;
import io.github.pnoker.common.enums.EnableFlagEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Driver BO
 *
 * @author pnoker
 * @since 2022.1.0
 */
@Data
@SuperBuilder
@RequiredArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DriverBO extends BaseBO {

    /**
     * 驱动名称
     */
    private String driverName;

    /**
     * 驱动编号
     */
    private String driverCode;

    /**
     * 驱动服务名称
     */
    private String serviceName;

    /**
     * 驱动类型标识
     */
    private DriverTypeFlagEnum driverTypeFlag;

    /**
     * 服务主机
     */
    private String serviceHost;

    /**
     * 使能标识
     */
    private EnableFlagEnum enableFlag;

    /**
     * 租户ID
     */
    private Long tenantId;
}