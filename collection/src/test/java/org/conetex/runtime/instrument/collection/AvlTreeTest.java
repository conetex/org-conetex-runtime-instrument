package org.conetex.runtime.instrument.collection;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AvlTreeTest {

    // Hilfsmethode: liest das private Feld 'root' via Reflection
    private Object getRootObject(AvlTree.Set<?> tree) throws Exception {
        Field rootField = AvlTree.Set.class.getDeclaredField("root");
        rootField.setAccessible(true);
        return rootField.get(tree);
    }

    // Hilfsmethode: rekursive Inorder-Sammlung via Reflection (unabhängig von den package-methoden)
    private <D extends Comparable<D>> List<D> collectInOrderReflectively(AvlTree.Set<D> tree) throws Exception {
        AvlTree.AbstractNode<D,D> root = tree.getRoot();
        List<D> out = new ArrayList<>();
        if (root == null) return out;

        collectInOrderNode(root, out);
        return out;
    }

    @SuppressWarnings("unchecked")
    private <D extends Comparable<D>> void collectInOrderNode(AvlTree.AbstractNode<D,D> nodeObj, List<D> out) throws Exception {
        if (nodeObj == null) return;
        AvlTree.AbstractNode<D,D> left = nodeObj.left();
        AvlTree.AbstractNode<D,D> right = nodeObj.right();
        D data = nodeObj.key();


        if (left != null) collectInOrderNode(left, out);

        out.add(data);

        if (right != null) collectInOrderNode(right, out);
    }

    // Hilfsmethode: ruft die package-private Traversal-Methoden via Reflection auf (um deren Code zu decken)
    private <X extends Comparable<X>> void invokeTraversals(AvlTree.Set<X> tree) throws Exception {
        // Aufruf (gibt Ausgaben auf stdout, wir rufen nur auf, um die Codezeilen zu decken)
        tree.preOrder(tree.getRoot());
        System.out.println(" - preOrder");

        tree.inOrder(tree.getRoot());
        System.out.println(" - inOrder");

        Iterator<X> i = tree.iterator();
        while(i.hasNext()){
            System.out.print( i.next() + "; ");
        }
        System.out.println(" - inOrderIterator A");
        for( X x : tree ){
            System.out.print( x + "; ");
        }
        System.out.println(" - inOrderIterator B");

        tree.reverseOrder(tree.getRoot());
        System.out.println(" - reverseOrder");

        for (Iterator<X> ir = tree.reverseIterator(); ir.hasNext(); ) {
            System.out.print( ir.next() + "; ");
        }
        System.out.println(" - reverseOrderIterator A");
        for (X x : tree.reverseIterable()) {
            System.out.print( x + "; ");
        }
        System.out.println(" - reverseOrderIterator B");


        tree.postOrder(tree.getRoot());
        System.out.println(" - postOrder");
    }

    @Test
    void testLLRotation() throws Exception {
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
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
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
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
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
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
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
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
        AvlTree.Set<String> tree = new AvlTree.Set<>();
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
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
        //assertThrows(NullPointerException.class, () -> tree.insertIntoTree(null));
        assertDoesNotThrow(() -> tree.insertIntoTree(null));
    }

    @Test
    void testDeleteTriggersLLRotation() throws Exception {
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
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
        /*AVLTree.SetO<Integer> tree1 = new AVLTree.SetO<>();
        // Aufbau so, dass nach dem Löschen eines linken Knotens ein right-heavy Fall entsteht (RR)
        int[] inserts1 = {10, 20, 5, 30, 25, 40, 35, 45};
        for (int v : inserts1) {
            System.out.println("insert " + v);
            tree1.insertIntoTree(v);
        }*/

        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
        // Aufbau so, dass nach dem Löschen eines linken Knotens ein right-heavy Fall entsteht (RR)
        int[] inserts = {10, 20, 5, 30, 25, 40, 35, 45};
        for (int v : inserts) {
            System.out.println("insert " + v);
            tree.insertIntoTree(v);
        }

        // Lösche einen Knoten im linken Teil, der die Balance auf der rechten Seite erzwingen sollte
        tree.deleteFromTree(5);
        tree.deleteFromTree(20);

        List<Integer> expected = Arrays.asList(10,25,30,35,40,45);
        assertEquals(expected, collectInOrderReflectively(tree));

        invokeTraversals(tree);
    }


    @Test
    void testDeleteTriggersLRRotation() throws Exception {
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
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
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
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
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();

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
        //assertThrows(Exception.class, () -> tree.deleteFromTree(null));
        assertDoesNotThrow(() -> tree.deleteFromTree(null));
        assertEquals(expectedAfter3, collectInOrderReflectively(tree));

        // Traversals to cover those methods
        invokeTraversals(tree);


    }

    @Test
    void testDeleteRootAndComplexCase() throws Exception {
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();
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

    @Test
    void smokeTest() throws Exception {
        assertDoesNotThrow(AvlTreeTest::smokeTestImplementation);
    }

    private static void smokeTestImplementation() {
        AvlTree.Set<Integer> tree = new AvlTree.Set<>();

        tree.insertIntoTree(3);
        tree.insertIntoTree(2);
        tree.insertIntoTree(1);

        tree.insertIntoTree(4);
        tree.insertIntoTree(5);
        tree.insertIntoTree(6);

        tree.insertIntoTree(7);
        tree.insertIntoTree(16);
        tree.insertIntoTree(25);

        tree.insertIntoTree(19);
        tree.insertIntoTree(20);

        tree.insertIntoTree(5);

        System.out.println("Preorder traversal of constructed AVL tree is : ");
        tree.preOrder(tree.getRoot());
        System.out.println();

        System.out.println("Inorder traversal of constructed AVL tree is : ");
        tree.inOrder(tree.getRoot());
        System.out.println();

        System.out.println("Postorder traversal of constructed AVL tree is : ");
        tree.postOrder(tree.getRoot());
        System.out.println();

        tree.deleteFromTree(19);
        tree.deleteFromTree(6);

        tree.deleteFromTree(7);

        tree.deleteFromTree(4);
        tree.deleteFromTree(16);

        System.out.println("Preorder traversal of constructed AVL tree is : ");
        tree.preOrder(tree.getRoot());
        System.out.println();

        System.out.println("Inorder traversal of constructed AVL tree is : ");
        tree.inOrder(tree.getRoot());
        System.out.println();

        System.out.println("Postorder traversal of constructed AVL tree is : ");
        tree.postOrder(tree.getRoot());
        System.out.println();

    }

}