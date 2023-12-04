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

package io.github.pnoker.center.manager.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.github.pnoker.center.manager.entity.query.DriverPageQuery;
import io.github.pnoker.center.manager.mapper.DriverMapper;
import io.github.pnoker.center.manager.service.DeviceService;
import io.github.pnoker.center.manager.service.DriverService;
import io.github.pnoker.common.entity.common.Pages;
import io.github.pnoker.common.exception.*;
import io.github.pnoker.common.model.Device;
import io.github.pnoker.common.model.DriverDO;
import io.github.pnoker.common.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DriverService Impl
 *
 * @author pnoker
 * @since 2022.1.0
 */
@Slf4j
@Service
public class DriverServiceImpl implements DriverService {

    @Resource
    private DriverMapper driverMapper;

    @Lazy
    @Resource
    private DeviceService deviceService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void save(DriverDO entityBO) {
        boolean duplicate = checkDuplicate(entityBO);
        if (duplicate) {
            throw new DuplicateException("The driver already exists");
        }

        if (driverMapper.insert(entityBO) < 1) {
            throw new AddException("The driver {} add failed", entityBO.getDriverName());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(Long id) {
        DriverDO entityDO = selectById(id);
        if (ObjectUtil.isNull(entityDO)) {
            throw new NotFoundException("The driver does not exist");
        }

        if (driverMapper.deleteById(id) < 1) {
            throw new DeleteException("The driver delete failed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(DriverDO entityBO) {
        selectById(entityBO.getId());
        entityBO.setOperateTime(null);
        if (driverMapper.updateById(entityBO) < 1) {
            throw new UpdateException("The driver update failed");
        }
    }

    @Override
    public DriverDO selectById(Long id) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<DriverDO> selectByPage(DriverPageQuery entityQuery) {
        if (ObjectUtil.isNull(entityQuery.getPage())) {
            entityQuery.setPage(new Pages());
        }
        return driverMapper.selectPage(PageUtil.page(entityQuery.getPage()), fuzzyQuery(entityQuery));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DriverDO> selectByIds(Set<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return driverMapper.selectBatchIds(ids);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriverDO selectByServiceName(String serviceName, Long tenantId, boolean throwException) {
        LambdaQueryWrapper<DriverDO> queryWrapper = Wrappers.<DriverDO>query().lambda();
        queryWrapper.eq(DriverDO::getServiceName, serviceName);
        queryWrapper.eq(DriverDO::getTenantId, tenantId);
        queryWrapper.last("limit 1");
        DriverDO entityDO = driverMapper.selectOne(queryWrapper);
        if (throwException && (ObjectUtil.isNull(entityDO))) {
            throw new NotFoundException("The driver does not exist of service name: {} for tenant: {}", serviceName, tenantId);
        }
        return entityDO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DriverDO> selectByProfileId(Long profileId) {
        List<Device> devices = deviceService.selectByProfileId(profileId);
        Set<Long> driverIds = devices.stream().map(Device::getDriverId).collect(Collectors.toSet());
        List<DriverDO> entityDOList = selectByIds(driverIds);
        if (CollUtil.isEmpty(entityDOList)) {
            return Collections.emptyList();
        }
        return entityDOList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DriverDO selectByDeviceId(Long deviceId) {
        Device device = deviceService.selectById(deviceId);
        return selectById(device.getDriverId());
    }

    @Override
    public Long count() {
        return driverMapper.selectCount(new QueryWrapper<>());
    }

    /**
     * 按服务名称，判断是否重复
     *
     * @param entityDO DriverDO
     * @return 是否重复
     */
    private boolean checkDuplicate(DriverDO entityDO) {
        DriverDO driverDO = selectByServiceName(entityDO.getServiceName(), entityDO.getTenantId(), false);
        return ObjectUtil.isNotNull(driverDO);
    }

    private LambdaQueryWrapper<DriverDO> fuzzyQuery(DriverPageQuery query) {
        LambdaQueryWrapper<DriverDO> queryWrapper = Wrappers.<DriverDO>query().lambda();
        if (ObjectUtil.isNotNull(query)) {
            queryWrapper.like(CharSequenceUtil.isNotEmpty(query.getDriverName()), DriverDO::getDriverName, query.getDriverName());
            queryWrapper.like(CharSequenceUtil.isNotEmpty(query.getServiceName()), DriverDO::getServiceName, query.getServiceName());
            queryWrapper.like(CharSequenceUtil.isNotEmpty(query.getServiceHost()), DriverDO::getServiceHost, query.getServiceHost());
            queryWrapper.eq(ObjectUtil.isNotNull(query.getDriverTypeFlag()), DriverDO::getDriverTypeFlag, query.getDriverTypeFlag());
            queryWrapper.eq(ObjectUtil.isNotNull(query.getEnableFlag()), DriverDO::getEnableFlag, query.getEnableFlag());
            queryWrapper.eq(ObjectUtil.isNotEmpty(query.getTenantId()), DriverDO::getTenantId, query.getTenantId());
        }
        return queryWrapper;
    }

}
