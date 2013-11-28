package com.github.jokertarot.da;

import java.util.*;

public class DoubleArrayBuilder {

    private static final int INITIAL_SIZE = 4096;
    private static final int MAX_EDGE_VALUE = 255;

    private List<String> words;
    private int maxNodeId;
    private ArrayList<Integer> base;
    private ArrayList<Integer> check;
    private int currentNodeIndex;

    private int emptyBeginIndex;
    private int emptyEndIndex;

    // Debug
    private boolean debug;
    private int codeIndexOffset;
    private int progressCount;

    public DoubleArrayBuilder() {
        codeIndexOffset = 0;
        debug = false;
    }

    public void setCodeIndexOffset(int index) {
        codeIndexOffset = index;
    }

    public void enableDebug() {
        debug = true;
    }

    private void initialize() {
        maxNodeId = 0;
        currentNodeIndex = 0;
        progressCount = 1;

        base = new ArrayList<Integer>();
        check = new ArrayList<Integer>();

        // base[0] and check[0] is not used.
        base.add(DoubleArray.NULL_ID);
        check.add(DoubleArray.NULL_ID);

        emptyBeginIndex = DoubleArray.START_ID;
        emptyEndIndex = INITIAL_SIZE - 1;
        createEmptyList(emptyBeginIndex, emptyEndIndex);
    }

    public void testing() {
        initialize();
    }

    private void checkPremise(List<String> words) {
        String prev = "";
        int lineCount = 1;
        for (String w : words) {
            if (w.compareTo(prev) <= 0) {
                StringBuilder msg = new StringBuilder();
                msg.append("Words should be sorted and no duplicated words: ");
                msg.append(prev + " <= " + w);
                msg.append(", at line:" + lineCount);
                throw new IllegalArgumentException(msg.toString());
            }
            prev = w;
            lineCount++;
        }
    }

    public DoubleArray build(List<String> words) {
        checkPremise(words);
        this.words = words;

        initialize();
        // Remove the root node from the empty list
        removeFromEmptyList(DoubleArray.START_ID);
        check.set(DoubleArray.START_ID, 0);

        makeNode(DoubleArray.START_ID, 0, 0, words.size());
        System.out.println("done.");

        assert(words.size() == currentNodeIndex);

        int[] intBase = new int[maxNodeId + 1];
        int[] intCheck = new int[maxNodeId + 1];

        for (int i = 0; i <= maxNodeId; i++) {
            intBase[i] = base.get(i);
            intCheck[i] = check.get(i);
        }

        if (debug)
            dumpResult();

        return new DoubleArray(intBase, intCheck);
    }

    private void dumpResult() {
        System.out.println(String.format("wordCounts: %d, nodeIndexCount: %d", words.size(), currentNodeIndex));
        for (int i = 1; i <= maxNodeId; i++)
            System.out.print(String.format("%3d ", i));
        System.out.println();
        for (int i = 1; i <= maxNodeId; i++)
            System.out.print(String.format("%3d ", base.get(i)));
        System.out.println();
        for (int i = 1; i <= maxNodeId; i++)
            System.out.print(String.format("%3d ", check.get(i)));
        System.out.println();
    }

    private static class EdgeRange {
        int nodeId;
        char edge;
        int left;
        int right;

        public EdgeRange(char edge, int left, int right) {
            this.nodeId = DoubleArray.NULL_ID;
            this.edge = edge;
            this.left = left;
            this.right = right;
        }
    }

    private String indentString(int depth, String mesg) {
        String spaces = depth == 0 ? "" : String.format("%" + 2*depth + "s", "");
        return spaces + mesg;
    }

    private void dumpNode(EdgeRange e, int depth, int parentId) {
        char printEdge = e.edge == DoubleArray.TERMINATE_CHAR ? '#' : (char) (e.edge + codeIndexOffset);
        String msg = String.format("id:%d[%c][%d,%d] parent:%d", e.nodeId, printEdge, e.left, e.right, parentId);
        System.out.println(indentString(depth, msg));
    }

