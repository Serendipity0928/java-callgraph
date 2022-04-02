package gr.gousiosg.javacg;

import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.*;

public class SPLTest {

    @SuppressWarnings("UnstableApiUsage")
    protected static Queue<String> keyCandidate4ConfigUtil = EvictingQueue.create(2);

    public static void main(String[] args) {
        keyCandidate4ConfigUtil.add("spl");
        keyCandidate4ConfigUtil.add("fwj");
        System.out.println(keyCandidate4ConfigUtil);

        System.out.println(keyCandidate4ConfigUtil.peek());
//        System.out.println(keyCandidate4ConfigUtil.poll());

        System.out.println(keyCandidate4ConfigUtil);


    }

    @Test
    public void mapTest() {
        HashMap<String, List<String>> shm = new HashMap<>();

        shm.putIfAbsent("spl", Lists.newArrayList());
        shm.get("spl").add("fwj");
        shm.get("spl").add("928");

        System.out.println(shm.get("spl"));

    }

}
