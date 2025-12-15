package org.conetex.contract.runtime.instrument;

import org.conetex.contract.runtime.instrument.interfaces.Counter;
import org.conetex.contract.runtime.instrument.interfaces.RetransformingClassFileTransformer;

public class Report {

    // check for arithmetic TODO delete it
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

    public static long[] calculateTotalCost(RetransformingClassFileTransformer transformer) {
        long[] result = new long[0];
        int[] weights = transformer.getCounterWeights();

        Counter[] counters = transformer.getCounters();
        int i = 0;
        while (counters != null) {
            // increase result
            long[] newResult = new long[result.length + 1];
            System.arraycopy(result, 0, newResult, 0, result.length);
            result = newResult;

            // store result part
            result[i] = calculateWeightedAverage(counters, weights);

            // prepare next level
            counters = CounterCounter.countCounters(counters);
            i++;
        }
        return result;
    }

    private static long calculateWeightedAverage(Counter[] counters, int[] weights) {
        long weightedAvr = 0;
        for (int i = 0; i < counters.length; i++) {
            if(counters[i] != null) {
                            // <-- weight ------------------------->   <-- average -->
                weightedAvr += (counters[i].getCount() / weights[i]) / counters.length;
            }
        }
        return weightedAvr;
    }

}
