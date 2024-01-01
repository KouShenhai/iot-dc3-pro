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

package io.github.pnoker.center.auth.entity.builder;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.pnoker.center.auth.entity.bo.ApiBO;
import io.github.pnoker.center.auth.entity.model.ApiDO;
import io.github.pnoker.center.auth.entity.vo.ApiVO;
import io.github.pnoker.common.entity.ext.ApiExt;
import io.github.pnoker.common.entity.ext.JsonExt;
import io.github.pnoker.common.utils.JsonUtil;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Api Builder
 *
 * @author pnoker
 * @since 2022.1.0
 */
@Mapper(componentModel = "spring")
public interface ApiBuilder {

    /**
     * VO to BO
     *
     * @param entityVO EntityVO
     * @return EntityBO
     */
    @Mapping(target = "tenantId", ignore = true)
    ApiBO buildBOByVO(ApiVO entityVO);

    /**
     * VOList to BOList
     *
     * @param entityVOList EntityVO Array
     * @return EntityBO Array
     */
    List<ApiBO> buildBOListByVOList(List<ApiVO> entityVOList);

    /**
     * BO to DO
     *
     * @param entityBO EntityBO
     * @return EntityDO
     */
    @Mapping(target = "apiExt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    ApiDO buildDOByBO(ApiBO entityBO);

    @AfterMapping
    default void afterProcess(ApiBO entityBO, @MappingTarget ApiDO entityDO) {
        ApiExt entityExt = entityBO.getApiExt();
        if (ObjectUtil.isNotNull(entityExt)) {
            JsonExt ext = new JsonExt();
            ext.setType(entityExt.getType());
            ext.setVersion(entityExt.getVersion());
            ext.setVersion(entityExt.getVersion());
            ext.setRemark(entityExt.getRemark());
            ext.setContent(JsonUtil.toJsonString(entityExt.getContent()));
            entityDO.setApiExt(ext);
        }
    }

    /**
     * BOList to DOList
     *
     * @param entityBOList EntityBO Array
     * @return EntityDO Array
     */
    List<ApiDO> buildDOListByBOList(List<ApiBO> entityBOList);

    /**
     * DO to BO
     *
     * @param entityDO EntityDO
     * @return EntityBO
     */
    @Mapping(target = "apiExt", ignore = true)
    ApiBO buildBOByDO(ApiDO entityDO);

    @AfterMapping
    default void afterProcess(ApiDO entityDO, @MappingTarget ApiBO entityBO) {
        JsonExt entityExt = entityDO.getApiExt();
        if (ObjectUtil.isNotNull(entityExt)) {
            ApiExt ext = new ApiExt();
            ext.setType(entityExt.getType());
            ext.setVersion(entityExt.getVersion());
            ext.setVersion(entityExt.getVersion());
            ext.setRemark(entityExt.getRemark());
            ext.setContent(JsonUtil.parseObject(entityExt.getContent(), ApiExt.Content.class));
            entityBO.setApiExt(ext);
        }
    }

    /**
     * DOList to BOList
     *
     * @param entityDOList EntityDO Array
     * @return EntityBO Array
     */
    List<ApiBO> buildBOListByDOList(List<ApiDO> entityDOList);

    /**
     * BO to VO
     *
     * @param entityBO EntityBO
     * @return EntityVO
     */
    ApiVO buildVOByBO(ApiBO entityBO);

    /**
     * BOList to VOList
     *
     * @param entityBOList EntityBO Array
     * @return EntityVO Array
     */
    List<ApiVO> buildVOListByBOList(List<ApiBO> entityBOList);

    /**
     * DOPage to BOPage
     *
     * @param entityPageDO EntityDO Page
     * @return EntityBO Page
     */
    @Mapping(target = "optimizeJoinOfCountSql", ignore = true)
    Page<ApiBO> buildBOPageByDOPage(Page<ApiDO> entityPageDO);

    /**
     * BOPage to VOPage
     *
     * @param entityPageBO EntityBO Page
     * @return EntityVO Page
     */
    @Mapping(target = "optimizeJoinOfCountSql", ignore = true)
    Page<ApiVO> buildVOPageByBOPage(Page<ApiBO> entityPageBO);
}