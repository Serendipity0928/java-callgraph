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

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.bcel.classfile.ClassParser;

/**
 * Constructs a callgraph out of a JAR archive. Can combine multiple archives
 * into a single call graph.
 *
 * @author Georgios Gousios <gousiosg@gmail.com>
 */
public class JCallBoot {

    public static void main(String[] args) {

        System.out.println("Starting...");

        // 将ClassParser包装类解析的JavaClass类结升级为类访问器ClassVisitor
        Function<ClassParser, ClassVisitor> getClassVisitor =
                (ClassParser cp) -> {
                    try {
                        return new ClassVisitor(cp.parse());
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                };

        try {
            for (String arg : args) {

                File f = new File(arg);

                if (!f.exists()) {
                    System.err.println("Jar file " + arg + " does not exist");
                }

                try (JarFile jar = new JarFile(f)) {
                    // 将从jar中产生的文件迭代器转换为stream流
                    Stream<JarEntry> entries = enumerationAsStream(jar.entries());

                    // 为方便debug，先转换为List，后续删除
                    List<JarEntry> jarEntryList = Collections.list(jar.entries());
                    for (JarEntry jarEntry : jarEntryList) {
                        if(jarEntry.isDirectory() || !jarEntry.getName().endsWith(".class")) {
                            // 对于jar的文件夹、文件名不以".class"结尾的文件过滤。如META_INF文件(jar描述文件)
                            continue;
                        }

                        // class文件的包装类。做了什么事情？
                        ClassParser cp = new ClassParser(arg, jarEntry.getName());
                        // 将class解析类JavaClass转换为类监听器
                        ClassVisitor cv = getClassVisitor.apply(cp);
                        // 类访问器 开始初始化
                        cv.start();
//                        System.out.println(cv.methodCalls());


                    }



//                    String methodCalls = entries.
//                            flatMap(e -> {
//                                if (e.isDirectory() || !e.getName().endsWith(".class"))
//                                    return (new ArrayList<String>()).stream();
//
//                                ClassParser cp = new ClassParser(arg, e.getName());
//                                return getClassVisitor.apply(cp).start().methodCalls().stream();
//                            }).
//                            map(s -> s + "\n").
//                            reduce(new StringBuilder(),
//                                    StringBuilder::append,
//                                    StringBuilder::append).toString();
//
//                    BufferedWriter log = new BufferedWriter(new OutputStreamWriter(System.out));
//                    log.write(methodCalls);
//                    log.close();
                }
            }
        } catch (IOException e) {
            System.err.println("Error while processing jar: " + e.getMessage());
            e.printStackTrace();
        }

//        JMethodCallCore.test();

    }

    public static <T> Stream<T> enumerationAsStream(Enumeration<T> e) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new Iterator<T>() {
                            public T next() {
                                return e.nextElement();
                            }

                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }
                        },
                        Spliterator.ORDERED), false);
    }
}
