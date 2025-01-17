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

package io.github.pnoker.center.manager.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.pnoker.center.manager.entity.builder.GroupForManagerBuilder;
import io.github.pnoker.center.manager.entity.query.GroupQuery;
import io.github.pnoker.center.manager.service.GroupService;
import io.github.pnoker.common.base.BaseController;
import io.github.pnoker.common.constant.service.ManagerConstant;
import io.github.pnoker.common.entity.R;
import io.github.pnoker.common.entity.bo.GroupBO;
import io.github.pnoker.common.entity.vo.GroupVO;
import io.github.pnoker.common.enums.ResponseEnum;
import io.github.pnoker.common.valid.Add;
import io.github.pnoker.common.valid.Update;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

/**
 * 分组 Controller
 *
 * @author pnoker
 * @since 2022.1.0
 */
@Slf4j
@RestController
@Tag(name = "接口-分组")
@RequestMapping(ManagerConstant.GROUP_URL_PREFIX)
public class GroupController implements BaseController {

    @Resource
    private GroupForManagerBuilder groupForManagerBuilder;

    @Resource
    private GroupService groupService;

    /**
     * 新增
     *
     * @param entityVO {@link GroupVO}
     * @return R of String
     */
    @PostMapping("/add")
    @Operation(summary = "新增-分组")
    public R<String> add(@Validated(Add.class) @RequestBody GroupVO entityVO) {
        try {
            GroupBO entityBO = groupForManagerBuilder.buildBOByVO(entityVO);
            entityBO.setTenantId(getTenantId());
            groupService.save(entityBO);
            return R.ok(ResponseEnum.ADD_SUCCESS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 删除
     *
     * @param id ID
     * @return R of String
     */
    @PostMapping("/delete/{id}")
    public R<String> delete(@NotNull @PathVariable(value = "id") Long id) {
        try {
            groupService.remove(id);
            return R.ok(ResponseEnum.DELETE_SUCCESS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 更新
     *
     * @param entityVO {@link GroupVO}
     * @return R of String
     */
    @PostMapping("/update")
    public R<String> update(@Validated(Update.class) @RequestBody GroupVO entityVO) {
        try {
            GroupBO entityBO = groupForManagerBuilder.buildBOByVO(entityVO);
            groupService.update(entityBO);
            return R.ok(ResponseEnum.UPDATE_SUCCESS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 单个查询
     *
     * @param id ID
     * @return GroupVO {@link GroupVO}
     */
    @GetMapping("/id/{id}")
    public R<GroupVO> selectById(@NotNull @PathVariable(value = "id") Long id) {
        try {
            GroupBO entityBO = groupService.selectById(id);
            GroupVO entityVO = groupForManagerBuilder.buildVOByBO(entityBO);
            return R.ok(entityVO);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.fail(e.getMessage());
        }
    }

    /**
     * 分页查询
     *
     * @param entityQuery {@link GroupQuery}
     * @return R Of GroupVO Page
     */
    @PostMapping("/list")
    public R<Page<GroupVO>> list(@RequestBody(required = false) GroupQuery entityQuery) {
        try {
            if (ObjectUtil.isEmpty(entityQuery)) {
                entityQuery = new GroupQuery();
            }
            entityQuery.setTenantId(getTenantId());
            Page<GroupBO> entityPageBO = groupService.selectByPage(entityQuery);
            Page<GroupVO> entityPageVO = groupForManagerBuilder.buildVOPageByBOPage(entityPageBO);
            return R.ok(entityPageVO);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return R.fail(e.getMessage());
        }
    }

}
