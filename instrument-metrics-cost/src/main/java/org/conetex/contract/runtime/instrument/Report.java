package org.conetex.contract.runtime.instrument;

import org.conetex.contract.runtime.instrument.interfaces.Counter;
import org.conetex.contract.runtime.instrument.interfaces.RetransformingClassFileTransformer;

public class Report {

    public static void main(String[] args){
        long c = Long.MAX_VALUE;
        System.out.println("max:  " + Long.MAX_VALUE);
        double w = 0.05d;
        double resD = (c * w);
        long resL1 = (long) resD;
        long resL2 = (long) (c * w);
        System.out.println("resD:  " + resD);
        System.out.println("resL1: " + resL1);
        System.out.println("resL2: " + resL2);

        long wl = 20;
        long resL3 = c / wl;
        System.out.println("resL3: " + resL3);
    }

    public static long calculateWeightedAverage(Counter[] counters, int[] weights) {
        long weightedAvr = 0;  // Accumulate the weighted sums as a double to preserve precision
        for (int i = 0; i < counters.length; i++) {
            if(counters[i] != null) {
                            // <-- average ----------------------------->   <-- weighted -->
                weightedAvr += (counters[i].getCount() / counters.length) / weights[i]      ;
            }
        }
        return weightedAvr;
    }

}
