package org.conetex.contract.runtime.instrument;

import org.conetex.contract.runtime.instrument.interfaces.Counter;
import org.conetex.contract.runtime.instrument.interfaces.RetransformingClassFileTransformer;

public class Report {

    public static void main(String[] args){
        int baseI =     2;//  1000000;
        int weightI =    50;//   109890;

        String unsigned4Str2 = Long.toUnsignedString(Long.MIN_VALUE);
        String unsigned4Str3 = Long.toUnsignedString(Long.MAX_VALUE);

        long unsigned4 = Long.MIN_VALUE + 4;
        String unsigned4Str = Long.toUnsignedString(unsigned4);

        long res1 = (unsigned4 / baseI);
        String res1uStr = Long.toUnsignedString(res1);

        long res1b = Long.divideUnsigned(unsigned4, baseI);
        String res1buStr = Long.toUnsignedString(res1b);

        long unsignedp4 = Long.MAX_VALUE - 4;
        String unsignedp4Str = Long.toUnsignedString(unsignedp4);

        long res2 = (unsigned4 / baseI);
        String res2uStr = Long.toUnsignedString(res2);

        double res2s = ((unsigned4 * 1.0) / baseI);
        long resL1 = (unsigned4 / baseI) * weightI;

    }

    // check for arithmetic TODO delete it
    public static void mainX(String[] args){
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

        int pi = 100;
        int wi = 5;
        long resL4 = (c / pi) * wi;
        System.out.println("resL4: " + resL4);

        int bi =       1000000;//  1000000;
        int w2i    =    109890;//   109890;
                   // 0.109890
        double w2d = 0.109890;// 0.5;
        int resI5 = ( w2i / bi );
        double resD5 = ( w2i / bi );
        double resD5b = ( (w2i * 1d) / (bi * 1d) );
        long resL5a = c * ( w2i / bi );
        long resL5b = c * w2i / bi;
        double resL5c = c * w2d;
        long resL7 = w2i / bi;
        long resL5d3 = c * (w2i / bi);
       // long resL5d4 = c / (bi * w2i);

        long resL5d1 =  c / bi  * w2i;
        long resL5d2 = (c / bi) * w2i;

        System.out.println("resL5: " + resL5a);

    }

    public static long[] calculateTotalCost(RetransformingClassFileTransformer transformer) {
        long[] result = new long[1];
        int[] weights = transformer.getCounterWeights();
        int base = transformer.getCounterWeightsBase();

        Counter[] counters = transformer.getCounters();
        result[0] = calculateWeightedAverage(counters, weights, base);
        counters = CounterCounter.countCounters(counters);

        int i = 0;
        while (counters != null) {
            // increase result
            long[] newResult = new long[result.length + 1];
            System.arraycopy(result, 0, newResult, 1, result.length);
            result = newResult;

            // store result part
            result[0] = calculateWeightedAverage(counters, weights, base);

            // prepare next level
            counters = CounterCounter.countCounters(counters);
            i++;
        }
        return result;
    }

    private static long calculateWeightedAverage(Counter[] counters, int[] weights, int base) {
        long weightedAvr = 0;
        for (int i = 0; i < counters.length; i++) {
            if(counters[i] != null) {
                            // <-- weight ------------------------->   <-- average -->
                long c = counters[i].getCount();
                int w = weights[i];
                int l = counters.length;
                long a = c / w;
                long b = a / l;
                Long.divideUnsigned(a, l);
                //Long..mu.divideUnsigned(a, l);
                weightedAvr += (counters[i].getCount() / base) * weights[i];
            }
        }
        return weightedAvr;
    }

}
