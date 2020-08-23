package com.nitkart.learn.jool;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class WindowsAnswers {

    private List<Tuple3<String, String, Integer>> products = Arrays.asList(
            tuple("Microsoft Lumia", "Smartphone", 200),
            tuple("HTC One", "Smartphone", 400),
            tuple("Nexus", "Smartphone", 500),
            tuple("iPhone", "Smartphone", 900),
            tuple("HP Elite", "Laptop", 1200),
            tuple("Lenovo Thinkpad", "Laptop", 700),
            tuple("Sony VAIO", "Laptop", 700),
            tuple("Dell Vostro", "Laptop", 800),
            tuple("iPad", "Tablet", 700),
            tuple("Kindle Fire", "Tablet", 150),
            tuple("Samsung Galaxy Tab", "Tablet", 200)
    );

    @Test
    void sumOfCurrentElementAndNextElement() {
        List<Integer> sums = Seq
                .of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .window(0, 1)
                .map(w -> w.sum().orElse(0))
                .limit(10)
                .toList();
        assertThat(sums).contains(3, 5, 7, 9, 11, 13, 15, 17, 19);
    }

    @Test
    void runningTotal() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> runningTotals = Seq
                .seq(numbers)
                .window(Long.MIN_VALUE, 0)
                .map(w -> w.sum().orElse(0))
                .limit(10)
                .toList();
        assertThat(runningTotals).contains(1, 3, 6, 10, 15, 21, 28, 36, 45, 55);
    }

    @Test
    void rankProductsByCategory() {
        Function<Tuple3<String, String, Integer>, String> partitionByProductCategory = t3 -> t3.v2;
        Comparator<? super Tuple3<String, String, Integer>> orderByPrice = comparing(t3 -> t3.v3);
        List<Tuple4<String, String, Integer, Long>> productsRankedByCategory = Seq
                .seq(products)
                .window(partitionByProductCategory, orderByPrice)
                .map(t3 -> t3.value().concat(t3.rank()))
                .sorted(comparing(
                            (Function<Tuple4<String, String, Integer, Long>, String>)Tuple4::v2) //Why is this needed???
                            .thenComparing(Tuple4::v4))
                .toList();
        assertThat(productsRankedByCategory).containsExactly(
                tuple("Lenovo Thinkpad",    "Laptop", 700, 0L),
                tuple("Sony VAIO",          "Laptop", 700, 0L),
                tuple("Dell Vostro",        "Laptop", 800, 2L),
                tuple("HP Elite",           "Laptop", 1200, 3L),
                tuple("Microsoft Lumia",    "Smartphone", 200, 0L),
                tuple("HTC One",            "Smartphone", 400, 1L),
                tuple("Nexus",              "Smartphone", 500, 2L),
                tuple("iPhone",             "Smartphone", 900, 3L),
                tuple("Kindle Fire",        "Tablet", 150, 0L),
                tuple("Samsung Galaxy Tab", "Tablet", 200, 1L),
                tuple("iPad",               "Tablet", 700, 2L)
        );
    }
}
