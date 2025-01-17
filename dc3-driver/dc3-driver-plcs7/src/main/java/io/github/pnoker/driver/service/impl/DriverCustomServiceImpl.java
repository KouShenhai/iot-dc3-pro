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

package io.github.pnoker.driver.service.impl;

import cn.hutool.core.util.ObjectUtil;
import io.github.pnoker.common.constant.common.DefaultConstant;
import io.github.pnoker.common.driver.context.DriverContext;
import io.github.pnoker.common.driver.service.DriverCustomService;
import io.github.pnoker.common.driver.service.DriverSenderService;
import io.github.pnoker.common.entity.dto.AttributeConfigDTO;
import io.github.pnoker.common.entity.dto.DeviceDTO;
import io.github.pnoker.common.entity.dto.PointDTO;
import io.github.pnoker.common.enums.DeviceStatusEnum;
import io.github.pnoker.common.enums.PointTypeFlagEnum;
import io.github.pnoker.common.exception.ServiceException;
import io.github.pnoker.common.utils.JsonUtil;
import io.github.pnoker.driver.api.S7Connector;
import io.github.pnoker.driver.api.S7Serializer;
import io.github.pnoker.driver.api.factory.S7ConnectorFactory;
import io.github.pnoker.driver.api.factory.S7SerializerFactory;
import io.github.pnoker.driver.bean.PlcS7PointVariable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static io.github.pnoker.common.utils.DriverUtil.attribute;
import static io.github.pnoker.common.utils.DriverUtil.value;

/**
 * @author pnoker
 * @since 2022.1.0
 */
@Slf4j
@Service
public class DriverCustomServiceImpl implements DriverCustomService {

    @Resource
    private DriverContext driverContext;
    @Resource
    private DriverSenderService driverSenderService;

    /**
     * Plc Connector Map
     * 仅供参考
     */
    private Map<Long, MyS7Connector> s7ConnectorMap;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class MyS7Connector {
        private ReentrantReadWriteLock lock;
        private S7Connector connector;
    }

    @Override
    public void initial() {
        /*
        !!! 提示：此处逻辑仅供参考，请务必结合实际应用场景。!!!
        !!!
        你可以在此处执行一些特定的初始化逻辑，驱动在启动的时候会自动执行该方法。
        */
        s7ConnectorMap = new ConcurrentHashMap<>(16);
    }

    @Override
    public void schedule() {
        /*
        !!! 提示：此处逻辑仅供参考，请务必结合实际应用场景。!!!
        !!!
        上传设备状态，可自行灵活拓展，不一定非要在schedule()接口中实现，你可以：
        - 在read中实现设备状态的判断；
        - 在自定义定时任务中实现设备状态的判断；
        - 通过某种判断机制实现设备状态的判断。

        最后通过 driverSenderService.deviceStatusSender(deviceId,deviceStatus) 接口将设备状态交给SDK管理，其中设备状态（StatusEnum）：
        - ONLINE:在线
        - OFFLINE:离线
        - MAINTAIN:维护
        - FAULT:故障
         */
        driverContext.getDriverMetadataDTO().getDeviceMap().keySet().forEach(id -> driverSenderService.deviceStatusSender(id, DeviceStatusEnum.ONLINE, 25, TimeUnit.SECONDS));
    }

    @Override
    public String read(Map<String, AttributeConfigDTO> driverInfo, Map<String, AttributeConfigDTO> pointInfo, DeviceDTO device, PointDTO point) {
        /*
        !!! 提示：此处逻辑仅供参考，请务必结合实际应用场景。!!!
         */
        log.debug("Plc S7 Read, device: {}, point: {}", JsonUtil.toJsonString(device), JsonUtil.toJsonString(point));
        MyS7Connector myS7Connector = getS7Connector(device.getId(), driverInfo);
        myS7Connector.lock.writeLock().lock();
        S7Serializer serializer = S7SerializerFactory.buildSerializer(myS7Connector.getConnector());
        PlcS7PointVariable plcs7PointVariable = getPointVariable(pointInfo, point.getPointTypeFlag().getCode());

        try {
            return String.valueOf(serializer.dispense(plcs7PointVariable));
        } catch (Exception e) {
            log.error("Plc S7 Read Error: {}", e.getMessage());
            return DefaultConstant.DEFAULT_STRING_VALUE;
        } finally {
            myS7Connector.lock.writeLock().unlock();
        }
    }

