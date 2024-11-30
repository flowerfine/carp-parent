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
package cn.sliew.carp.framework.common.reflection;

public class JobParameter {

    private String className;
    private String actualClassName;
    private Object object;

    private JobParameter() {
        // used for deserialization
    }

    private JobParameter(Class<?> clazz) {
        this(clazz.getName(), null);
    }

    public JobParameter(Class<?> clazz, Object object) {
        this(clazz.getName(), object);
    }

    public JobParameter(Object object) {
        this(object.getClass().getName(), object);
    }

    public JobParameter(String className, Object object) {
        this(className, isNotNullNorAnEnum(object) ? object.getClass().getName() : className, object);
    }

    public JobParameter(String className, String actualClassName, Object object) {
        this.className = className;
        this.actualClassName = actualClassName;
        this.object = object;
    }

    /**
     * Represents the class name expected by the job method (e.g. an object or an interface)
     *
     * @return the class name expected by the job method (e.g. an object or an interface)
     */
    public String getClassName() {
        return className;
    }

    /**
     * Represents the actual class name of the job parameter (e.g. an object), this will never be an interface
     *
     * @return the actual class name of the job parameter (e.g. an object), this will never be an interface
     */
    public String getActualClassName() {
        return actualClassName;
    }

    /**
     * The actual job parameter
     *
     * @return the actual job parameter
     */
    public Object getObject() {
        return object;
    }


    protected static boolean isNotNullNorAnEnum(Object object) {
        return object != null && !(object instanceof Enum);
    }
}
