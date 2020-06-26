package com.possible_triangle.divide.helper;

import java.util.TreeMap;

public class Roman {

    private final static TreeMap<Integer, String> map = new TreeMap<Integer,String>() {{
        put(1000, "M");
        put(900, "CM");
        put(500, "D");
        put(400, "CD");
        put(100, "C");
        put(90, "XC");
        put(50, "L");
        put(40, "XL");
        put(10, "X");
        put(9, "IX");
        put(5, "V");
        put(4, "IV");
        put(1, "I");
    }};

    public static String toRoman(int number) {
        if(number <= 0) return "0";
        int l = map.floorKey(number);
        if (number == l) return map.get(number);
        return map.get(l) + toRoman(number - l);
    }

}
