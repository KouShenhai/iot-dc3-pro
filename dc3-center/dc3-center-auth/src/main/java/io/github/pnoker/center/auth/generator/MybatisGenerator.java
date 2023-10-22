/*
 * Copyright 2016-present the original author or authors.
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

package io.github.pnoker.center.auth.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.TemplateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;
import com.google.common.collect.ImmutableMap;

import java.sql.Types;

public class MybatisGenerator {
    public static void main(String[] args) {
        String path = System.getProperty("user.dir") + "/dc3-center/dc3-center-auth/src/main";
        FastAutoGenerator.create(
                        "jdbc:mysql://dc3-mysql:33306/dc3?useSSL=false",
                        "root",
                        "dc3"
                )
                .globalConfig(builder -> builder
                        .outputDir(path + "/java")
                        .author("Automatic Generated By Mybatis Plus")
                        .disableOpenDir()
                )
                .dataSourceConfig(builder -> builder
                        .typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                            int typeCode = metaInfo.getJdbcType().TYPE_CODE;
                            if (typeCode == Types.SMALLINT) {
                                return DbColumnType.INTEGER;
                            }
                            return typeRegistry.getColumnType(metaInfo);
                        }))
                .packageConfig(builder -> builder
                        .parent("io.github.pnoker.center.auth")
                        .entity("entity.model")
                        .service("service")
                        .serviceImpl("service.impl")
                        .mapper("mapper")
                        .pathInfo(ImmutableMap.of(
                                OutputFile.service, path + "/java/io/github/pnoker/center/auth/service",
                                OutputFile.serviceImpl, path + "/java/io/github/pnoker/center/auth/service/impl",
                                OutputFile.xml, path + "/resources/mapping"))
                ).templateConfig(builder -> builder
                        .disable(TemplateType.CONTROLLER))
                .templateEngine(new VelocityTemplateEngine())
                .strategyConfig(builder -> builder
                        .entityBuilder()
                        .enableLombok()
                        .formatFileName("%sDO")
                        .enableTableFieldAnnotation()
                        .enableRemoveIsPrefix()
                        .enableFileOverride()
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .formatServiceImplFileName("%sServiceImpl")
                        .enableFileOverride()
                        .mapperBuilder()
                        .enableBaseColumnList()
                        .enableBaseResultMap()
                        .enableFileOverride()
                ).strategyConfig(builder -> builder
                        // 仅修改 addInclude 为代生成代表明即可
                        .addInclude(
                                "sentry_device_point_check_history",
                                "sentry_alert_history"
                        )).execute();
    }
}