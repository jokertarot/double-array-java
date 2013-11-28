package com.github.jokertarot.da;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class DoubleArrayBuildMain {
    public static void main(String[] args) throws IOException {
        List<String> words = new ArrayList<String>();
        SortedSet<String> wordSet = new TreeSet<String>();

        BufferedReader reader =
                new BufferedReader(new InputStreamReader(System.in));
        String word;
        while ((word = reader.readLine()) != null) {
            wordSet.add(word);
        }
        words.addAll(wordSet);

        long startTime = System.nanoTime();
        DoubleArrayBuilder builder = new DoubleArrayBuilder();
        DoubleArray da = builder.build(words);
        long endTime = System.nanoTime();

        double size = (double)da.getSize() / 1024.0 / 1024.0;
        double elapsedTime = (endTime - startTime) / 10E6;

        String resultMessage =
                String.format("node count: %d, size: %.2f MB, time: %.1f ms",
                        da.getNodeCount(), size, elapsedTime);
        System.out.println(resultMessage);

        DoubleArray.save("da.dat", da);
    }
}
