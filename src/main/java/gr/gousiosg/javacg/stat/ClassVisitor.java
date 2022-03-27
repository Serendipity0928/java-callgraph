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

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.MethodGen;

import java.util.ArrayList;
import java.util.List;

/**
 * The simplest of class visitors, invokes the method visitor class for each
 * method found.
 * 最简单的类访问器，用于为每个发现的方法调用方法访问器。
 * 该类继承自EmptyVisitor，这个是由BCEL提供的空方法体的访问器主类，其实现Visitor接口。---访问器模式
 * EmptyVisitor文档地址：https://commons.apache.org/proper/commons-bcel/apidocs/index.html
 * Visitor文档地址：https://commons.apache.org/proper/commons-bcel/apidocs/index.html
 */
public class ClassVisitor extends EmptyVisitor {

    private JavaClass clazz;                    // class文件解析类
    private ConstantPoolGen constants;          // 构建动态类常量池
    private String classReferenceFormat;        // 输出类引用字符格式串(以C开头)
    private final DynamicCallManager DCManager = new DynamicCallManager();      //
    private List<String> methodCalls = new ArrayList<>();                       // 存储类中的方法调用链

    public ClassVisitor(JavaClass jc) {
        clazz = jc;
        constants = new ConstantPoolGen(clazz.getConstantPool());
        classReferenceFormat = "C:" + clazz.getClassName() + " %s";
    }

    public void visitJavaClass(JavaClass jc) {
        // 访问器模式，此处即是调用监听器的visitConstantPool方法,输出当前类引用的其他类
        jc.getConstantPool().accept(this);
        // 获取到当前类中所有的方法，然后进行遍历获取当前类的方法引用的其他方法
        Method[] methods = jc.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            DCManager.retrieveCalls(method, jc);
            DCManager.linkCalls(method);
            // 访问器模式，此处是调用监听器的visitMethod方法，输出当前方法引用的其他方法
            method.accept(this);

        }
    }

    public void visitConstantPool(ConstantPool constantPool) {
        // 遍历JavaClass的常量池
        for (int i = 0; i < constantPool.getLength(); i++) {
            Constant constant = constantPool.getConstant(i);
            // 在解析常量池的时候 BCEL是用1开始插入的
            if (constant == null)
                continue;
            /**
             * 过滤出表示类的常量池符号；即打印出当前类中引用的其他类
             * 常量池的tag：https://hllvm-group.iteye.com/group/topic/38367 （RednaxelaFX）
             */
            if (constant.getTag() == 7) {
                String referencedClass = 
                    constantPool.constantToString(constant);
                System.out.println(String.format(classReferenceFormat, referencedClass));
            }
        }
    }

    public void visitMethod(Method method) {
        // 将当前方法封装为一个动态方法类
        MethodGen mg = new MethodGen(method, clazz.getClassName(), constants);
        // 构建自定义动态方法访问类
        MethodVisitor visitor = new MethodVisitor(mg, clazz);
        // 将当前方法的所有调用关系集合添加到当前类的调用关系集合中
        methodCalls.addAll(visitor.start());
    }

    public ClassVisitor start() {
        visitJavaClass(clazz);
        return this;
    }

    public List<String> methodCalls() {
        return this.methodCalls;
    }
}
