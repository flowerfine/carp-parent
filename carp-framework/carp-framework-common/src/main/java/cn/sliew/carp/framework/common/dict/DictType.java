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

import cn.sliew.carp.framework.common.dict.alert.AlertStatus;
import cn.sliew.carp.framework.common.dict.common.IsDeleted;
import cn.sliew.carp.framework.common.dict.common.YesOrNo;
import cn.sliew.carp.framework.common.dict.datasource.DataSourceType;
import cn.sliew.carp.framework.common.dict.datasource.RedisMode;
import cn.sliew.carp.framework.common.dict.k8s.ClusterStatus;
import cn.sliew.carp.framework.common.dict.k8s.ClusterType;
import cn.sliew.carp.framework.common.dict.license.LicenseType;
import cn.sliew.carp.framework.common.dict.oam.AppType;
import cn.sliew.carp.framework.common.dict.schedule.ScheduleEngineType;
import cn.sliew.carp.framework.common.dict.schedule.ScheduleJobType;
import cn.sliew.carp.framework.common.dict.schedule.ScheduleStatus;
import cn.sliew.carp.framework.common.dict.schedule.ScheduleType;
import cn.sliew.carp.framework.common.dict.security.*;
import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Arrays;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum DictType implements DictDefinition {

    YES_OR_NO("yes_or_no", "是否", YesOrNo.class),
    IS_DELETED("is_delete", "是否删除", IsDeleted.class),

    LICENSE_TYPE("license_type", "证书类型", LicenseType.class),

    K8S_CLUSTER_TYPE("k8s_cluster_type", "集群类型", ClusterType.class),
    K8S_CLUSTER_STATUS("k8s_cluster_status", "集群状态", ClusterStatus.class),

    OAM_APP_TYPE("oam_app_type", "应用类型", AppType.class),

    ALERT_STATUS("alert_status", "告警消息状态", AlertStatus.class),

    SEC_APPLICATION_TYPE("sec_application_type", "安全-应用类型", SecApplicationType.class),
    SEC_APPLICATION_STATUS("sec_application_status", "安全-应用状态", SecApplicationStatus.class),
    USER_TYPE("sec_user_type", "安全-用户类型", SecUserType.class),
    USER_STATUS("sec_user_status", "安全-用户状态", SecUserStatus.class),
    ROLE_TYPE("sec_role_type", "安全-角色类型", SecRoleType.class),
    ROLE_STATUS("sec_role_status", "安全-角色状态", SecRoleStatus.class),
    RESOURCE_WEB_TYPE("sec_resource_web_type", "安全-资源-web-类型", SecResourceWebType.class),
    RESOURCE_DATA_TYPE("sec_resource_data_type", "安全-资源-数据-类型", SecResourceDataType.class),
    RESOURCE_STATUS("sec_resource_status", "安全-资源状态", SecResourceStatus.class),

    DATASOURCE_TYPE("datasource_type", "数据源类型", DataSourceType.class),
    DS_REDIS_MODE("datasource_redis_mode", "Redis Mode", RedisMode.class),

    SCHEDULE_TYPE("schedule_type", "Schedule Type", ScheduleType.class),
    SCHEDULE_STATUS("schedule_status", "Schedule Status", ScheduleStatus.class),
    SCHEDULE_JOB_TYPE("schedule_job_type", "Schedule Job Type", ScheduleJobType.class),
    SCHEDULE_ENGINE_TYPE("schedule_engine_type", "Schedule Engine Type", ScheduleEngineType.class),
    ;

    @JsonCreator
    public static DictType of(String code) {
        return Arrays.stream(values())
                .filter(type -> type.getCode().equals(code))
                .findAny().orElseThrow(() -> new EnumConstantNotPresentException(DictType.class, code));
    }

    @EnumValue
    private String code;
    private String name;
    private Class instanceClass;

    DictType(String code, String name, Class instanceClass) {
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
