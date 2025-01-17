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

package io.github.pnoker.center.data.generator;

import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.google.common.collect.ImmutableMap;
import io.github.pnoker.common.utils.MybatisUtil;

/**
 * 自动代码生成工具
 * <p>
 * 注意：
 * <p>
 * 当前配置仅用于 dc3-center-data 服务模块，如果需要用于其他模块请重新配置 path 参数。
 *
 * @author pnoker
 * @since 2022.1.0
 */
public class MybatisGenerator {
    public static void main(String[] args) {
        generator();
    }

    public static void generator() {
        String path = System.getProperty("user.dir") + "/dc3-center/dc3-center-data/src/main";
        MybatisUtil.defaultGenerator("dc3-mysql", "3306", "dc3_data", "root", "dc3")
                .globalConfig(builder -> MybatisUtil.defaultGlobalConfig(builder, path))
                .dataSourceConfig(MybatisUtil::defaultDataSourceConfig)
                .packageConfig(builder -> builder
                        .parent("io.github.pnoker.center.data")
                        .entity("entity.model")
                        .service("dal")
                        .serviceImpl("dal.impl")
                        .mapper("mapper")
                        .pathInfo(ImmutableMap.of(
                                OutputFile.service, path + "/java/io/github/pnoker/center/data/dal",
                                OutputFile.serviceImpl, path + "/java/io/github/pnoker/center/data/dal/impl",
                                OutputFile.xml, path + "/resources/mapping"))
                ).templateConfig(builder -> builder.disable(TemplateType.CONTROLLER))
                .templateEngine(new VelocityTemplateEngine())
                .strategyConfig(MybatisUtil::defaultStrategyConfig)
                .strategyConfig(builder -> builder
                        .addInclude(
                                "dc3_alarm_message_profile",
                                "dc3_alarm_notify_profile",
                                "dc3_alarm_rule",
                                "dc3_group",
                                "dc3_label",
                                "dc3_label_bind"
                        )
                ).execute();
    }
}