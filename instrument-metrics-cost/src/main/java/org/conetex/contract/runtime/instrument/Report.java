package org.conetex.contract.runtime.instrument;

import org.conetex.contract.runtime.instrument.interfaces.Counter;
import org.conetex.contract.runtime.instrument.interfaces.RetransformingClassFileTransformer;

import java.text.DecimalFormat;

import static org.conetex.contract.runtime.instrument.counter.AbstractCounter.*;

public class Report {

    public static void main(String[] args){

        // todo dies werden tests:
        int[] weightsPN       = {  5    ,  5  };

        Long[] positivDigits1  = {  8L   ,  5L };
        long[] resultP1  = x2calculateWeightedAverage(positivDigits1 , weightsPN);
        System.out.println(resultP1[0] + "." + resultP1[1] + " <-----    positivDigits == -1*");

        Long[] negativDigits1  = { -8L   , -5L };
        long[] resultN1  = x2calculateWeightedAverage(negativDigits1 , weightsPN);
        System.out.println(resultN1[0] + "." + resultN1[1] + " <-----    negativDigits");

        Long[] positivDigits0  = new Long[]{  8L   ,  11L };
        long[] resultP0  = x2calculateWeightedAverage(positivDigits0 , weightsPN);
        System.out.println(resultP0[0] + "." + resultP0[1] + " <-----    positivDigits  == -1*");

        Long[] negativDigits0  = new Long[]{ -8L   , -11L };
        long[] resultN0  = x2calculateWeightedAverage(negativDigits0 , weightsPN);
        System.out.println(resultN0[0] + "." + resultN0[1] + " <-----    negativDigits");


        Long[] positivDigits2  = {  8L   ,  8L-13L };
        long[] resultP2  = x2calculateWeightedAverage(positivDigits2 , weightsPN);
        System.out.println(resultP2[0] + "." + resultP2[1] + " <-----    positivDigits == 8-6.5 == " + ((8-13)+(13*0.5)));

        Long[] negativDigits2  = new Long[]{ -8L   , -8L+13L };
        long[] resultN2  = x2calculateWeightedAverage(negativDigits2 , weightsPN);
        System.out.println(resultN2[0] + "." + resultN2[1] + " <-----    negativDigits == -8+6.5 == " + ((-8+13)-(13*0.5)));



        System.out.println(" ================ ");
        System.out.println(" ================ ");

        //                    A       B               C               D
        Long[] digits10   = { 0L    , 8L            , 6L            , 2L     };
        Long[] digits1    = { 7L    , 6L            , 2L            , 5L     };
        int[] weights     = {  1    ,  1            ,  1            , 1      };
        long[] result10 = x2calculateWeightedAverage(digits10, weights);
        long[] result1  = x2calculateWeightedAverage(digits1 , weights);
        System.out.println(result10[0] + "." + result10[1] + " " + result1[0] + "." + result1[1] + " <-----    result");

        System.out.println(" ================ ");

        //                    A       B               C1      C2      D            we doubled C
        Long[] w0Digits10 = { 0L    , 8L            , 6L    , 6L    , 2L     };
        Long[] w0Digits1  = { 7L    , 6L            , 2L    , 2L    , 5L     };
        int[] w0Weights   = {  2    ,  2            ,  1    ,  1    ,  2     }; // so C has half weight
        long[] w0Result10 = x2calculateWeightedAverage(w0Digits10, w0Weights);
        long[] w0Result1  = x2calculateWeightedAverage(w0Digits1 , w0Weights);
        System.out.println(w0Result10[0] + "." + w0Result10[1] + " " + w0Result1[0] + "." + w0Result1[1] + " <----- w0 result");

        System.out.println(" ================ ");

        //                    A       B               C1      C2      D            this works even if we have C1=C+x and C2=C-x.
        Long[] w1Digits10 = { 0L    , 8L            , 9L    , 3L    , 2L     };
        Long[] w1Digits1  = { 7L    , 6L            , 4L    , 0L    , 5L     };
        int[] w1Weights   = {  2    ,  2            ,  1    ,  1    ,  2     };
        long[] w1Result10 = x2calculateWeightedAverage(w1Digits10, w1Weights);
        long[] w1Result1  = x2calculateWeightedAverage(w1Digits1 , w1Weights);
        System.out.println(w1Result10[0] + "." + w1Result10[1] + " " + w1Result1[0] + "." + w1Result1[1] + " <----- w1 result");

        System.out.println(" ================ ");

        //                    A       B1      B2      C               D           we doubled B
        Long[] w2Digits10 = { 0L    , 9L    , 7L    , 6L            , 2L     };
        Long[] w2Digits1  = { 7L    , 4L    , 8L    , 2L            , 5L     };
        int[] w2Weights   = {  2    ,  1    ,  1    ,  2            ,  2     }; // so B has half weight
        long[] w2Result10 = x2calculateWeightedAverage(w2Digits10, w2Weights);
        long[] w2Result1  = x2calculateWeightedAverage(w2Digits1 , w2Weights);
        System.out.println(w2Result10[0] + "." + w2Result10[1] + " " + w2Result1[0] + "." + w2Result1[1] + " <----- w2 result");


        System.out.println(" ================ ");

        /**/
        // it is not possible to just double the count D because 75% to 25% is not the same as 60% to 40%
        //                    A       B               C               D            we found out D has 2x cost relativ to A
        Long[] w3Digits10 = { 0L    , 8L            , 6L            , 2*2L     }; // so we just count it 2x
        Long[] w3Digits1  = { 7L    , 6L            , 2L            , 2*5L     }; // so we just count it 2x
        int[] w3Weights   = { 2     , 2             , 2             , 2        };
        long[] w3Result10 = x2calculateWeightedAverage100(w3Digits10, w3Weights);
        long[] w3Result1  = x2calculateWeightedAverage100(w3Digits1 , w3Weights);
        System.out.println(w3Result10[0] + "." + w3Result10[1] + " " + w3Result1[0] + "." + w3Result1[1] + " <----- as expected w3 (2*counter) result is bigger than w2");

        System.out.println(" ================ ");


        Long[] w3NewDigits10 = { 0L    , 8L            , 6L            , 2L, 2L     }; // so we just count it 2x
        Long[] w3NewDigits1  = { 7L    , 6L            , 2L            , 5L, 5L     }; // so we just count it 2x
        int[] w3NewWeights   = { 2     , 2             , 2             , 2 , 2      };
        long[] w3NewResult10 = x2calculateWeightedAverage(w3NewDigits10, w3NewWeights);
        long[] w3NewResult1  = x2calculateWeightedAverage(w3NewDigits1 , w3NewWeights);
        System.out.println(w3NewResult10[0] + "." + w3NewResult10[1] + " " + w3NewResult1[0] + "." + w3NewResult1[1] + " <----- as expected w3New (count 2 times) result is bigger than w2");

        System.out.println(" ================ ");

        //                    A       B1      B2      C1      C2      D
        Long[] w4Digits10 = { 0L    , 8L            , 6L            , 2L     };
        Long[] w4Digits1  = { 7L    , 6L            , 2L            , 5L     };
        int[] w4Weights   = { 2     , 2             , 2             , 4      }; // it is the same like weight it 2x
        long[] w4Result10 = x2calculateWeightedAverage(w4Digits10, w4Weights);
        long[] w4Result1  = x2calculateWeightedAverage(w4Digits1 , w4Weights);
        System.out.println(w4Result10[0] + "." + w4Result10[1] + " " + w4Result1[0] + "." + w4Result1[1] + " <----- w4 result");

        System.out.println(" ================ ");

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

    // von copi
    private static long[] x2calculateWeightedAverage100(Long[] counters, int[] weights) {
        long numerator = 0;
        long denominator = 0;

        for (int i = 0; i < counters.length; i++) {
            numerator += counters[i] * weights[i];
            denominator += weights[i];
        }

        // Normierung der Normierung:
        // Skaliere Zähler und Nenner so, dass die Nenner-Summe auf eine feste Basis gebracht wird (z.B. 100).
        int targetDenominator = 100;
        double factor = (double) targetDenominator / denominator;
        //numerator = Math.round(numerator * factor);
        denominator = targetDenominator;

        long result = numerator / denominator;
        long remainder = numerator % denominator;
        long fraction = remainder * 100 / denominator;

        return new long[] {result, fraction};
    }


    // von mir
    private static long[] x2calculateWeightedAverage(Long[] counters, int[] weights) {
        int weightsSum = 0;
        for (int weight : weights) {
            weightsSum += weight;
        }
        long weightedAvr = 0;
        long remainder = 0;
        for (int i = 0; i < counters.length; i++) {
            long count = Long.MIN_VALUE;
            if(counters[i] != null) {
                count = counters[i];
            }
            // multiplication may overflow, so we apply division early and we use method multiply that catches this.
            // addition may also overflow if weightSum is not correct, so we use method add that catches this.
            weightedAvr = add(weightedAvr, multiply((count / weightsSum), weights[i]));  // weightedAvr += (count / weightsSum) * weights[i];
            remainder = add(remainder, multiply(count % weightsSum, weights[i]));     // remainder += (count % weightsSum) * weights[i];
        }
        long correction = remainder / weightsSum;
        weightedAvr = add(weightedAvr, correction);

        long remainderAbs = remainder < 0 ? remainder * -1 : remainder;
        int scale = weightsSum;
        long fraction = ((remainderAbs % weightsSum) * scale) / weightsSum;

        return new long[] {weightedAvr , fraction };
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
