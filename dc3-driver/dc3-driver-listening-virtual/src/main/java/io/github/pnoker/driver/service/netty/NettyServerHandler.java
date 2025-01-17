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

package io.github.pnoker.driver.service.netty;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.ObjectUtil;
import io.github.pnoker.common.driver.context.DriverContext;
import io.github.pnoker.common.driver.service.DriverSenderService;
import io.github.pnoker.common.entity.dto.AttributeConfigDTO;
import io.github.pnoker.common.entity.dto.DeviceDTO;
import io.github.pnoker.common.entity.dto.PointDTO;
import io.github.pnoker.common.entity.dto.PointValueDTO;
import io.github.pnoker.common.utils.ConvertUtil;
import io.github.pnoker.common.utils.DriverUtil;
import io.github.pnoker.driver.service.netty.tcp.NettyTcpServer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author pnoker
 * @since 2022.1.0
 */
@Slf4j
@Service
public class NettyServerHandler {

    @Resource
    private DriverSenderService driverSenderService;
    @Resource
    private DriverContext driverContext;

    public Long getDeviceIdByName(String name) {
        List<DeviceDTO> values = new ArrayList<>(driverContext.getDriverMetadataDTO().getDeviceMap().values());
        for (int i = 0; i < values.size(); i++) {
            DeviceDTO device = values.get(i);
            if (device.getDeviceName().equals(name)) {
                return device.getId();
            }
        }
        return null;
    }

    public void read(ChannelHandlerContext context, ByteBuf byteBuf) {
        log.info("{}->{}", context.channel().remoteAddress(), ByteBufUtil.hexDump(byteBuf));
        String deviceName = byteBuf.toString(0, 22, CharsetUtil.CHARSET_ISO_8859_1);
        Long deviceId = getDeviceIdByName(deviceName);
        String hexKey = ByteBufUtil.hexDump(byteBuf, 22, 1);

        //TODO 简单的例子，用于存储channel，然后配合write接口实现向下发送数据
        NettyTcpServer.deviceChannelMap.put(deviceId, context.channel());

        List<PointValueDTO> pointValues = new ArrayList<>(16);
        Map<Long, Map<String, AttributeConfigDTO>> pointInfoMap = driverContext.getDriverMetadataDTO().getPointInfoMap().get(deviceId);
        for (Long pointId : pointInfoMap.keySet()) {
            PointDTO point = driverContext.getPointByDeviceIdAndPointId(deviceId, pointId);
            Map<String, AttributeConfigDTO> infoMap = pointInfoMap.get(pointId);
            int start = DriverUtil.value(infoMap.get("start").getType().getCode(), infoMap.get("start").getValue());
            int end = DriverUtil.value(infoMap.get("end").getType().getCode(), infoMap.get("end").getValue());

            if (infoMap.get("key").getValue().equals(hexKey)) {
                PointValueDTO pointValue = null;
                switch (point.getPointName()) {
                    case "海拔":
                        float altitude = byteBuf.getFloat(start);
                        pointValue = new PointValueDTO(deviceId, pointId, String.valueOf(altitude),
                                ConvertUtil.convertValue(point, String.valueOf(altitude)));
                        break;
                    case "速度":
                        double speed = byteBuf.getDouble(start);
                        pointValue = new PointValueDTO(deviceId, pointId, String.valueOf(speed),
                                ConvertUtil.convertValue(point, String.valueOf(speed)));
                        break;
                    case "液位":
                        long level = byteBuf.getLong(start);
                        pointValue = new PointValueDTO(deviceId, pointId, String.valueOf(level),
                                ConvertUtil.convertValue(point, String.valueOf(level)));
                        break;
                    case "方向":
                        int direction = byteBuf.getInt(start);
                        pointValue = new PointValueDTO(deviceId, pointId, String.valueOf(direction),
                                ConvertUtil.convertValue(point, String.valueOf(direction)));
                        break;
                    case "锁定":
                        boolean lock = byteBuf.getBoolean(start);
                        pointValue = new PointValueDTO(deviceId, pointId, String.valueOf(lock),
                                ConvertUtil.convertValue(point, String.valueOf(lock)));
                        break;
                    case "经纬":
                        String lalo = byteBuf.toString(start, end, CharsetUtil.CHARSET_ISO_8859_1).trim();
                        pointValue = new PointValueDTO(deviceId, pointId, lalo,
                                ConvertUtil.convertValue(point, lalo));
                        break;
                    default:
                        break;
                }
                if (ObjectUtil.isNotNull(pointValue)) {
                    pointValues.add(pointValue);
                }
            }
        }
        driverSenderService.pointValueSender(pointValues);
    }
}
