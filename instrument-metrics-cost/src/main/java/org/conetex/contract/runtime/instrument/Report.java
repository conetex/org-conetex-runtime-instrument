package org.conetex.contract.runtime.instrument;

import org.conetex.contract.runtime.instrument.interfaces.Counter;
import org.conetex.contract.runtime.instrument.interfaces.RetransformingClassFileTransformer;

import java.text.DecimalFormat;

import static org.conetex.contract.runtime.instrument.counter.AbstractCounter.*;

public class Report {



    public static void main(String[] args) {
        System.out.println(" Lmin   : " + Long.MIN_VALUE);
        System.out.println(" Lmax   : " + Long.MAX_VALUE);
        System.out.println(" min   : " + MIN_VALUE);
        System.out.println(" max   : " + MAX_VALUE);
        Long[] counters = {
                null, // ArithmeticAddSubNeg
                1L, // ArithmeticDivRem // 10% = -1,013,556,353,129,971,313
                null, // ArithmeticMul
                null, // ArrayLoad
                null, // ArrayNew
                null, // ArrayStore
                null, // CompareInt
                null, // CompareLong
                null, // CompareObject
                null, // ExceptionThrow
                null, // FieldLoad
                null, // FieldStore
                null, // Jump
                null, // MethodCall
                null, // MethodEntry
                null, // Monitor
                null, // VariableLoad
                null, // VariableStore
                null  // TypeCheck
        };
        int[] weights = {
                35165, // ArithmeticAddSubNeg
                109890, // ArithmeticDivRem
                43956, // ArithmeticMul

                48352, // ArrayLoad
                98901, // ArrayNew
                65934, // ArrayStore

                30769, // CompareInt
                39560, // CompareLong
                43956, // CompareObject

                109890, // ExceptionThrow

                52747, // FieldLoad
                61539, // FieldStore

                28571, // Jump

                54945, // MethodCall
                0, // MethodEntry

                76923, // Monitor

                50550, // VariableLoad
                21978, // VariableStore

                26374  // TypeCheck
        };
        long min = Long.MIN_VALUE;
        //long[] counters = {Long.MIN_VALUE, Long.MAX_VALUE};
        //long[] counters = {-6,6};
        //long[] counters = {Long.MIN_VALUE/2, Long.MIN_VALUE};
        //Long[] counters = { -9L, -10L };
        //long[] counters = {Long.MAX_VALUE, Long.MAX_VALUE/2};

        //int[] weights = { 750000, 250000};
        //int[] weights = { 750000, 250000};
        //int[] weights = { 500000, 500000 };
        //int[] weights = {  500000 };

        //int base =        2;

        long res = xcalculateWeightedAverage(counters, weights, Long.MIN_VALUE);
        // -4611686018427387904
        // -4611686018427000000
        System.out.println(min);
        System.out.println(res + " <-----");
        System.out.println(res - min);
    }

    public static void mainXXXXXX(String[] args) {
        long c1 = 1;//Long.MAX_VALUE;
        long c2 = -3;//Long.MIN_VALUE;
        System.out.println(xweightedAverage(c1, c2));
    }

    public static long xweightedAverage(long counter1, long counter2) {
        // Achtung: Multiplikation kann überlaufen!
        // Lösung: mit long rechnen, aber Division frühzeitig einbauen
        long part1 = (counter1 / 10) * 5; // zuerst durch 10 teilen, dann multiplizieren
        long part2 = (counter2 / 10) * 5;

        // Restkorrektur: damit wir nicht zu viel verlieren
        long remainder = (counter1 % 10) * 5 + (counter2 % 10) * 5;
        long correction = remainder / 10;

        return part1 + part2 + correction;
    }

    public static void mainXXXXX(String[] args){
        System.out.println("unsigned = " + Long.toUnsignedString(0));

        double weight1 = 0.499999;
        double weight2 = 0.499999;

        long resS = Long.MIN_VALUE;

        System.out.println(" min   : " + Long.MIN_VALUE);
        System.out.println(" start : " + resS);
        System.out.println(" max   : " + Long.MAX_VALUE);

        long c1 = 0;//Long.MAX_VALUE; // 9;
        long c2 = 0;//Long.MAX_VALUE; // 9;

        System.out.println("  c1   : " + c1);
        System.out.println("  c2   : " + c2);

        long res1 = (long) (c1 * weight1) ;
        long res2 = (long) (c2 * weight2) ;

        //4,611,686,018,427,387,903.5 4611686018427387903.5 (expected Long.MAX_VALUE * 0.5)
        //4,611,676,795,055,351,048.7 4611676795055351048.7 (expected Long.MAX_VALUE * 0.499999)
        // 4611676795055351296
        System.out.println("res1   : " + res1);
        System.out.println("res2   : " + res2);

        long res = resS;
        System.out.println("res...:  " + res);

        long resNew = res + res1;
        if(res > resNew){
            System.out.println("ueberlauf 1: " + res + " > " + resNew);
            resNew = Long.MAX_VALUE;
        }
        res = resNew;
        System.out.println("res..1:  " + res);

        resNew = res + res2;
        if(res > resNew){
            System.out.println("ueberlauf 2: " + res + " > " + resNew);
            resNew = Long.MAX_VALUE;
        }
        res = resNew;
        System.out.println("res..2:  " + res);

        System.out.println("-- end --");

    }

