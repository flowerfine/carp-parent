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

import cn.sliew.carp.framework.common.reflection.lambdas.JobRunrJob;
import cn.sliew.carp.framework.common.util.reflection.ReflectionUtils;
import cn.sliew.milky.common.exception.Rethrower;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Optional;

public class KotlinJobDetailsFinder extends AbstractJobDetailsFinder {

    private static final String INVOKE = "invoke";
    private static final String ACCEPT = "accept";
    private static final String RUN = "run";

    private enum KotlinVersion {
        ONE_FOUR,
        ONE_FIVE,
        ONE_SIX
    }

    private final JobRunrJob jobRunrJob;
    private int methodCounter = 0;
    private KotlinVersion kotlinVersion;

    private String nestedKotlinClassWithMethodReference;

    KotlinJobDetailsFinder(JobRunrJob jobRunrJob, Object... params) {
        super(new KotlinJobDetailsBuilder(jobRunrJob, params));
        this.jobRunrJob = jobRunrJob;
        try (InputStream classContainingLambdaInputStream = getClassContainingLambdaAsInputStream()) {
            parse(classContainingLambdaInputStream);
        } catch (IOException e) {
            Rethrower.throwAs(e);
        }
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return new AnnotationVisitor(Opcodes.ASM7) {
            @Override
            public void visit(String name, Object value) {
                if ("mv".equals(name)) {
                    int[] version = ReflectionUtils.cast(value);
                    if (version[0] == 1 && version[1] == 4) {
                        kotlinVersion = KotlinVersion.ONE_FOUR;
                    } else if (version[0] == 1 && version[1] == 5) {
                        kotlinVersion = KotlinVersion.ONE_FIVE;
                    } else if (version[0] == 1 && version[1] == 6) {
                        kotlinVersion = KotlinVersion.ONE_SIX;
                    } else {
                        throw new UnsupportedOperationException("The Kotlin version " + version[0] + "." + version[1] + " is unsupported");
                    }
                }
            }
        };
    }

    @Override
    protected boolean isLambdaContainingJobDetails(String name) {
        if (name.equals(ACCEPT) || name.equals(INVOKE)) {
            methodCounter++;
        }
        if (KotlinVersion.ONE_FOUR.equals(kotlinVersion)) {
            return name.equals(RUN) || ((name.equals(ACCEPT) || name.equals(INVOKE)) && methodCounter == 2);
        } else {
            return name.equals(RUN) || ((name.equals(ACCEPT) || name.equals(INVOKE)) && methodCounter == 1);
        }
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (access == 0x1018 || access == 0x1000) {
            this.nestedKotlinClassWithMethodReference = name;
        }
    }

    @Override
    protected InputStream getClassContainingLambdaAsInputStream() {
        return JobDetailsGeneratorUtils.getKotlinClassContainingLambdaAsInputStream(jobRunrJob);
    }

    @Override
    protected void parse(InputStream inputStream) throws IOException {
        Optional<Field> field = ReflectionUtils.findField(jobRunrJob.getClass(), "function");
        if (field.isPresent()) {
            getJobDetailsFromKotlinFunction(field.get());
        } else {
            super.parse(inputStream);
            parseNestedClassIfItIsAMethodReference();
        }
    }

    private void getJobDetailsFromKotlinFunction(Field field) {
        Object function = ReflectionUtils.getValueFromField(field, jobRunrJob);
        Field receiver = ReflectionUtils.getField(function.getClass(), "receiver");
        Field name = ReflectionUtils.getField(function.getClass(), "name");
        Class<?> receiverClass = ReflectionUtils.getValueFromField(receiver, function).getClass();
        String methodName = ReflectionUtils.cast(ReflectionUtils.getValueFromField(name, function));
        jobDetailsBuilder.setClassName(receiverClass.getName());
        jobDetailsBuilder.setMethodName(methodName);
    }

    private void parseNestedClassIfItIsAMethodReference() throws IOException {
        boolean isNestedKotlinClassWithMethodReference = nestedKotlinClassWithMethodReference != null
                && !JobDetailsGeneratorUtils.toFQResource(jobRunrJob.getClass().getName()).equals(nestedKotlinClassWithMethodReference);

        if (isNestedKotlinClassWithMethodReference) {
            String location = "/" + nestedKotlinClassWithMethodReference + ".class";
            try (InputStream inputStream = jobRunrJob.getClass().getResourceAsStream(location)) {
                super.parse(inputStream);
                while (jobDetailsBuilder.getInstructions().size() > 1) {
                    jobDetailsBuilder.pollFirstInstruction();
                }
            }
        }
    }

}
