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
package cn.sliew.carp.framework.common.util.reflection;

import java.io.InputStream;
import java.net.URL;
import java.util.jar.Manifest;

public enum JarUtils {
    ;

    public static String getVersion(Class<?> clazz) {
        String version = clazz.getPackage().getImplementationVersion();
        if (version != null) return version;

        return getManifestAttributeValue(clazz, "Bundle-Version");
    }

    public static String getManifestAttributeValue(Class<?> clazz, String attributeName) {
        return getManifest(clazz).getMainAttributes().getValue(attributeName);
    }

    private static Manifest getManifest(Class<?> clazz) {
        String resource = "/" + clazz.getName().replace(".", "/") + ".class";
        String fullPath = clazz.getResource(resource).toString();
        String archivePath = fullPath.substring(0, fullPath.length() - resource.length());
        if (archivePath.endsWith("\\WEB-INF\\classes") || archivePath.endsWith("/WEB-INF/classes")) {
            archivePath = archivePath.substring(0, archivePath.length() - "/WEB-INF/classes".length()); // Required for wars
        }

        try (InputStream input = new URL(archivePath + "/META-INF/MANIFEST.MF").openStream()) {
            return new Manifest(input);
        } catch (Exception e) {
            throw new RuntimeException("Loading MANIFEST for class " + clazz + " failed!", e);
        }
    }
}
