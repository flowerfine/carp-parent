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
package cn.sliew.carp.framework.storage.config;

import lombok.Data;

@Data
public class HdfsConfigProperties {

    public static final String CORE_SITE_XML = "core-site.xml";
    public static final String HDFS_SITE_XML = "hdfs-site.xml";
    public static final String KERBEROS_CONF = "krb5.conf";
    public static final String KERBEROS_KEYTAB = "keytab";
    public static final String KERBEROS_KEYTAB_PRINCIPAL = "keytab-principal";
    public static final String SYS_PROP_JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";

    public static final String HADOOP_HOME_ENV = "HADOOP_HOME";
    public static final String HADOOP_CONF_DIR_ENV = "HADOOP_CONF_DIR";

    private String defaultFS;

    private String coreSiteXml;
    private String coreSiteXmlPath;

    private String hdfsSiteXml;
    private String hdfsSiteXmlPath;

    private String kerberosConf;
    private String kerberosConfPath;

    private String kerberosKeytab;

    private String kerberosKeytabPrincipal;
}
