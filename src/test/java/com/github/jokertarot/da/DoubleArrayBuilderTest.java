package com.github.jokertarot.da;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class DoubleArrayBuilderTest {
    private DoubleArrayBuilder builder;

    @Test
    public void testEmptyList() {
        builder = new DoubleArrayBuilder();
        builder.testing();
    }

    @Test
    public void testBuild() {
        builder = new DoubleArrayBuilder();
        String[] words ={"ab", "ac", "b", "da"};
        builder.enableDebug();
        builder.setCodeIndexOffset('a' - 1);
        DoubleArray da = builder.build(Arrays.asList(words));
    }

    @Test
    public void testFindIndexSmall() throws Exception {
        builder = new DoubleArrayBuilder();
        String[] words ={"ace", "ad", "ade", "cab", "dab", "dad"};
        DoubleArray da = builder.build(Arrays.asList(words));

        assertEquals(-1, da.findIndex(""));
        assertEquals(-1, da.findIndex("a"));
        assertEquals(-1, da.findIndex("da"));
        assertEquals(-1, da.findIndex("dac"));

        assertEquals(0, da.findIndex("ace"));
        assertEquals(3, da.findIndex("cab"));

    }

    @Test
    public void testSaveAndLoad() throws IOException, ClassNotFoundException {
        builder = new DoubleArrayBuilder();
        String[] words ={"ace", "ad", "ade", "cab", "dab", "dad"};
        DoubleArray da = builder.build(Arrays.asList(words));

        File tmpfile = File.createTempFile("double_array_test", "dump");
        try {
            FileOutputStream os = new FileOutputStream(tmpfile);
            DoubleArray.save(os, da);
            os.close();

            FileInputStream in = new FileInputStream(tmpfile);
            DoubleArray nda = DoubleArray.load(in);

            assertEquals(-1, nda.findIndex(""));
            assertEquals(-1, nda.findIndex("a"));
            assertEquals(-1, nda.findIndex("da"));
            assertEquals(-1, nda.findIndex("dac"));

            assertEquals(0, nda.findIndex("ace"));
            assertEquals(3, nda.findIndex("cab"));
        } finally {
            tmpfile.delete();
        }
    }
}
