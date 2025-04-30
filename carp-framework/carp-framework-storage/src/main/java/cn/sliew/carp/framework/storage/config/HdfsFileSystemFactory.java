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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HdfsFileSystemFactory {

    static final String CORE_SITE_XML = "core-site.xml";
    static final String HDFS_SITE_XML = "hdfs-site.xml";

    private static final String HDFS_PROVIDER = "hdfs";

    static final String KERBEROS_CONF = "krb5.conf";
    static final String KERBEROS_KEYTAB = "keytab";
    static final String KERBEROS_KEYTAB_PRINCIPAL = "keytab-principal";
    private static final String SYS_PROP_JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";
}
