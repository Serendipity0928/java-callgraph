/*
 * Copyright (c) 2011 - Georgios Gousios <gousiosg@gmail.com>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package gr.gousiosg.javacg.stat;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.*;

import java.util.*;

/**
 * The simplest of method visitors, prints any invoked method
 * signature for all method invocations.
 * 最简单的方法访问器，用于打印所有被调用的方法签名
 * 
 * Class copied with modifications from CJKM: http://www.spinellis.gr/sw/ckjm/
 */
public class MethodVisitor extends JMethodCallCore {

    private final HashSet<String> mccConfigUtilMethod = new HashSet<String>(){{
        add("getBoolean");add("getInt");add("getLong");add("getString");add("getStringArray");
    }};

    private int ldcConsecutiveNum = 0;

    public MethodVisitor(MethodGen methodGen, JavaClass visitedClass) {
        super(methodGen, visitedClass);
    }

    /**
     * 方法参数列表
     */
    private String argumentList(Type[] arguments) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arguments.length; i++) {
            if (i != 0) {
                sb.append(",");
            }
            sb.append(arguments[i].toString());
        }
        return sb.toString();
    }

    public List<String> start() {
        // 抽象方法或native方法没有方法体，直接返回空集合
        if (methodGen.isAbstract() || methodGen.isNative())
            return Collections.emptyList();

//        String name = methodGen.getName();
//        System.out.println(name);


        for (InstructionHandle ih = methodGen.getInstructionList().getStart();
                ih != null; ih = ih.getNext()) {
            Instruction i = ih.getInstruction();

            // 找到不属于InstructionConst || 属于ConstantPushInstruction || 属于ReturnInstruction的指令集
            if (!visitInstruction(i))
                i.accept(this);

            if(!(i instanceof LDC)) {
                ldcConsecutiveNum = 0;
            }
        }
        return methodCalls;
    }

    private boolean visitInstruction(Instruction i) {
        short opcode = i.getOpcode();
        return ((InstructionConst.getInstruction(opcode) != null)
                && !(i instanceof ConstantPushInstruction)
                && !(i instanceof ReturnInstruction));
    }

    @Override
    public void visitINVOKEVIRTUAL(INVOKEVIRTUAL invokevirtual) {
        methodCalls.add(String.format(format,"M", invokevirtual.getReferenceType(constantPoolGen),
                invokevirtual.getMethodName(constantPoolGen),argumentList(invokevirtual.getArgumentTypes(constantPoolGen))));
        addMethodCallCache(invokevirtual);
    }

    @Override
    public void visitINVOKEINTERFACE(INVOKEINTERFACE invokeinterface) {
        methodCalls.add(String.format(format,"I", invokeinterface.getReferenceType(constantPoolGen),
                invokeinterface.getMethodName(constantPoolGen),argumentList(invokeinterface.getArgumentTypes(constantPoolGen))));
        addMethodCallCache(invokeinterface);
    }

    @Override
    public void visitINVOKESPECIAL(INVOKESPECIAL invokespecial) {
        methodCalls.add(String.format(format,"O", invokespecial.getReferenceType(constantPoolGen),
                invokespecial.getMethodName(constantPoolGen),argumentList(invokespecial.getArgumentTypes(constantPoolGen))));
        addMethodCallCache(invokespecial);
    }

    @Override
    public void visitINVOKESTATIC(INVOKESTATIC invokestatic) {
        String calleeClassName = invokestatic.getReferenceType(constantPoolGen).toString();
        String methodName = invokestatic.getMethodName(constantPoolGen);
        methodCalls.add(String.format(format,"S", calleeClassName, methodName,
                argumentList(invokestatic.getArgumentTypes(constantPoolGen))));

        addMethodCallCache(invokestatic);

        // 检查是否为 com.sankuai.meituan.util.ConfigUtilAdapter.getXXX static方法
        if(calleeClassName.equals("com.sankuai.meituan.util.ConfigUtilAdapter") && mccConfigUtilMethod.contains(methodName)) {
            recordKeyAndMethodSignature(invokestatic, calleeClassName, methodName, ldcConsecutiveNum);
        }

    }

    @Override
    public void visitINVOKEDYNAMIC(INVOKEDYNAMIC invokedynamic) {
        methodCalls.add(String.format(format,"D", invokedynamic.getType(constantPoolGen),
                invokedynamic.getMethodName(constantPoolGen), argumentList(invokedynamic.getArgumentTypes(constantPoolGen))));
        addMethodCallCache(invokedynamic);
    }

    @Override
    public void visitLDC(LDC ldc) {
        /**
         * ①：调用MCC配置时，会调用ConfigUtilAdapter的几个静态方法，即GetXXX()。可枚举方法名称
         * ②：注意到，在业务调用方法时，需要注意有的GET方法可能只需要一个参数，有的需要两个，这就意味着需要多次ldc命令从常量池中获取字符串，注意能够区分出key值
         * ③：静态方法对应字节码指令 static
         * ④：todo: 接口多态、lambda、stream、多线程等可能会导致链路丢失
         * 相关资料：https://www.cnblogs.com/tenghoo/p/jvm_opcodejvm.html、https://www.zhihu.com/question/296143618
         */

        if(ldc.getType(constantPoolGen) == Type.STRING) {
            updateKeyCandidateLDC(ldc.getValue(constantPoolGen).toString());
            ldcConsecutiveNum++;
            return;
        }
        ldcConsecutiveNum = 0;
    }



}
