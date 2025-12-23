package org.conetex.runtime.instrument.counter;

import org.conetex.runtime.instrument.interfaces.ChainsOfLongs;
import org.conetex.runtime.instrument.interfaces.LinkedLong;
import org.conetex.runtime.instrument.interfaces.ChainOfLongs;
import org.conetex.runtime.instrument.interfaces.ResultLongDividedByInt;

@SuppressWarnings("ClassCanBeRecord")
public class CountersWeighted implements ChainsOfLongs {

    private final LongLimits minMax;
    private final int[] weights;
    private final ChainOfLongs[] counters;

    public CountersWeighted(LongLimits minMax, ChainOfLongs[] counters, int[] weights) {
        this.minMax = minMax;
        this.counters = counters;
        this.weights = weights;
    }

    private static long[] transformToLong(LinkedLong[] counters) {
        long[] countersRaw = new long[counters.length];
        for (int i = 0; i < counters.length; i++) {
            countersRaw[i] = counters[i].getValue();
        }
        return countersRaw;
    }

    private ResultLongDividedByInt weightedAverage(LinkedLong[] counters) {
        return Arithmetics.weightedAverage(CountersWeighted.transformToLong(counters), this.weights);
    }

    @Override
    public ResultLongDividedByInt[] average() {
        ResultLongDividedByInt[] result = new ResultLongDividedByInt[1];

        LinkedLong[] counters = new LinkedLong[this.counters.length];
        for (int i = 0; i < this.counters.length; i++) {
            counters[i] = this.counters[i].peek();
        }

        result[0] = this.weightedAverage(counters);
        counters = this.countPreviousOnAll(counters);

        while (this.containsCountableCounters(counters)) {
            // increase result
            ResultLongDividedByInt[] newResult = new ResultLongDividedByInt[result.length + 1];
            System.arraycopy(result, 0, newResult, 1, result.length);
            result = newResult;

            // store result part
            result[0] = this.weightedAverage(counters);

            // prepare next level
            counters = this.countPreviousOnAll(counters);
        }
        return result;
    }

    private boolean containsCountableCounters(LinkedLong[] counters) {
        for (LinkedLong counter : counters) {
            if (counter.getValue() > this.minMax.min()) {
                return true;
            }
        }
        return false;
    }

    private synchronized LinkedLong[] countPreviousOnAll(LinkedLong[] counters) {
        LinkedLong[] counterCounter = new LinkedLong[counters.length];

        for (int i = 0; i < counters.length; i++) {

            if(counters[i].hasPrevious()){
                counterCounter[i] = this.countPrevious(counters[i].getPrevious());
            }
            else{
                counterCounter[i] = new Tail(this.minMax);
            }

        }

        return counterCounter;
    }

    private LinkedLong countPrevious(LinkedLong current) {
        if(current.hasPrevious()){
            Node previousCounter = new Node(new Tail(this.minMax), this.minMax);
            do {

                if (previousCounter.value == this.minMax.max()) {
                    previousCounter = new Node(previousCounter, this.minMax);
                }
                previousCounter.value++;

                current = current.getPrevious();
            } while (current.hasPrevious());
            return previousCounter;
        }
        else{
            return new Tail(this.minMax);
        }
    }

}
