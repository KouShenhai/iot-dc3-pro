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

package io.github.pnoker.center.manager.entity.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.github.pnoker.common.entity.base.BaseVO;
import io.github.pnoker.common.entity.ext.PointAttributeExt;
import io.github.pnoker.common.enums.AttributeTypeFlagEnum;
import io.github.pnoker.common.enums.EnableFlagEnum;
import io.github.pnoker.common.valid.Add;
import io.github.pnoker.common.valid.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * PointAttribute BO
 *
 * @author pnoker
 * @since 2022.1.0
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Schema(title = "PointAttribute", description = "位号属性")
public class PointAttributeVO extends BaseVO {

    /**
     * 显示名称
     */
    @Schema(description = "显示名称")
    @NotBlank(message = "显示名称不能为空",
            groups = {Add.class})
    @Pattern(regexp = "^[A-Za-z0-9\\u4e00-\\u9fa5][A-Za-z0-9\\u4e00-\\u9fa5-_#@/.|]{1,31}$",
            message = "显示名称格式无效",
            groups = {Add.class, Update.class})
    private String displayName;

    /**
     * 属性名称
     */
    @Schema(description = "属性名称")
    @NotBlank(message = "属性名称不能为空",
            groups = {Add.class})
    @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9-_#@/.|]{1,31}$",
            message = "属性名称格式无效",
            groups = {Add.class, Update.class})
    private String attributeName;

    /**
     * 属性类型标识
     */
    @Schema(description = "属性类型标识")
    private AttributeTypeFlagEnum attributeTypeFlag;

    /**
     * 默认值
     */
    @Schema(description = "默认值")
    private String defaultValue;

    /**
     * 驱动ID
     */
    @Schema(description = "驱动ID")
    @NotNull(message = "驱动ID不能为空"
            , groups = {Add.class, Update.class})
    private Long driverId;

    /**
     * 位号属性拓展信息
     */
    @Schema(description = "位号属性拓展信息")
    private PointAttributeExt attributeExt;

    /**
     * 使能标识
     */
    @Schema(description = "使能标识")
    private EnableFlagEnum enableFlag;

    /**
     * 签名
     */
    @Schema(description = "签名")
    private String signature;

    /**
     * 版本
     */
    @Schema(description = "版本")
    private Integer version;
}
