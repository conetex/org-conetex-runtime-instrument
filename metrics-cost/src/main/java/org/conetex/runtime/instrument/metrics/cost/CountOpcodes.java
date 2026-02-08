package org.conetex.runtime.instrument.metrics.cost;

public class CountOpcodes {

    public static void consume(String counterName, int opcode) {
        System.out.println("consumed " + counterName + " " + opcode);
    }

}
