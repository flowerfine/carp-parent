/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.sliew.carp.framework.queue.kekio.configuration;

import cn.sliew.carp.framework.spring.jackson.ObjectMapperSubtypeConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(KekioObjectMapperSubtypeProperties.class)
public class KekioObjectMapperConfiguration implements InitializingBean {

    @Autowired
    private KekioObjectMapperSubtypeProperties properties;
    @Autowired
    private ObjectMapper mapper;

    @Override
    public void afterPropertiesSet() throws Exception {
        ObjectMapperSubtypeConfigurer subtypeConfigurer = new ObjectMapperSubtypeConfigurer(false);

//        subtypeConfigurer.registerSubtype(mapper, new StringSubtypeLocator(properties.getMessageRootType(), properties.getMessagePackages()));
//        subtypeConfigurer.registerSubtype(mapper, new StringSubtypeLocator(properties.getAttributeRootType(), properties.getAttributePackages()));
//        properties.getExtraSubtypes().forEach((rootType, subtypePackages) -> {
//            subtypeConfigurer.registerSubtype(mapper, new StringSubtypeLocator(rootType, subtypePackages));
//        });

        subtypeConfigurer.registerSubtype(mapper, properties.getMessageSubtypeLocator());
        subtypeConfigurer.registerSubtype(mapper, properties.getAttributeSubtypeLocator());
        properties.getExtraSubtypeLocators().forEach(subtypeLocator -> {
            subtypeConfigurer.registerSubtype(mapper, subtypeLocator);
        });
    }

}
