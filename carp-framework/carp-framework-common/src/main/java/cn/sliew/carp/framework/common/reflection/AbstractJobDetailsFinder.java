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

import cn.sliew.carp.framework.common.reflection.instructions.*;
import cn.sliew.carp.framework.common.reflection.lambdas.JobRunrJob;
import cn.sliew.carp.framework.common.util.reflection.JarUtils;
import org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;

abstract class AbstractJobDetailsFinder extends ClassVisitor {

    protected final JobDetailsBuilder jobDetailsBuilder;

    protected AbstractJobDetailsFinder(JobDetailsBuilder jobDetailsBuilder) {
        super(Opcodes.ASM7);
        this.jobDetailsBuilder = jobDetailsBuilder;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (isLambdaContainingJobDetails(name)) {
            return new MethodVisitor(Opcodes.ASM7) {

                @Override
                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                    VisitFieldInstruction instruction = AllJVMInstructions.get(opcode, jobDetailsBuilder);
                    instruction.load(owner, name, descriptor);
                }

                @Override
                public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
                    InvokeDynamicInstruction instruction = AllJVMInstructions.get(Opcodes.INVOKEDYNAMIC, jobDetailsBuilder);
                    instruction.load(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
                }

                @Override
                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                    VisitMethodInstruction visitMethodInstruction = AllJVMInstructions.get(opcode, jobDetailsBuilder);
                    visitMethodInstruction.load(owner, name, descriptor, isInterface);
                }

                @Override
                public void visitInsn(int opcode) {
                    ZeroOperandInstruction zeroOperandInstruction = AllJVMInstructions.get(opcode, jobDetailsBuilder);
                    zeroOperandInstruction.load();
                }

                @Override
                public void visitVarInsn(int opcode, int variable) {
                    VisitLocalVariableInstruction instruction = AllJVMInstructions.get(opcode, jobDetailsBuilder);
                    instruction.load(variable);
                }

                @Override
                public void visitIntInsn(int opcode, int operand) {
                    SingleIntOperandInstruction singleIntOperandInstruction = AllJVMInstructions.get(opcode, jobDetailsBuilder);
                    singleIntOperandInstruction.load(operand);
                }

                @Override
                public void visitLdcInsn(Object value) {
                    LdcInstruction ldcInstruction = AllJVMInstructions.get(Opcodes.LDC, jobDetailsBuilder);
                    ldcInstruction.load(value);
                }

                @Override
                public void visitTypeInsn(int opcode, String type) {
                    VisitTypeInstruction instruction = AllJVMInstructions.get(opcode, jobDetailsBuilder);
                    instruction.load(type);
                }
            };
        } else {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
    }

    protected abstract boolean isLambdaContainingJobDetails(String name);

    protected abstract InputStream getClassContainingLambdaAsInputStream();

    public JobDetails getJobDetails() {
        return jobDetailsBuilder.getJobDetails();
    }

    protected void parse(InputStream inputStream) throws IOException {
        try {
            ClassReader parser = new ClassReader(inputStream);
            parser.accept(this, ClassReader.SKIP_FRAMES);
        } catch (IllegalArgumentException e) {
            if(e.getMessage().startsWith("Unsupported class file")) {
                String requiredAsmVersion = JarUtils.getManifestAttributeValue(JobRunrJob.class, "Minimum-ASM-Version");
                String actualAsmVersion = JarUtils.getVersion(Opcodes.class);
                throw new IllegalArgumentException("JobRunr needs (and automatically adds) ASM " + requiredAsmVersion + " as a transitive dependency but you have ASM " + actualAsmVersion + " on the classpath.", e);
            }
            throw e;
        }
    }

}
