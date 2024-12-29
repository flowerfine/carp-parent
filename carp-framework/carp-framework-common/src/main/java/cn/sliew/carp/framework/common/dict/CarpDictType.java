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
package cn.sliew.carp.framework.common.dict;

import cn.sliew.carp.framework.common.dict.alert.CarpAlertStatus;
import cn.sliew.carp.framework.common.dict.common.CarpIsDeleted;
import cn.sliew.carp.framework.common.dict.common.CarpYesOrNo;
import cn.sliew.carp.framework.common.dict.datasource.CarpDataSourceType;
import cn.sliew.carp.framework.common.dict.datasource.CarpRedisMode;
import cn.sliew.carp.framework.common.dict.k8s.CarpClusterStatus;
import cn.sliew.carp.framework.common.dict.k8s.CarpClusterType;
import cn.sliew.carp.framework.common.dict.license.CarpLicenseType;
import cn.sliew.carp.framework.common.dict.oam.CarpAppType;
import cn.sliew.carp.framework.common.dict.schedule.*;
import cn.sliew.carp.framework.common.dict.security.*;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum CarpDictType implements DictDefinition {

    YES_OR_NO("carp_yes_or_no", "是否", CarpYesOrNo.class),
    IS_DELETED("carp_is_delete", "是否删除", CarpIsDeleted.class),

    LICENSE_TYPE("carp_license_type", "证书类型", CarpLicenseType.class),

    K8S_CLUSTER_TYPE("carp_k8s_cluster_type", "集群类型", CarpClusterType.class),
    K8S_CLUSTER_STATUS("carp_k8s_cluster_status", "集群状态", CarpClusterStatus.class),

    OAM_APP_TYPE("carp_oam_app_type", "应用类型", CarpAppType.class),

    ALERT_STATUS("carp_alert_status", "告警消息状态", CarpAlertStatus.class),

    SEC_APPLICATION_TYPE("carp_sec_application_type", "安全-应用类型", CarpSecApplicationType.class),
    SEC_APPLICATION_STATUS("carp_sec_application_status", "安全-应用状态", CarpSecApplicationStatus.class),
    USER_TYPE("carp_sec_user_type", "安全-用户类型", CarpSecUserType.class),
    USER_STATUS("carp_sec_user_status", "安全-用户状态", CarpSecUserStatus.class),
    ROLE_TYPE("carp_sec_role_type", "安全-角色类型", CarpSecRoleType.class),
    ROLE_STATUS("carp_sec_role_status", "安全-角色状态", CarpSecRoleStatus.class),
    RESOURCE_WEB_TYPE("carp_sec_resource_web_type", "安全-资源-web-类型", CarpSecResourceWebType.class),
    RESOURCE_DATA_TYPE("carp_sec_resource_data_type", "安全-资源-数据-类型", CarpSecResourceDataType.class),
    RESOURCE_STATUS("carp_sec_resource_status", "安全-资源状态", CarpSecResourceStatus.class),

    DATASOURCE_TYPE("carp_datasource_type", "数据源类型", CarpDataSourceType.class),
    DS_REDIS_MODE("carp_datasource_redis_mode", "Redis Mode", CarpRedisMode.class),

    SCHEDULE_TYPE("carp_schedule_type", "Schedule Type", CarpScheduleType.class),
    SCHEDULE_STATUS("carp_schedule_status", "Schedule Status", CarpScheduleStatus.class),
    SCHEDULE_JOB_TYPE("carp_schedule_job_type", "Schedule Job Type", CarpScheduleJobType.class),
    SCHEDULE_ENGINE_TYPE("carp_schedule_engine_type", "Schedule Engine Type", CarpScheduleEngineType.class),
    ;

    @JsonCreator
    public static CarpDictType of(String code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode().equals(code))
                .findAny().orElseThrow(() -> new EnumConstantNotPresentException(CarpDictType.class, code));
    }

    @EnumValue
    private String code;
    private String name;
    private Class instanceClass;

    CarpDictType(String code, String name, Class instanceClass) {
        this.code = code;
        this.name = name;
        this.instanceClass = instanceClass;
    }

    @Override
    public String getProvider() {
        return "Carp";
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name;
    }

    @JsonIgnore
    public Class getInstanceClass() {
        return instanceClass;
    }
}
