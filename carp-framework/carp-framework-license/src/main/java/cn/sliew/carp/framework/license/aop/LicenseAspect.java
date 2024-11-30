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
package cn.sliew.carp.framework.license.aop;

import cn.sliew.carp.framework.license.CarpLicense;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Objects;

@Slf4j
@Component
public class LicenseAspect {

    @Autowired(required = false)
    private CarpLicense license;

    @Pointcut("@annotation(cn.sliew.carp.framework.license.annotation.ValidLicenseEnabled)")
    public void validLicensePointcut() {
    }

    @Pointcut("@annotation(cn.sliew.carp.framework.license.annotation.ProLicenseEnabled)")
    public void proLicensePointcut() {
    }

    @Pointcut("@annotation(cn.sliew.carp.framework.license.annotation.EnterpriseLicenseEnabled)")
    public void enterpriseLicensePointcut() {
    }

    @Before("validLicensePointcut()")
    public void beforeValidLicenseCheck(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (Objects.nonNull(license) && license.isValid()) {
            return;
        }
        throw new RuntimeException("license invalid or expired");
    }

    @Before("proLicensePointcut()")
    public void beforeProLicenseCheck(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (Objects.nonNull(license) && license.isPro()) {
            return;
        }
        throw new RuntimeException("license invalid or expired");
    }

    @Before("enterpriseLicensePointcut()")
    public void beforeEnterpriseLicenseCheck(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (Objects.nonNull(license) && license.isEnterprise()) {
            return;
        }
        throw new RuntimeException("license invalid or expired");
    }

}
