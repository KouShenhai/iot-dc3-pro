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

package io.github.pnoker.center.auth.entity.bo;

import io.github.pnoker.common.base.BaseBO;
import io.github.pnoker.common.valid.Add;
import io.github.pnoker.common.valid.Update;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;

/**
 * TenantBind BO
 *
 * @author pnoker
 * @since 2022.1.0
 */
@Data
@SuperBuilder
@RequiredArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TenantBindBO extends BaseBO {

    /**
     * 租户ID
     */
    @NotBlank(message = "Tenant id can't be empty",
            groups = {Add.class, Update.class})
    private Long tenantId;

    /**
     * 用户ID
     */
    @NotBlank(message = "User id can't be empty",
            groups = {Add.class, Update.class})
    private Long userId;
}