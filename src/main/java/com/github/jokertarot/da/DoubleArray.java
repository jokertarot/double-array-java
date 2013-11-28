package com.github.jokertarot.da;

import java.io.*;

public class DoubleArray {

    public static final int START_ID = 1;
    public static final int NULL_ID = 0;
    public static final char TERMINATE_CHAR = '\0';

    public static void save(OutputStream os, DoubleArray da) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));
        int nodeCount = da.getNodeCount();
        dos.writeInt(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            dos.writeInt(da.base[i]);
            dos.writeInt(da.check[i]);
        }
        dos.close();
    }

    public static void save(String filename, DoubleArray da) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        save(fos, da);
        fos.close();
    }

    public static DoubleArray load(InputStream in) throws IOException {
        DataInputStream din = new DataInputStream(new BufferedInputStream(in));
        int nodeCount = din.readInt();
        int[] base = new int[nodeCount];
        int[] check = new int[nodeCount];
        for (int i = 0; i < nodeCount; i++) {
            base[i] = din.readInt();
            check[i] = din.readInt();
        }
        return new DoubleArray(base, check);
    }

    public static DoubleArray load(String filename) throws IOException {
        FileInputStream fin = new FileInputStream(filename);
        DoubleArray da = load(fin);
        fin.close();
        return da;
    }

    public int getSize() {
        return base.length * Integer.SIZE / 4;
    }

    public int getNodeCount() {
        return base.length;
    }

    private int[] base;
    private int[] check;

    protected DoubleArray(int[] base, int[] check) {
        this.base = base;
        this.check = check;
    }

    public int findIndex(String key) {
        return traverse(START_ID, key);
    }

    private int traverse(int nodeId, String key) {
        int length = key.length();
        for (int position = 0; position < length; position++) {
            int c = key.charAt(position);
            assert(c >= 1 && c <= 255);
            int nextNodeId = base[nodeId] + c;
            if (check[nextNodeId] != nodeId)
                return -1;
            nodeId = nextNodeId;
        }
        int terminateId = base[nodeId] + TERMINATE_CHAR;
        return (check[terminateId] == nodeId) ? -base[terminateId] - 1 : -1;
    }
}