    public static void mainXXXX(String[] args){
                       //0.035165
        double weight1 = 0.499999;
        double weight2 = 0.499999;

        long resS = 0;

        System.out.println("   m   : " + Long.MAX_VALUE);

        long c1 = Long.MAX_VALUE; // 9;
        long c2 = Long.MAX_VALUE; // 9;

        System.out.println("  c1   : " + c1);
        System.out.println("  c2   : " + c2);

        double resD1 = (c1 * weight1);
        double resD2 = Math.nextDown( (c2 * weight2) );

        System.out.println("resD1  : " + resD1);
        System.out.printf( "resD1f : %.2f%n", resD1);
        System.out.println("resD1df: " + (new DecimalFormat("#.###")).format(resD1) );

        System.out.println("resD2  : " + resD2);
        System.out.printf( "resD2f  : %.2f%n", resD2);
        System.out.println("resD2df: " + (new DecimalFormat("#.###")).format(resD2) );

        long res1 = (long) (c1 * weight1) ;
        long res2 = (long) (c2 * weight2) ;

        //4,611,686,018,427,387,903.5 4611686018427387903.5 (expected Long.MAX_VALUE * 0.5)
        //4,611,676,795,055,351,048.7 4611676795055351048.7 (expected Long.MAX_VALUE * 0.499999)
                                   // 4611676795055351296
        System.out.println("res1   : " + res1);
        System.out.println("res2   : " + res2);

        System.out.println("res1f  : " + (long) Math.floor(c1 * weight1));
        System.out.println("res2f  : " + (long) Math.floor(c2 * weight2));

        System.out.println("resA   :  " + (res1 + res2) );
        System.out.println("resA   :  " + (res1 * 2) );

        long res = resS;
        System.out.println("res...:  " + res);
        res = res + res1;
        System.out.println("res...:  " + res);
        res = res + res2;
        System.out.println("res   :  " + res);

        System.out.println("-- end --");

    }

    public static void mainXXX(String[] args){
        int baseI =       10;//  1000000;
        int weightI =      5;//   109890;

        double weightD = 0.5;

        long cl = Long.MAX_VALUE; // 9;

        long res1L = (cl / baseI);
        long res2L = (cl / baseI) * weightI;
        long res3L = cl / baseI * weightI;

        double resD1D = cl * weightD;
        long resD1L = (long) (cl * weightD);

        System.out.println("-- end --");


    }

    public static void mainXX(String[] args){
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

        Counter[] counters = transformer.getCounters();
        result[0] = calculateWeightedAverage(counters, weights);
        counters = CounterCounter.countCounters(counters);

        int i = 0;
        while (counters != null) {
            // increase result
            long[] newResult = new long[result.length + 1];
            System.arraycopy(result, 0, newResult, 1, result.length);
            result = newResult;

            // store result part
            result[0] = calculateWeightedAverage(counters, weights);

            /* todo debug fix
[0, -3, -5, 0, -3]

-16
[0, -2, -5, -3, -2]
0
[0, 2, 7, 13, 14] warum 0?    2 + 7 ist unplausible
            */

            // prepare next level
            counters = CounterCounter.countCounters(counters);
            i++;
        }
        return result;
    }

    private static long add(long a, long b) {
        long re = a + b;
        if(b > 0 && re < a){
            System.err.println("overflow: " + a + " > (" + a + " + " + b + " = " + re + ")");
            return MAX_VALUE;
        }
        if(b < 0 && re > a){
            System.err.println("underflow: " + a + " < (" + a + " + " + b + " = " + re + ")");
            return MIN_VALUE;
        }
        return re;
    }

    public static long multiply(long a, long b) {
        // Schnelle Fälle
        if (a == 0 || b == 0) {
            return 0;
        }

        long result = a * b;

        boolean expectedPositive = (a > 0 && b > 0) || (a < 0 && b < 0);

        if (expectedPositive && result < 0) {
            System.err.println("overflow: " + result + " != " + a + " * " + b);
            return MAX_VALUE;
        }
        if (!expectedPositive && result > 0) {
            System.err.println("underflow: " + result + " != " + a + " * " + b);
            return MIN_VALUE;
        }

        return result;
    }

    private static long calculateWeightedAverage(Counter[] counters, int[] weights) {
        int weightsSum = 0;
        for (int weight : weights) {
            weightsSum += weight;
        }
        long weightedAvr = 0;
        long remainder = 0;
        for (int i = 0; i < counters.length; i++) {
            long count = MIN_VALUE;
            if(counters[i] != null) {
                count = counters[i].getCount();
            }
            // multiplication may overflow, so we apply division early and we use method multiply that catches this.
            // addition may also overflow if weightSum is not correct, so we use method add that catches this.
            weightedAvr = add(weightedAvr, multiply((count / weightsSum), weights[i])); // weightedAvr += (count / weightsSum) * weights[i];
            remainder = add(remainder, multiply((count % weightsSum), weights[i]));     // remainder += (count % weightsSum) * weights[i];
        }
        long correction = remainder / weightsSum;
        weightedAvr = add(weightedAvr, correction);
        return weightedAvr;
    }

    private static long xcalculateWeightedAverage(Long[] counters, int[] weights, long minvalue) {
        int weightsSum = 0;
        for (int weight : weights) {
            weightsSum += weight;
        }
        long weightedAvr = 0;
        long remainder = 0;
        for (int i = 0; i < counters.length; i++) {
            long count = minvalue;
            if(counters[i] != null) {
                count = counters[i];
            }
            // multiplication may overflow, so we apply division early and we use method multiply that catches this.
            // addition may also overflow if weightSum is not correct, so we use method add that catches this.
            weightedAvr = add(weightedAvr, multiply((count / weightsSum), weights[i])); // weightedAvr += (count / weightsSum) * weights[i];
            remainder = add(remainder, multiply(count % weightsSum, weights[i]));     // remainder += (count % weightsSum) * weights[i];
        }
        long correction = remainder / weightsSum;
        weightedAvr = add(weightedAvr, correction);
        return weightedAvr;
    }

}
