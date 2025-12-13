package org.conetex.contract.runtime.example;



import org.conetex.contract.runtime.example.subpackage.ClassFromOtherPackage;
import org.conetex.contract.runtime.instrument.counter.Jump;
import org.conetex.contract.runtime.instrument.counter.MethodEntry;

import java.util.TreeMap;

public class Main {

    public static void main(String[] args) {

        System.out.println("S T A R T");
        System.out.println("Example Counter at start: " + MethodEntry.getHead().getCount() + " ");
        System.out.println("Example Jumps at start: " + Jump.getHead().getCount() + " ");
        //MethodCalls.head.count = Long.MIN_VALUE;
        System.out.println("Example Counter at reset: " + MethodEntry.getHead().getCount() + " ");
        System.out.println("Example Jumps at reset: " + Jump.getHead().getCount() + " ");

        System.out.println("Example Counter before real nothing: " + MethodEntry.getHead().getCount() + " ");
        System.out.println("Example Jumps before real nothing: " + Jump.getHead().getCount() + " ");
        System.out.println("Example Jumps after real nothing: " + Jump.getHead().getCount() + " ");
        System.out.println("Example Counter after real nothing: " + MethodEntry.getHead().getCount() + " ");

        System.out.println("Example Counter before nothing: " + MethodEntry.getHead().getCount() + " ");
        System.out.println("Example Jumps before nothing: " + Jump.getHead().getCount() + " ");
        nothing();
        System.out.println("Example Jumps after nothing: " + Jump.getHead().getCount() + " ");
        System.out.println("Example Counter after nothing: " + MethodEntry.getHead().getCount() + " ");

        long i = MethodEntry.getHead().getCount();
        Long x = Long.valueOf(Long.MAX_VALUE);
        long i1 = MethodEntry.getHead().getCount();
        Long xx = x.longValue();
        String xxx = xx.toString();
        System.out.println("Example Jumps at toString: " + Jump.getHead().getCount() + " ");
        long i2 = MethodEntry.getHead().getCount();
        System.out.println("Example Counter: " + MethodEntry.getHead().getCount() + " " + xxx);
        i = MethodEntry.getHead().getCount();
        System.out.println("load counter..." + x);
        System.out.println("Example Counter: ".concat(Long.toString(MethodEntry.getHead().getCount())) );
        ClassFromOtherPackage.test();
        i = MethodEntry.getHead().getCount();
        foo();
        System.out.println("Example Counter: " + MethodEntry.getHead().getCount());
        Methods.foo();
        System.out.println("Example Counter: " + MethodEntry.getHead().getCount());
        Methods.bar();
        System.out.println("Example Counter: " + MethodEntry.getHead().getCount());
        bar();
        System.out.println("Example Counter: " + MethodEntry.getHead().getCount());

        TreeMap<String, String> treeX = new TreeMap<>();
        System.out.println("Example Counter x: ".concat( Long.valueOf(MethodEntry.getHead().getCount()).toString() ));
        treeX.put("x", "xx");
        StackTraceElement yyy = null;
        //IdentityHashMap.IdentityHashMapIterator xxxxx = null;
        String isInMap = treeX.get("x");
        System.out.println("Example Counter x: ".concat( Long.valueOf(MethodEntry.getHead().getCount()).toString() ));
        System.out.println("Example Counter x: ".concat( Long.valueOf(MethodEntry.getHead().getCount()).toString() ));

    }

    static void nothing() {
    }

    static void foo() {
        System.out.println("foo");
    }

    static void bar() {
        System.out.println("bar");
    }

}