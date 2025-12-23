package org.conetex.runtime.instrument;

import org.conetex.runtime.instrument.counter.Arithmetics;
import org.conetex.runtime.instrument.interfaces.*;

public class Report {

    // todo above is general math -

    public static void main(String[] args){

        // todo create tests:
        int[] weightsPN       = {  5    ,  5  };

        long[] positiveDigits1  = {  8L   ,  5L };
        ResultLongDividedByInt resultP1  = Arithmetics.weightedAverage(positiveDigits1 , weightsPN);
        System.out.println(resultP1.value() + "." + resultP1.fraction() + " <-----    positiveDigits == -1*");

        long[] negativeDigits1  = { -8L   , -5L };
        ResultLongDividedByInt resultN1  = Arithmetics.weightedAverage(negativeDigits1 , weightsPN);
        System.out.println(resultN1.value() + "." + resultN1.fraction() + " <-----    negativeDigits");

        long[] positiveDigits0  = new long[]{  8L   ,  11L };
        ResultLongDividedByInt resultP0  = Arithmetics.weightedAverage(positiveDigits0 , weightsPN);
        System.out.println(resultP0.value() + "." + resultP0.fraction() + " <-----    positiveDigits  == -1*");

        long[] negativeDigits0  = new long[]{ -8L   , -11L };
        ResultLongDividedByInt resultN0  = Arithmetics.weightedAverage(negativeDigits0 , weightsPN);
        System.out.println(resultN0.value() + "." + resultN0.fraction() + " <-----    negativeDigits");


        long[] positiveDigits2  = {  8L   ,  8L-13L };
        ResultLongDividedByInt resultP2  = Arithmetics.weightedAverage(positiveDigits2 , weightsPN);
        System.out.println(resultP2.value() + "." + resultP2.fraction() + " <-----    positiveDigits == 8-6.5 == " + ((8-13)+(13*0.5)));

        long[] negativeDigits2  = new long[]{ -8L   , -8L+13L };
        ResultLongDividedByInt resultN2  = Arithmetics.weightedAverage(negativeDigits2 , weightsPN);
        System.out.println(resultN2.value() + "." + resultN2.fraction() + " <-----    negativeDigits == -8+6.5 == " + ((-8+13)-(13*0.5)));



        System.out.println(" ================ ");
        System.out.println(" ================ ");

        //                    A       B               C               D
        long[] digits10   = { 0L    , 8L            , 6L            , 2L     };
        long[] digits1    = { 7L    , 6L            , 2L            , 5L     };
        int[] weights     = {  1    ,  1            ,  1            , 1      };
        ResultLongDividedByInt result10 = Arithmetics.weightedAverage(digits10, weights);
        ResultLongDividedByInt result1  = Arithmetics.weightedAverage(digits1 , weights);
        System.out.println(result10.value() + "." + result10.fraction() + " " + result1.value() + "." + result1.fraction() + " <-----    result");

        System.out.println(" ================ ");

        //                    A       B               C1      C2      D            we doubled C
        long[] w0Digits10 = { 0L    , 8L            , 6L    , 6L    , 2L     };
        long[] w0Digits1  = { 7L    , 6L            , 2L    , 2L    , 5L     };
        int[] w0Weights   = {  2    ,  2            ,  1    ,  1    ,  2     }; // so C has half weight
        ResultLongDividedByInt w0Result10 = Arithmetics.weightedAverage(w0Digits10, w0Weights);
        ResultLongDividedByInt w0Result1  = Arithmetics.weightedAverage(w0Digits1 , w0Weights);
        System.out.println(w0Result10.value() + "." + w0Result10.fraction() + " " + w0Result1.value() + "." + w0Result1.fraction() + " <----- w0 result");

        System.out.println(" ================ ");

        //                    A       B               C1      C2      D            this works even if we have C1=C+x and C2=C-x.
        long[] w1Digits10 = { 0L    , 8L            , 9L    , 3L    , 2L     };
        long[] w1Digits1  = { 7L    , 6L            , 4L    , 0L    , 5L     };
        int[] w1Weights   = {  2    ,  2            ,  1    ,  1    ,  2     };
        ResultLongDividedByInt w1Result10 = Arithmetics.weightedAverage(w1Digits10, w1Weights);
        ResultLongDividedByInt w1Result1  = Arithmetics.weightedAverage(w1Digits1 , w1Weights);
        System.out.println(w1Result10.value() + "." + w1Result10.fraction() + " " + w1Result1.value() + "." + w1Result1.fraction() + " <----- w1 result");

        System.out.println(" ================ ");

        //                    A       B1      B2      C               D           we doubled B
        long[] w2Digits10 = { 0L    , 9L    , 7L    , 6L            , 2L     };
        long[] w2Digits1  = { 7L    , 4L    , 8L    , 2L            , 5L     };
        int[] w2Weights   = {  2    ,  1    ,  1    ,  2            ,  2     }; // so B has half weight
        ResultLongDividedByInt w2Result10 = Arithmetics.weightedAverage(w2Digits10, w2Weights);
        ResultLongDividedByInt w2Result1  = Arithmetics.weightedAverage(w2Digits1 , w2Weights);
        System.out.println(w2Result10.value() + "." + w2Result10.fraction() + " " + w2Result1.value() + "." + w2Result1.fraction() + " <----- w2 result");


        System.out.println(" ================ ");

        /**/
        // it is not possible to just double the count D because 75% to 25% is not the same as 60% to 40%
        //                       A       B               C               D            we found out D has 2x cost relative to A
        long[] w3NewDigits10 = { 0L    , 8L            , 6L            , 2L, 2L     }; // so we just count it 2 times
        long[] w3NewDigits1  = { 7L    , 6L            , 2L            , 5L, 5L     }; // so we just count it 2 times
        int[] w3NewWeights   = { 2     , 2             , 2             , 2 , 2      };
        ResultLongDividedByInt w3NewResult10 = Arithmetics.weightedAverage(w3NewDigits10, w3NewWeights);
        ResultLongDividedByInt w3NewResult1  = Arithmetics.weightedAverage(w3NewDigits1 , w3NewWeights);
        System.out.println(w3NewResult10.value() + "." + w3NewResult10.fraction() + " " + w3NewResult1.value() + "." + w3NewResult1.fraction() + " <----- as expected w3New (count 2 times) result is bigger than w2");
        System.out.println(w3NewResult10.remainder() + " " + w3NewResult1.remainder() + " <- w3New rests");

        //                    A       B1      B2      C1      C2      D
        long[] w3Digits10 = { 0L    , 8L            , 6L            , 2*2L     }; // so we just multiply its count by 2
        long[] w3Digits1  = { 7L    , 6L            , 2L            , 2*5L     }; // so we just multiply its count by 2
        int[] w3Weights   = { 2     , 2             , 2             , 2        }; // but we keep the weightsSum 10 from "count it 2 times" above
        ResultLongDividedByInt w3Result10 = Arithmetics.weightedAverage(w3Digits10, w3Weights, 10);
        ResultLongDividedByInt w3Result1  = Arithmetics.weightedAverage(w3Digits1 , w3Weights, 10);
        System.out.println(w3Result10.value() + "." + w3Result10.fraction() + " " + w3Result1.value() + "." + w3Result1.fraction() + " <----- as expected w3 (2*counter) result is bigger than w2");
        System.out.println(w3Result10.remainder() + " " + w3Result1.remainder() + " <- w3 rests");

        System.out.println(" ================ ");

        //                    A       B1      B2      C1      C2      D
        long[] w4Digits10 = { 0L    , 8L            , 6L            , 2L     };
        long[] w4Digits1  = { 7L    , 6L            , 2L            , 5L     };
        int[] w4Weights   = { 2     , 2             , 2             , 4      }; // it is the same as weight it 2x
        ResultLongDividedByInt w4Result10 = Arithmetics.weightedAverage(w4Digits10, w4Weights);
        ResultLongDividedByInt w4Result1  = Arithmetics.weightedAverage(w4Digits1 , w4Weights);
        System.out.println(w4Result10.value() + "." + w4Result10.fraction() + " " + w4Result1.value() + "." + w4Result1.fraction() + " <----- w4 result");
        System.out.println(w4Result10.remainder() + " " + w4Result1.remainder() + " <- w4 rests");

        //                     A       B1      B2      C1      C2      D
        long[] w4Digits10b = { 0L    , 8L            , 6L            , 2L     };
        long[] w4Digits1b  = { 7L    , 6L            , 2L            , 5L     };
        int[] w4WeightsB   = { 1     , 1             , 1             , 2      };                                      // weights must be in right relation to each other.
        ResultLongDividedByInt w4Result10b = Arithmetics.weightedAverage(10, w4Digits10b, w4WeightsB); // but to get an understandable fraction, weightsSum has not to be 10.
        ResultLongDividedByInt w4Result1b  = Arithmetics.weightedAverage(10, w4Digits1b , w4WeightsB); // we can achieve this by setting scale to 10.
        System.out.println(w4Result10b.value() + "." + w4Result10b.fraction() + " " + w4Result1b.value() + "." + w4Result1b.fraction() + " <----- w4b result");
        System.out.println(w4Result10b.remainder() + " " + w4Result1b.remainder() + " <- w4b rests");

        System.out.println(" ================ ");

    }
    public static ResultLongDividedByInt[] calculateTotalCost(RetransformingClassFileTransformer transformer) {
        return transformer.getConfig().average();
    }







}
