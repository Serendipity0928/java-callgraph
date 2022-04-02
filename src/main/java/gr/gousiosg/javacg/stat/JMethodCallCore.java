package gr.gousiosg.javacg.stat;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.generic.*;

import java.util.*;
import java.util.stream.Collectors;

public class JMethodCallCore extends EmptyVisitor {

    protected JavaClass visitedClass;         // 当前类信息
    protected MethodGen methodGen;           // 构建的动态方法
    protected ConstantPoolGen constantPoolGen;     // 构建的动态常量池，和类的动态常量池相同
    protected final String callMethodFullName;
    protected String format;          // 方法格式化字符串输出
    protected List<String> methodCalls = new ArrayList<>();   // 当前方法调用其他方法集合

    @SuppressWarnings("UnstableApiUsage")
    protected Queue<String> keyCandidate4ConfigUtil = EvictingQueue.create(2);

    public static Map<String, Set<String>> methodCallCache = new HashMap<>();
    public static Map<String, String> keyMethodCache = new HashMap<>();

    public JMethodCallCore(MethodGen methodGen, JavaClass visitedClass) {
        this.visitedClass = visitedClass;
        this.methodGen = methodGen;
        this.constantPoolGen = methodGen.getConstantPool();
        this.callMethodFullName = getMethodFullName(visitedClass.getClassName(), methodGen.getName(), methodGen.getArgumentTypes());
        this.format = "M:" + this.callMethodFullName;
    }


    public void addMethodCallCache(InvokeInstruction instruction) {
        String refClassName = instruction.getReferenceType(constantPoolGen).toString();
        if(instruction instanceof INVOKEDYNAMIC) {
            refClassName = instruction.getType(constantPoolGen).toString(); // dynamic区别
        }

        String calleeMethodName = getMethodFullName(refClassName, instruction.getMethodName(constantPoolGen),
                instruction.getArgumentTypes(constantPoolGen));
        methodCallCache.putIfAbsent(calleeMethodName, Sets.newHashSet());
        methodCallCache.get(calleeMethodName).add(callMethodFullName);

//        if(!callMethodFullName.equals("com.sankuai.meituan.tsp.stocksupply.boot.Boot:main(java.lang.String[])")
//                && instruction instanceof INVOKEVIRTUAL) {
//            System.out.println("debug");
//        }
    }

    public void updateKeyCandidateLDC(String keyCan) {
        keyCandidate4ConfigUtil.add(keyCan);
    }

    public void recordKeyAndMethodSignature(INVOKESTATIC invokestatic, String calleeClassName, String methodName, int ldcConsecutiveNum) {
        Type[] argumentTypes = invokestatic.getArgumentTypes(constantPoolGen);

        /**
         * 固定队列两种情况：①：[-,key]；②：[key,default]
         * 对于第①种情框，连续ldc指令肯定为1；对于第二种情况，连续字符串ldc指令为2
         */
        String LionKey = keyCandidate4ConfigUtil.poll();
        // 第一种情况太多，这里判断是否是第二种情况 | 注意候选key的池子大小限制
        if(ldcConsecutiveNum != 2 && keyCandidate4ConfigUtil.size() > 0) {
            LionKey = keyCandidate4ConfigUtil.peek();
        }


        // 直接调用ConfigUtilAdaptor的静态方法，key传入 String或(static final String 变量)
        keyMethodCache.putIfAbsent(LionKey, callMethodFullName);
    }


    public String getMethodFullName(String refClassName, String methodName, Type[] argumentTypes) {
        return refClassName + ":" + methodName + "(" +
                Arrays.stream(argumentTypes).map(Type::toString).collect(Collectors.joining(",")) + ")";
    }

    public static void test() {
        System.out.println(keyMethodCache.containsKey("wm_order_cancel_incr_strock_lock_switch")); // 2
        System.out.println(keyMethodCache.get("wm_order_cancel_incr_strock_lock_switch"));
        String stock_manage_region_max = keyMethodCache.get("wm_order_cancel_incr_strock_lock_switch");
        System.out.println("调用当前key的方法是" + stock_manage_region_max);
        System.out.println("下面是调用该方法的所有类");
        for (String s : methodCallCache.get(stock_manage_region_max)) {
            System.out.println(s);
            for (String s1 : methodCallCache.getOrDefault(s, Sets.newHashSet())) {
                System.out.println("  ++" + s1);
            }
        }

    }


}
