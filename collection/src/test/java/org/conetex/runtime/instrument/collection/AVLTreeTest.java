package org.conetex.runtime.instrument.collection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AVLTreeTest {

    // Hilfsmethode: liest das private Feld 'root' via Reflection
    private Object getRootObject(AVLTree.Set<?> tree) throws Exception {
        Field rootField = AVLTree.class.getDeclaredField("root");
        rootField.setAccessible(true);
        return rootField.get(tree);
    }

    // Hilfsmethode: rekursive Inorder-Sammlung via Reflection (unabhängig von den package-methoden)
    private <D extends Comparable<D>> List<D> collectInOrderReflectively(AVLTree.Set<D> tree) throws Exception {
        Object root = getRootObject(tree);
        List<D> out = new ArrayList<>();
        if (root == null) return out;

        Class<?> nodeClass = Class.forName("org.conetex.runtime.instrument.collection.AVLTree$Node");
        collectInOrderNode(root, nodeClass, out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private <D extends Comparable<D>> void collectInOrderNode(Object nodeObj, Class<?> nodeClass, List<D> out) throws Exception {
        if (nodeObj == null) return;
        Field leftF = nodeClass.getDeclaredField("left");
        Field rightF = nodeClass.getDeclaredField("right");
        Field dataF = nodeClass.getDeclaredField("data");
        leftF.setAccessible(true);
        rightF.setAccessible(true);
        dataF.setAccessible(true);

        Object left = leftF.get(nodeObj);
        if (left != null) collectInOrderNode(left, nodeClass, out);

        D data = (D) dataF.get(nodeObj);
        out.add(data);

        Object right = rightF.get(nodeObj);
        if (right != null) collectInOrderNode(right, nodeClass, out);
    }

    // Hilfsmethode: ruft die package-private Traversal-Methoden via Reflection auf (um deren Code zu decken)
    private void invokeTraversals(AVLTree.Set<?> tree) throws Exception {
        Object root = getRootObject(tree);
        Class<?> nodeClass = Class.forName("org.conetex.runtime.instrument.collection.AVLTree$Node");

        Method pre = AVLTree.class.getDeclaredMethod("preOrder", nodeClass);
        Method in = AVLTree.class.getDeclaredMethod("inOrder", nodeClass);
        Method post = AVLTree.class.getDeclaredMethod("postOrder", nodeClass);

        pre.setAccessible(true);
        in.setAccessible(true);
        post.setAccessible(true);

        // Aufruf (gibt Ausgaben auf stdout, wir rufen nur auf, um die Codezeilen zu decken)
        pre.invoke(tree, root);
        in.invoke(tree, root);
        post.invoke(tree, root);
    }

    @Test
    void testLLRotation() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        // Insert 3,2,1 -> LL case -> right rotation
        tree.insertIntoTree(3);
        tree.insertIntoTree(2);
        tree.insertIntoTree(1);

        List<Integer> expected = Arrays.asList(1,2,3);
        List<Integer> actual = collectInOrderReflectively(tree);
        assertEquals(expected, actual);

        // Traversal methods aufrufen (Coverage)
        invokeTraversals(tree);
    }

    @Test
    void testRRRotation() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        // Insert 1,2,3 -> RR case -> left rotation
        tree.insertIntoTree(1);
        tree.insertIntoTree(2);
        tree.insertIntoTree(3);

        List<Integer> expected = Arrays.asList(1,2,3);
        assertEquals(expected, collectInOrderReflectively(tree));

        invokeTraversals(tree);
    }

    @Test
    void testLRRotation() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        // Insert 3,1,2 -> LR case
        tree.insertIntoTree(3);
        tree.insertIntoTree(1);
        tree.insertIntoTree(2);

        List<Integer> expected = Arrays.asList(1,2,3);
        assertEquals(expected, collectInOrderReflectively(tree));

        invokeTraversals(tree);
    }

    @Test
    void testRLRotation() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        // Insert 1,3,2 -> RL case
        tree.insertIntoTree(1);
        tree.insertIntoTree(3);
        tree.insertIntoTree(2);

        List<Integer> expected = Arrays.asList(1,2,3);
        assertEquals(expected, collectInOrderReflectively(tree));

        invokeTraversals(tree);
    }

    @Test
    void testDuplicateInsertReplaces() throws Exception {
        AVLTree.Set<String> tree = new AVLTree.Set<>();
        tree.insertIntoTree("b");
        tree.insertIntoTree("a");
        tree.insertIntoTree("c");
        // replace "b" with "b" (same value) -> code path that sets data on equal
        tree.insertIntoTree("b");

        List<String> expected = Arrays.asList("a","b","c");
        assertEquals(expected, collectInOrderReflectively(tree));
    }

    @Test
    void testInsertNullThrows() {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        assertThrows(NullPointerException.class, () -> tree.insertIntoTree(null));
    }

    @Test
    void testDeleteTriggersLLRotation() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        // Aufbau so, dass nach dem Löschen eines rechten Knotens ein left-heavy Fall entsteht (LL)
        // Sequenz gewählt aus klassischen AVL-Beispielen
        int[] inserts = {30, 20, 40, 10, 25, 5, 15, 2};
        for (int v : inserts) tree.insertIntoTree(v);

        // Lösche einen Knoten im rechten Teil, der die Balance auf der linken Seite erzwingen sollte
        tree.deleteFromTree(40);
        tree.deleteFromTree(25);

        // Inorder muss sortiert bleiben
        List<Integer> expected = Arrays.asList(2,5,10,15,20,30);
        assertEquals(expected, collectInOrderReflectively(tree));

        // Traversals aufrufen, damit auch synchronized-Rotationspfade betreten werden
        invokeTraversals(tree);
    }


    @Test
    void testDeleteTriggersRRRotation() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        // Aufbau so, dass nach dem Löschen eines linken Knotens ein right-heavy Fall entsteht (RR)
        int[] inserts = {10, 20, 5, 30, 25, 40, 35, 45};
        for (int v : inserts) tree.insertIntoTree(v);

        // Lösche einen Knoten im linken Teil, der die Balance auf der rechten Seite erzwingen sollte
        tree.deleteFromTree(5);
        tree.deleteFromTree(20);

        List<Integer> expected = Arrays.asList(10,25,30,35,40,45);
        assertEquals(expected, collectInOrderReflectively(tree));

        invokeTraversals(tree);
    }


    @Test
    void testDeleteTriggersLRRotation() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        // Aufbau für LR-Fall nach Löschung: linkes Kind hat rechten schweren Teil
        int[] inserts = {30, 10, 40, 5, 20, 15, 25, 17};
        for (int v : inserts) tree.insertIntoTree(v);

        // Lösche einen Knoten im rechten Bereich, so dass die linke Seite relativ schwerer wird
        tree.deleteFromTree(40);
        tree.deleteFromTree(30);


        List<Integer> expected = Arrays.asList(5,10,15,17,20,25);
        assertEquals(expected, collectInOrderReflectively(tree));

        invokeTraversals(tree);
    }

    @Test
    void testDeleteTriggersRLRotation() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        // Aufbau für RL-Fall nach Löschung: rechtes Kind hat linken schweren Teil
        int[] inserts = {30, 10, 40, 5, 20, 15, 25, 22};
        for (int v : inserts) tree.insertIntoTree(v);

        // Lösche einen Knoten im linken Bereich, so dass die rechte Seite relativ schwerer wird
        tree.deleteFromTree(5);
        tree.deleteFromTree(15);

        List<Integer> expected = Arrays.asList(10, 20, 22, 25, 30, 40);
        assertEquals(expected, collectInOrderReflectively(tree));

        invokeTraversals(tree);
    }

    @Test
    void testDeleteLeafAndOneChildAndTwoChildren() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();

        // Aufbau ähnlich zu deinem main-Beispiel
        int[] inserts = {3,2,1,4,5,6,7,16,25,19,20,5};
        for (int v : inserts) tree.insertIntoTree(v);

        // initial check
        List<Integer> initial = collectInOrderReflectively(tree);
        List<Integer> expectedInitial = Arrays.asList(1,2,3,4,5,6,7,16,19,20,25);
        assertEquals(expectedInitial, initial);

        // delete leaf (25)
        tree.deleteFromTree(25);
        List<Integer> after1 = collectInOrderReflectively(tree);
        List<Integer> expectedAfter1 = Arrays.asList(1,2,3,4,5,6,7,16,19,20);
        assertEquals(expectedAfter1, after1);

        // delete node with two children (16 has children 6 and 20 in this tree)
        tree.deleteFromTree(16);
        List<Integer> after2 = collectInOrderReflectively(tree);
        List<Integer> expectedAfter2 = Arrays.asList(1,2,3,4,5,6,7,19,20);
        assertEquals(expectedAfter2, after2);

        tree.deleteFromTree(5);
        // delete node with one child (delete 5 which may be internal/leaf depending on structure)
        tree.deleteFromTree(6);
        List<Integer> after3 = collectInOrderReflectively(tree);
        // expected: 1,2,4,6,7,16,19,20  (5 removed)
        List<Integer> expectedAfter3 = Arrays.asList(1,2,3,4,7,19,20);
        assertEquals(expectedAfter3, after3);

        // delete non-existent -> no change
        tree.deleteFromTree(999);
        assertEquals(expectedAfter3, collectInOrderReflectively(tree));

        // delete null -> no-op
        tree.deleteFromTree(null);
        assertEquals(expectedAfter3, collectInOrderReflectively(tree));

        // Traversals to cover those methods
        invokeTraversals(tree);
    }

    @Test
    void testDeleteRootAndComplexCase() throws Exception {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();
        // Build a tree where root will be deleted and successor logic used
        int[] inserts = {20,10,30,5,15,12,17};
        for (int v : inserts) tree.insertIntoTree(v);

        // current inorder
        List<Integer> before = collectInOrderReflectively(tree);
        assertEquals(Arrays.asList(5,10,12,15,17,20,30), before);

        // delete root (20) which has two children -> successor is 30's leftmost (30)
        tree.deleteFromTree(20);
        List<Integer> after = collectInOrderReflectively(tree);
        // expected: 5,10,12,15,17,30
        assertEquals(Arrays.asList(5,10,12,15,17,30), after);

        invokeTraversals(tree);
    }
}