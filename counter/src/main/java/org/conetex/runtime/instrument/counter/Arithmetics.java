package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.ResultLongDividedByInt;

public class Arithmetics {

    private record ResultLongDividedByIntImpl2(long value, int remainder, int fraction) implements ResultLongDividedByInt {}

    public static long add(long a, long b) {
        long re = a + b;
        if (b > 0 && re < a) {
            System.err.println("overflow: " + a + " > (" + a + " + " + b + " = " + re + ")");
            return Long.MAX_VALUE;
        }
        if (b < 0 && re > a) {
            System.err.println("underflow: " + a + " < (" + a + " + " + b + " = " + re + ")");
            return Long.MIN_VALUE;
        }
        return re;
    }

    public static long multiply(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }

        long result = a * b;

        boolean expectedPositive = (a > 0 && b > 0) || (a < 0 && b < 0);

        if (expectedPositive && result < 0) {
            System.err.println("overflow: " + result + " != " + a + " * " + b);
            return Long.MAX_VALUE;
        }
        if (!expectedPositive && result > 0) {
            System.err.println("underflow: " + result + " != " + a + " * " + b);
            return Long.MIN_VALUE;
        }

        return result;
    }

    public static int sum(int[] weights) {
        int weightsSum = 0;
        for (int weight : weights) {
            weightsSum += weight;
        }
        return weightsSum;
    }

    public static ResultLongDividedByInt weightedAverage(long[] counters, int[] weights) {
        int weightsSumAlsoUsedAsScale = sum(weights);
        return weightedAverage(weightsSumAlsoUsedAsScale, counters, weights, weightsSumAlsoUsedAsScale);
    }

    public static ResultLongDividedByInt weightedAverage(int scale, long[] counters, int[] weights) {
        return weightedAverage(scale, counters, weights, sum(weights));
    }

    public static ResultLongDividedByInt weightedAverage(long[] counters, int[] weights, int weightsSum) {
        return weightedAverage(sum(weights), counters, weights, weightsSum);
    }

    public static ResultLongDividedByInt weightedAverage(int scale, long[] counters, int[] weights, int weightsSum) {
        long weightedAvr = 0;
        long remainder = 0;
        for (int i = 0; i < counters.length; i++) {
            // multiplication may overflow, so we apply division early, and we use method multiply that catches this.
            // addition may also overflow if weightSum is not correct, so we use method add that catches this.
            weightedAvr = add(weightedAvr, multiply((counters[i] / weightsSum), weights[i]));  // weightedAvr += (count / weightsSum) * weights[i];
            remainder = add(remainder, multiply(counters[i] % weightsSum, weights[i]));     // remainder += (count % weightsSum) * weights[i];
        }
        long correction = remainder / weightsSum;
        weightedAvr = add(weightedAvr, correction);

        int remainingRemainder = (int) (remainder % weightsSum);
        int fraction = (remainingRemainder * scale) / weightsSum;

        return new ResultLongDividedByIntImpl2(
                weightedAvr,
                remainingRemainder,
                (fraction < 0 ? fraction * -1 : fraction)
        );
    }

}