    @Override
    public Boolean write(Map<String, AttributeConfigDTO> driverInfo, Map<String, AttributeConfigDTO> pointInfo, DeviceDTO device, AttributeConfigDTO value) {
        /*
        !!! 提示：此处逻辑仅供参考，请务必结合实际应用场景。!!!
         */
        log.debug("Plc S7 Write, device: {}, value: {}", JsonUtil.toJsonString(device), JsonUtil.toJsonString(value));
        MyS7Connector myS7Connector = getS7Connector(device.getId(), driverInfo);
        myS7Connector.lock.writeLock().lock();
        S7Serializer serializer = S7SerializerFactory.buildSerializer(myS7Connector.getConnector());
        PlcS7PointVariable plcs7PointVariable = getPointVariable(pointInfo, value.getType().getCode());

        try {
            store(serializer, plcs7PointVariable, value.getType().getCode(), value.getValue());
            return true;
        } catch (Exception e) {
            log.error("Plc S7 Write Error: {}", e.getMessage());
            return false;
        } finally {
            myS7Connector.lock.writeLock().unlock();
        }
    }

    /**
     * 获取 plcs7 serializer
     * 先从缓存中取，没有就新建
     *
     * @param deviceId   设备ID
     * @param driverInfo DeviceInfo Map
     * @return S7Serializer
     */
    private MyS7Connector getS7Connector(Long deviceId, Map<String, AttributeConfigDTO> driverInfo) {
        MyS7Connector myS7Connector = s7ConnectorMap.get(deviceId);
        if (ObjectUtil.isNull(myS7Connector)) {
            myS7Connector = new MyS7Connector();

            log.debug("Plc S7 Connection Info {}", JsonUtil.toJsonString(driverInfo));
            try {
                S7Connector s7Connector = S7ConnectorFactory.buildTCPConnector()
                        .withHost(attribute(driverInfo, "host"))
                        .withPort(attribute(driverInfo, "port"))
                        .build();
                myS7Connector.setLock(new ReentrantReadWriteLock());
                myS7Connector.setConnector(s7Connector);
            } catch (Exception e) {
                throw new ServiceException("new s7connector fail" + e.getMessage());
            }
            s7ConnectorMap.put(deviceId, myS7Connector);
        }
        return myS7Connector;
    }

    /**
     * 获取位号变量信息
     *
     * @param pointInfo PointInfo Map
     * @return Plcs7PointVariable
     */
    private PlcS7PointVariable getPointVariable(Map<String, AttributeConfigDTO> pointInfo, String type) {
        log.debug("Plc S7 Point Attribute Config {}", JsonUtil.toJsonString(pointInfo));
        return new PlcS7PointVariable(attribute(pointInfo, "dbNum"), attribute(pointInfo, "byteOffset"), attribute(pointInfo, "bitOffset"), attribute(pointInfo, "blockSize"), type);
    }

    /**
     * 向 Plc S7 写数据
     *
     * @param serializer         S7Serializer
     * @param plcS7PointVariable Plcs7PointVariable
     * @param type               Value Type
     * @param value              String Value
     */
    private void store(S7Serializer serializer, PlcS7PointVariable plcS7PointVariable, String type, String value) {
        PointTypeFlagEnum valueType = PointTypeFlagEnum.ofCode(type);
        if (ObjectUtil.isNull(valueType)) {
            throw new IllegalArgumentException("Unsupported type of " + type);
        }

        switch (valueType) {
            case INT:
                int intValue = value(type, value);
                serializer.store(intValue, plcS7PointVariable.getDbNum(), plcS7PointVariable.getByteOffset());
                break;
            case LONG:
                long longValue = value(type, value);
                serializer.store(longValue, plcS7PointVariable.getDbNum(), plcS7PointVariable.getByteOffset());
                break;
            case FLOAT:
                float floatValue = value(type, value);
                serializer.store(floatValue, plcS7PointVariable.getDbNum(), plcS7PointVariable.getByteOffset());
                break;
            case DOUBLE:
                double doubleValue = value(type, value);
                serializer.store(doubleValue, plcS7PointVariable.getDbNum(), plcS7PointVariable.getByteOffset());
                break;
            case BOOLEAN:
                boolean booleanValue = value(type, value);
                serializer.store(booleanValue, plcS7PointVariable.getDbNum(), plcS7PointVariable.getByteOffset());
                break;
            case STRING:
                serializer.store(value, plcS7PointVariable.getDbNum(), plcS7PointVariable.getByteOffset());
                break;
            default:
                break;
        }
    }

}
