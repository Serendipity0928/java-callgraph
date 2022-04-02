package gr.gousiosg.javacg.stat;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.MethodGen;

import java.io.IOException;

public class BCELNote {

    public static void main(String[] args) throws IOException {

        /**
         * ClassParser是Java Class文件的包装类。通过parse函数，解析为JavaClass【常量、方法等】返回。
         * 文档：https://commons.apache.org/proper/commons-bcel/apidocs/index.html
         */
        String zip_file = "";
        String file_name = "";
        ClassParser cp = new ClassParser(zip_file,file_name);

        /**
         * ConstantPoolGen这个用于构建动态常量池，即可以手动修改常量池
         * 文档：https://commons.apache.org/proper/commons-bcel/apidocs/index.html
         */
        JavaClass jClass = cp.parse();
        ConstantPoolGen constants = new ConstantPoolGen(jClass.getConstantPool());

        /**
         * MethodGen是构建动态方法的模板类。
         * 文档：https://commons.apache.org/proper/commons-bcel/apidocs/index.html
         */
        MethodGen mg = new MethodGen(jClass.getMethods()[0], jClass.getClassName(), constants);
        /**
         * InstructionList是一个指令集合的容器类。其包装在InstructionHandles对象中，InstructionHandles采用的是链表结构，核心指令类为Instruction。
         * 文档：https://commons.apache.org/proper/commons-bcel/apidocs/index.html
         */
        InstructionList instructionList = mg.getInstructionList();

        /**
         * springboot结合vue实现文件上传和下载
         * https://blog.csdn.net/q961250375/article/details/108810241
         */


    }


}
