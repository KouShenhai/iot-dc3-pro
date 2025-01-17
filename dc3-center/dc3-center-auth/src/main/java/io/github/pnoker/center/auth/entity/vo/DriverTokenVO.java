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

package io.github.pnoker.center.auth.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.pnoker.common.entity.base.BaseVO;
import io.github.pnoker.common.enums.EnableFlagEnum;
import io.github.pnoker.common.enums.ExpireFlagEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DriverToken VO
 *
 * @author pnoker
 * @since 2022.1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Schema(title = "DriverToken", description = "驱动令牌")
public class DriverTokenVO extends BaseVO {

    /**
     * 驱动编号
     */
    @Schema(description = "驱动编号")
    private String driverCode;

    /**
     * 驱动AppID
     */
    @Schema(description = "驱动AppID")
    private String driverAppId;

    /**
     * 驱动AppKey
     */
    @Schema(description = "驱动AppKey")
    private String driverAppKey;

    /**
     * 失效标识
     */
    @Schema(description = "失效标识")
    private ExpireFlagEnum expireFlag;

    /**
     * 失效时间
     */
    @Schema(description = "失效时间")
    private LocalDateTime expireTime;

    /**
     * 使能标识
     */
    @Schema(description = "使能标识")
    private EnableFlagEnum enableFlag;
}
