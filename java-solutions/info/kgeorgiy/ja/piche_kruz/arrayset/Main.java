package info.kgeorgiy.ja.piche_kruz.arrayset;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Main {
    public static void main (String[] args) {
        ArraySet<Integer> mySet = new ArraySet<>(List.of(-14, 12, 34, -14, 12, 34));
        System.out.println(mySet.first());
    }
}