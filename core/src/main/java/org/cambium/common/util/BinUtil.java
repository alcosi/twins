package org.cambium.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BinUtil {
    
    private BinUtil() {}

    private static class Range {
        private String name;
        private int from, to;
        public Range(String name, int from, int to) {
            super();
            this.name = name;
            this.from = from;
            this.to = to;
        }
        public String getName() {
            return name;
        }
        public int getFrom() {
            return from;
        }
        public int getTo() {
            return to;
        }
        boolean in(int v) {
            return from <= v && v <= to;
        }
    }
    
    private static List<Range> list = new ArrayList<>();
    
    private static void add(String name, int n) {
        add(name, n, n);
    }
    
    private static void add(String name, int n, int m) {
        while (n / 100000 == 0) {
            n *= 10;
        }
        while (m / 100000 == 0) {
            m *= 10; m += 9;
        }
        
        list.add(new Range(name, n , m));
    }
    
    static {
        add("American Express", 34);
        add("American Express", 37);
        add("Bankcard", 5610);
        add("Bankcard", 560221, 560225);
        add("China UnionPay", 62);
        add("Diners Club enRoute", 2014);
        add("Diners Club enRoute", 2149);
        add("Diners Club International", 36);
        add("Diners Club International", 300, 305);
        add("Diners Club International", 3095);
        add("Diners Club International", 38, 39);
        add("Diners Club United States & Canada", 54, 55);
        add("Discover Card", 6011);
        add("Discover Card", 64, 65);
        add("RuPay", 60);
        add("RuPay", 6521);
        add("InterPayment", 636, 639);
        add("JCB", 3528, 3589);
        add("Maestro", 50);
        add("Maestro", 56, 58);
        add("Maestro", 6);
        add("Dankort", 5019);
        add("Dankort", 4571);
        add("MIR", 2200, 2204);
        add("MasterCard", 2221, 2720);
        add("MasterCard", 51, 55);
        add("Solo", 6334);
        add("Solo", 6767);
        add("Switch", 4903);
        add("Switch", 4905);
        add("Switch", 4911);
        add("Switch", 4936);
        add("Switch", 564182);
        add("Switch", 633110);
        add("Switch", 6333);
        add("Switch", 6759);
        add("Troy", 979200, 979289);
        add("Visa", 4);
        add("UATP", 1);
        add("Verve", 506099, 506198);
        add("Verve", 650002, 650027);
    }
    
    public static String find(int bin) {
        return list.stream().filter(r -> r.in(bin)).sorted(Comparator.comparing(Range::getFrom).reversed().thenComparing(Range::getTo)).findFirst().map(Range::getName).orElse(null);
    }
}