    private void makeNode(int nodeId, int depth, int left, int right) {
        List<EdgeRange> edges = collectEdges(depth, left, right);
        if (edges.isEmpty())
            return;

        assignNode(nodeId, edges);

        for (EdgeRange e : edges) {
            if (debug)
                dumpNode(e, depth, nodeId);
            makeNode(e.nodeId, depth + 1, e.left, e.right);
        }
    }

    private char getEdge(String word, int depth) {
        if (depth == word.length())
            return DoubleArray.TERMINATE_CHAR;
        return (char) (word.charAt(depth) - codeIndexOffset);
    }

    private List<EdgeRange> collectEdges(int depth, int left, int right) {
        List<EdgeRange> edges = new ArrayList<EdgeRange>();

        int wordPosition = left;
        while (wordPosition < right) {
            String leftWord = words.get(wordPosition);

            if (leftWord.length() < depth) {
                wordPosition++;
                continue;
            }

            char edge = getEdge(leftWord, depth);
            int edgeLeft = wordPosition;
            wordPosition++;
            while (wordPosition < right &&
                    getEdge(words.get(wordPosition), depth) == edge)
                wordPosition++;
            int edgeRight = wordPosition;

            edges.add(new EdgeRange(edge, edgeLeft, edgeRight));
        }

        return edges;
    }

    private void assignNode(int nodeId, List<EdgeRange> edges) {
        int offset = lookupOffset(edges);
        base.set(nodeId, offset);
        for (EdgeRange e : edges) {
            e.nodeId = offset + e.edge;

            removeFromEmptyList(e.nodeId);
            check.set(e.nodeId, nodeId);

            // Assign negate index for the terminate edge.
            if (e.edge == DoubleArray.TERMINATE_CHAR) {
                base.set(e.nodeId, -(currentNodeIndex+1));
                currentNodeIndex++;
                // Show progress
                if (((double)currentNodeIndex / words.size()) * 10 > progressCount) {
                    System.out.print(progressCount + "0%..");
                    progressCount++;
                }
            }

            maxNodeId = Math.max(maxNodeId, e.nodeId);
        }
    }

    private int lookupOffset(List<EdgeRange> edges) {
        expandEmptyNodesIfNeeded(maxNodeId + MAX_EDGE_VALUE);
        // If a slot is an empty slot, base[i] contains negated index of the next
        // empty slot, and check[i] contains negated index of the previous empty slot.
        int offset = emptyBeginIndex;
        boolean loop = true;
        while (loop) {
            loop = false;
            for (EdgeRange e : edges) {
                if (check.get(offset + e.edge) >= 0) {
                    loop = true;
                    assert(base.get(offset) < 0);
                    offset = -base.get(offset);
                    break;
                }
            }
        }
        return offset;
    }

    // This method doesn't modify neither base[] or check[].
    private void removeFromEmptyList(int nodeId) {
        // FIXME: should not be occurred.
        if (check.get(nodeId) >= 0) {
            throw new IllegalArgumentException("Used slot: " + nodeId);
        }
        int next = -base.get(nodeId);
        int prev = -check.get(nodeId);
        base.set(prev, -next);
        check.set(next, -prev);
        if (nodeId == emptyBeginIndex)
            emptyBeginIndex = next;
        if (nodeId == emptyEndIndex)
            emptyEndIndex = prev;
    }

    private void expandEmptyNodesIfNeeded(int size) {
        int currentSize = base.size();
        if (size > currentSize) {
            int newSize = size + (currentSize >> 1);
            base.ensureCapacity(newSize);
            check.ensureCapacity(newSize);

            base.set(emptyEndIndex, -currentSize);
            check.set(emptyBeginIndex, -(newSize - 1));
            createEmptyList(currentSize, newSize - 1);
            base.set(newSize - 1, -emptyBeginIndex);
            check.set(currentSize, -emptyEndIndex);
            emptyEndIndex = newSize - 1;
        }
    }

    // Note that end is inclusive.
    private void createEmptyList(int begin, int end) {
        // begin
        base.add(-(begin+1));
        check.add(-end);
        // [begin+1 .. end-1]
        for (int i = begin + 1; i < end; i++) {
            base.add(-(i+1));
            check.add(-(i-1));
        }
        // end
        base.add(-begin);
        check.add(-(end-1));
    }
}
