package org.conetex.runtime.test.instrument.count.opcodes;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class CountOpcodesA {

    public static void mainA(String[] args) {
        try (PrintStream out = new PrintStream(
                Files.newOutputStream(
                        Paths.get("target", "MainCountOpcodesA.txt"),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                ),
                true, // autoFlush
                StandardCharsets.UTF_8
        )) {

            out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ A");
            int j = 0;
            for (int i = 1; i <= 1; i++) {
                j = j + i * i;
            }
            out.println(j);
        }
     catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void mainB(String[] args) {
        try (PrintStream out = new PrintStream(
                Files.newOutputStream(
                        Paths.get("target", "MainCountOpcodesB.txt"),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND
                ),
                true, // autoFlush
                StandardCharsets.UTF_8
        )) {

            out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ B");
            int j = 0;
            for (int i = 1; i <= 10; i++) {
                j = j + i * i;
                out.print(j);
            }
            out.println(j);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // used by integration test
    public static void main(String[] args) {
        System.out.printf("Hello and welcome integration test");
    }

    public static void xmain(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        System.out.printf("Hello and welcome!");

        for (int i = 1; i <= 5; i++) {
            //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
            // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
            System.out.println("i = " + i);
        }

        // Eine Liste mit zufälligen Zahlen
        List<Integer> numbers = Arrays.asList(3, 7, 42, -5, 13);

        // Irgendeine mathematische Spielerei
        double weirdValue = Math.sqrt(Math.abs(numbers.get(2)))
                * Math.sin(numbers.get(0))
                + Math.pow(numbers.get(1), 2);

        System.out.println("Weird value: " + weirdValue);

        // Strings sinnlos manipulieren
        String text = "Banane";
        String reversed = new StringBuilder(text).reverse().toString();
        String shouted = text.toUpperCase(Locale.ROOT) + "!!!";
        String mixed = reversed.substring(0, 2) + shouted.substring(1, 4);

        System.out.println("Reversed: " + reversed);
        System.out.println("Shouted: " + shouted);
        System.out.println("Mixed nonsense: " + mixed);

        // Collections durcheinanderwürfeln
        Set<String> fruits = new HashSet<>(Arrays.asList("Apfel", "Birne", "Kirsche"));
        fruits.add("Apfel"); // tut nichts, aber sieht beschäftigt aus

        List<String> sorted = fruits.stream()
                .sorted(Comparator.comparingInt(String::length).reversed())
                .collect(Collectors.toList());

        System.out.println("Sorted by length desc: " + sorted);

        // Eine Map, die niemand braucht
        Map<String, Integer> map = new HashMap<>();
        map.put("X", 1);
        map.put("Y", 2);
        map.put("Z", 3);

        int sum = map.values().stream().reduce(0, Integer::sum);
        System.out.println("Sum of map values: " + sum);

        // Noch mehr Unsinn
        String joined = String.join(" ~ ", sorted);
        System.out.println("Joined nonsense: " + joined);


    }


}