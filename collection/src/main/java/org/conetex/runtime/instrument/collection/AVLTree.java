package org.conetex.runtime.instrument.collection;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class AVLTree<T extends Comparable<T>> {

    public interface Entry<K extends Comparable<K>, V> {

        K key();

        V value();

    }

    static abstract class AbstractNode<K extends Comparable<K>, V> implements Entry<K, V>
    {

        public abstract K key();

        public abstract V value();

        abstract int height();

        abstract AbstractNode<K,V> left();

        abstract AbstractNode<K,V> right();

        abstract AbstractNodeChange<K,V> insert(AbstractLeafNode<K,V> nodeToInsert);

        abstract AbstractNode<K,V> find(K keyToFind);

        abstract AbstractNodeChange<K,V> delete(K keyToDelete);

        abstract AbstractNodeChange<K,V> change(AbstractNode<K,V> left, AbstractNode<K,V> right);

    }

    private static abstract class AbstractNodeChange<K extends Comparable<K>, V> {

        private AbstractNode<K,V> left;

        private AbstractNode<K,V> right;

        private final K key;

        private int height;

        AbstractNodeChange(AbstractNode<K,V> left, K key, AbstractNode<K,V> right) {
            this.left = left;
            this.right = right;
            this.key = key;
            this.height = AbstractBalancedNode.calculateHeight(this.left, this.right);
        }

        abstract AbstractNode<K,V> implement();

    }

    private static abstract class AbstractBalancedNode<K extends Comparable<K>, V> extends AbstractNode<K,V>{
        private AbstractNode<K,V> left;
        private AbstractNode<K,V> right;
        private K key;
        private int height;

        private AbstractBalancedNode(AbstractNode<K,V> left, K key, AbstractNode<K,V> right) {
            this.left = left;
            this.right = right;
            this.key = key;
            this.height = AbstractBalancedNode.calculateHeight(this.left, this.right);
        }

        private AbstractBalancedNode(AbstractNode<K,V> left, K key, AbstractNode<K,V> right, int height) {
            this.left = left;
            this.right = right;
            this.key = key;
            this.height = height;
        }

        @Override
        final int height() {
            return this.height;
        }

        static int calculateHeight(AbstractNode<?,?> left, AbstractNode<?,?> right) {
            if(left == null){
                if(right == null){
                    return 1;
                }
                else{
                    return 1 + right.height();
                }
            }
            else{
                if(right == null){
                    return 1 + left.height();
                }
                else{
                    int leftHeight = left.height();
                    int rightHeight = right.height();
                    if(leftHeight > rightHeight){
                        return 1 + leftHeight;
                    }
                    else{
                        return 1 + rightHeight;
                    }
                }
            }
        }

        @Override
        public K key() {
            return this.key;
        }

        @Override
        AbstractNode<K,V> left() {
            return this.left;
        }

        @Override
        AbstractNode<K,V> right() {
            return this.right;
        }

        @Override
        AbstractNode<K,V> find(K keyToFind) {
            int cmp = keyToFind.compareTo(this.key);
            if (cmp < 0) {
                // go left
                if (this.left == null) {
                    return null;
                }
                else{
                    return this.left.find(keyToFind);
                }
            }
            if (cmp > 0) {
                // go right
                if (this.right == null) {
                    return null;
                }
                else{
                    return this.right.find(keyToFind);
                }
            }
            return this;
        }

        @Override
        AbstractNodeChange<K,V> delete(K valueToRemove) {
            int cmp = valueToRemove.compareTo(this.key);

            if (cmp < 0) {
                // go left
                AbstractNode<K,V> oldLeft;
                while(true) {
                    synchronized (this) {
                        if (this.left == null) {
                            return null;
                        }
                        oldLeft = this.left;
                    }
                    AbstractNodeChange<K,V> newLeft = oldLeft.delete(valueToRemove);
                    synchronized (this) {
                        if (this.left == oldLeft) {
                            if(newLeft == null){
                                return null;
                            }
                            this.left = newLeft.implement();
                            if (this.right != null && this.right instanceof AVLTree.AbstractBalancedNode<K,V> rightA && rightA.height > newLeft.height + 1) {
                                // right heavy
                                if (this.right.left() == null || (this.right.right() != null && this.right.left().height() <= this.right.right().height())) {
                                    // RR
                                    //return rightA.callRotateLeft(this);
                                    return this.rotateLeftNew(rightA);
                                } else {
                                    // RL
                                    //return rightA.callRotateRightLeft(this);
                                    return this.rotateRightLeftNew(rightA);
                                }
                            }
                            this.height = AbstractBalancedNode.calculateHeight(this.left, this.right);
                            return null;
                        }
                    }
                }
            }

            if (cmp == 0) {
                // found node
                synchronized (this) {
                    if (this.left == null && this.right == null) {
                        // delete this node - successor not needed
                        return new NodeChangeToNull<>();
                    }
                    if (this.left == null) {
                        // delete this node - successor is right
                        return new AbstractNodeChange<>(null, null, null){
                            @Override
                            AbstractNode<K,V> implement() {
                                return AbstractBalancedNode.this.right;
                            }
                        };
                    }
                    if (this.right == null) {
                        // delete this node - successor is left
                        return new NodeChangeToExisting<>(this.left);
                    }
                    // delete this node - new value is smallest (left) at right
                    AbstractNode<K,V> newValueNode = this.right;
                    while (newValueNode.left() != null) {
                        newValueNode = newValueNode.left();
                    }
                    // copy data from successor to this
                    //this.takeOverKeyValue(newValueNode);
                    this.key = newValueNode.key();
                    this.updateValue(newValueNode.value());
                    // remove successor
                    valueToRemove = newValueNode.key();
                }
            }

            // go right
            AbstractNode<K,V> oldRight;
            while(true) {
                synchronized (this) {
                    if (this.right == null) {
                        return null;
                    }
                    oldRight = this.right;
                }
                AbstractNodeChange<K,V> newRight = oldRight.delete(valueToRemove);
                synchronized (this) {
                    if (this.right == oldRight) {
                        if(newRight == null){
                            return null;
                        }
                        this.right = newRight.implement();
                        if (this.left != null && this.left instanceof AVLTree.AbstractBalancedNode<K,V> leftA && leftA.height > newRight.height + 1) {
                            // left heavy
                            if ( leftA.right() == null || (leftA.left() != null && leftA.left().height() >= leftA.right().height()) ) {
                                // LL
                                //return leftA.callRotateRight(this);
                                return this.rotateRightNew(leftA);
                            } else {
                                // LR
                                //return leftA.callRotateLeftRight(this);
                                return this.rotateLeftRightNew(leftA);
                            }
                        }
                        this.height = AbstractBalancedNode.calculateHeight(this.left, this.right);
                        return null;
                    }
                }
            }

        }

        @Override
        abstract AbstractNodeChange<K,V> change(AbstractNode<K,V> left, AbstractNode<K,V> right);

        abstract AbstractBalancedNode<K,V> create(AbstractNode<K,V> left, AbstractNode<K,V> right);

        // Insert a key into the AVL tree and return the new root of the subtree
        @Override
        AbstractNodeChange<K,V> insert(AbstractLeafNode<K,V> nodeToInsert) {
            int cmp = nodeToInsert.key.compareTo(this.key);
            if (cmp < 0){
                AbstractNode<K,V> oldLeft;
                while(true) {
                    synchronized (this) {
                        if (this.left == null) {
                            //return new SetNodeFactoryN<>(nodeToInsert, this.key, this.right);
                            return this.change(nodeToInsert, this.right);
                        }
                        oldLeft = this.left;
                    }
                    AbstractNodeChange<K,V> newLeft = oldLeft.insert(nodeToInsert);
                    synchronized (this) {
                        if (this.left != oldLeft) {
                            // another thread changed this.left ==> retry
                            continue;
                        }
                        // no other thread changed this.left in between
                        if(newLeft == null){
                            return null;
                        }
                        if ( //newLeft instanceof AVLTree.AbstractBalancedNode<K,K> newLeftA &&
                                newLeft.height - 1 > (this.right == null ? 0 : this.right.height())) {
                            // left heavy
                            if (newLeft.right == null || (newLeft.left != null && newLeft.left.height() >= newLeft.right.height())) {
                                // LL
                                //return this.rotateRightNew(newLeft, oldLeft);
                                //return newLeft.callRotateRight(this);
                                return this.rotateRightNew(newLeft);
                            }
                            // left.right HIGHER ==> LR
                            //return this.rotateLeftRightNew(newLeft, oldLeft);
                            //return newLeft.callRotateLeftRight(this);
                            return this.rotateLeftRightNew(newLeft);
                        }
                        this.left = newLeft.implement();
                        return null;
                        //return new SetNodeFactoryN<>(newLeft, this.key, this.right);
                    }
                }
            }
            if (cmp > 0){
                AbstractNode<K,V> oldRight;
                while(true) {
                    synchronized (this) {
                        if (this.right == null) {
                            return this.change(this.left, nodeToInsert);
                        }
                        oldRight = this.right;
                    }
                    AbstractNodeChange<K,V> newRight = oldRight.insert(nodeToInsert);
                    synchronized (this) {
                        if (this.right == oldRight) {
                            // no other thread changed this.right in between
                            if(newRight == null){
                                return null;
                            }
                            if (//newRight instanceof AVLTree.AbstractBalancedNode<K,K> newRightA &&
                                    newRight.height - 1 > (this.left == null ? 0 : this.left.height())){
                                // right heavy
                                if (newRight.left == null || (newRight.right != null && newRight.left.height() <= newRight.right.height())) {
                                    // RR
                                    //return this.rotateLeftNew(newRight, oldRight);
                                    //return newRight.callRotateLeft(this);
                                    return this.rotateLeftNew(newRight);
                                }
                                // right.left HIGHER ==> RL
                                //return this.rotateRightLeftNew(newRight, oldRight);
                                //return newRight.callRotateRightLeft(this);
                                return this.rotateRightLeftNew(newRight);
                            }
                            this.right = newRight.implement();
                            return null;
                            //return new SetNodeFactoryN<>(this.left, this.key, newRight);
                        }   // else: another thread changed this.left ==> retry
                    }
                }
            }
            // insert in this node ==> replace data
            synchronized (this) {
                this.updateValue(nodeToInsert.value());
                return null;//new SetNodeFactory<>(this.left, this.key, this.right);
            }

        }

        abstract void updateValue(V value);

        // LL
        private AbstractNodeChange<K,V> rotateRightNew(AbstractNodeChange<K,V> newRoot) {
            // Perform rotation
            // newRoot is new ==> we can change it
            newRoot.right = this.create(newRoot.right, this.right);
            // Update heights
            newRoot.height = AbstractBalancedNode.calculateHeight(newRoot.left, newRoot.right);
            return newRoot;
        }

        // LL
        private AbstractNodeChange<K,V> rotateRightNew(AbstractBalancedNode<K,V> newRoot) {
            // Perform rotation
            AbstractBalancedNode<K,V> newThis = this.create(newRoot.right, this.right);
            // newRoot is not new ==> make it new
            return newRoot.change(newRoot.left, newThis);
        }

        // RR
        private AbstractNodeChange<K,V> rotateLeftNew(AbstractNodeChange<K,V> newRoot) {
            // newRoot is new ==> we can change it
            // Perform rotation
            newRoot.left = this.create(this.left, newRoot.left);
            newRoot.height = AbstractBalancedNode.calculateHeight(newRoot.left, newRoot.right);
            return newRoot;
        }

        // RR
        private AbstractNodeChange<K,V> rotateLeftNew(AbstractBalancedNode<K,V> newRoot) {
            // Perform rotation
            AbstractBalancedNode<K,V> newThis = this.create(this.left, newRoot.left);
            // newRoot is not new ==> make it new
            return newRoot.change(newThis, newRoot.right);
        }

        // LR
        private AbstractNodeChange<K,V> rotateLeftRightNew(AbstractNodeChange<K,V> newLeft) {
            // newLeft is new ==> update it
            AbstractNode<K,V> newLeftRight = newLeft.right; // but remember data before update
            newLeft.right = newLeft.right.left();
            newLeft.height = AbstractBalancedNode.calculateHeight(newLeft.left, newLeft.right);
            return newLeftRight.change(
                    newLeft.implement(),
                    this.create(newLeftRight.right(), this.right)
            );
        }

        // LR
        private AbstractNodeChange<K,V> rotateLeftRightNew(AbstractBalancedNode<K,V> newLeft) {
            // newLeft is not new ==> make it new
            return newLeft.right.change(
                    newLeft.create(newLeft.left, newLeft.right.left()),
                    this.create(newLeft.right.right(), this.right)
            );
        }

        // RL
        private AbstractNodeChange<K,V> rotateRightLeftNew(AbstractNodeChange<K,V> newRight) {
            // newRight is new ==> update it
            AbstractNode<K,V> newRightLeft = newRight.left; // but remember data before update
            newRight.left = newRight.left.right();
            newRight.height = AbstractBalancedNode.calculateHeight(newRight.left, newRight.right);
            return newRightLeft.change(
                    this.create(this.left, newRightLeft.left()),
                    newRight.implement()
            );
        }

        // RL
        private AbstractNodeChange<K,V> rotateRightLeftNew(AbstractBalancedNode<K,V> newRight) {
            // newRight is not new ==> make it new
            return newRight.left.change(
                    this.create(this.left, newRight.left.left()),
                    newRight.create(newRight.left.right(), newRight.right)
            );
        }

    }

    private static abstract class AbstractLeafNode<K extends Comparable<K>, V> extends AbstractNode<K,V> {

        private K key;
        //protected int height;

        AbstractLeafNode(K key) {
            this.key = key;
        }

        @Override
        // Insert a key into the AVL tree and return the new root of the subtree
        AbstractNodeChange<K,V> insert(AbstractLeafNode<K,V> nodeToInsert) {
            synchronized (this) {
                int cmp = nodeToInsert.key.compareTo(this.key);
                if (cmp < 0){
                    return this.change(nodeToInsert, null);
                }
                if (cmp > 0){
                    return this.change(null, nodeToInsert);
                }
                // replace data
                this.updateValue(nodeToInsert);
                return null;
            }
        }

        abstract void updateValue(AbstractLeafNode<K,V> source);

        @Override
        public abstract V value();

        @Override
        AbstractNode<K,V> find(K keyToFind) {
            if (keyToFind.compareTo(this.key) == 0) {
                return this;
            }
            return null;
        }

        @Override
        AbstractNodeChange<K,V> delete(K keyToDelete) {
            if (keyToDelete.compareTo(this.key) == 0) {
                // delete this
                return new NodeChangeToNull<>();
            }
            // nothing to do because keyToDelete was not found
            return null;
        }

        @Override
        AbstractNode<K,V> left() {
            return null;
        }

        @Override
        AbstractNode<K,V> right() {
            return null;
        }

        @Override
        int height() {
            return 1;
        }

        @Override
        public K key() {
            return this.key;
        }

    }

    private static abstract class AbstractTree<K extends Comparable<K>, V> {

        private AbstractNode<K,V> root = null;

        AbstractNode<K,V> getRoot(){
            return this.root;
        }

        void insert(AbstractLeafNode<K,V> valueToInsert) {

            while(true) {
                AbstractNode<K,V> theRoot;
                synchronized(this) {
                    if(this.root == null){
                        this.root = valueToInsert;
                        return;
                    }
                    theRoot = this.root;
                }
                AbstractNodeChange<K,V> newRoot = theRoot.insert(valueToInsert);
                synchronized (this) {
                    if (this.root != theRoot) {
                        // another thread changed this.root ==> retry
                        continue;
                    }
                    // no other thread changed this.root in between
                    if(newRoot == null){
                        return;
                    }
                    this.root = newRoot.implement();
                    return;
                }
            }

        }

        void deleteFromTree(K keyToDelete) {
            if(keyToDelete == null){
                //throw new NullPointerException("can not delete null");
                return;
            }
            while(true) {
                AbstractNode<K,V> theRoot;
                synchronized (this) {
                    if (this.root == null) {
                        return;
                    }
                    theRoot = this.root;
                }

                AbstractNodeChange<K,V> newRoot = theRoot.delete(keyToDelete);
                synchronized (this) {
                    if (this.root != theRoot) {
                        // another thread changed this.root ==> retry
                        continue;
                    }
                }
                // no other thread changed this.root in between
                if(newRoot == null){
                    return;
                }
                this.root = newRoot.implement();
                return;
            }
        }

        V findInTree(K keyToFind) {
            if(keyToFind == null){
                //throw new NullPointerException("can not find null");
                return null;
            }

            AbstractNode<K,V> theRoot;
            synchronized(this) {
                if(this.root == null){
                    return null;
                }
                theRoot = this.root;
            }

            AbstractNode<K,V> result = theRoot.find(keyToFind);
            if(result == null){
                return null;
            }
            return result.value();
        }

        // Utility functions for traversal
        void preOrder(AbstractNode<K,V> node) {
            if (node != null) {
                System.out.print(node.key() + " ");
                preOrder(node.left());
                preOrder(node.right());
            }
        }

        void inOrder(AbstractNode<K,V> node) {
            if (node != null) {
                inOrder(node.left());
                System.out.print(node.key() + " ");
                inOrder(node.right());
            }
        }

        void reverseOrder(AbstractNode<K,V> node) {
            if (node != null) {
                reverseOrder(node.right());
                System.out.print(node.key() + " ");
                reverseOrder(node.left());
            }
        }

        void postOrder(AbstractNode<K,V> node) {
            if (node != null) {
                postOrder(node.left());
                postOrder(node.right());
                System.out.print(node.key() + " ");
            }
        }

    }

    private static class NodeChangeToExisting<K extends Comparable<K>,V> extends AbstractNodeChange<K,V> {

        private final AbstractNode<K,V> alreadyCreatedNode;

        NodeChangeToExisting(AbstractNode<K,V> alreadyCreatedNode) {
            super(null, null, null);
            this.alreadyCreatedNode = alreadyCreatedNode;
        }

        @Override
        AbstractNode<K,V> implement() {
            return this.alreadyCreatedNode;
        }

    }

    private static class NodeChangeToNull<K extends Comparable<K>,V> extends AbstractNodeChange<K,V> {

        private NodeChangeToNull() {
            super(null, null, null);
        }

        @Override
        AbstractNode<K,V> implement() {
            return null;
        }

    }

    private static class SetNodeChange<K extends Comparable<K>> extends AbstractNodeChange<K,K> {

        SetNodeChange(AbstractNode<K,K> left, K key, AbstractNode<K,K> right) {
            super(left, key, right);
        }

        @Override
        AbstractNode<K,K> implement() {
            return new SetNode<K>(super.left, super.key, super.right, super.height);
        }

    }

    private static class SetNode<K extends Comparable<K>> extends AbstractBalancedNode<K,K> {

        private SetNode(AbstractNode<K,K> left, K key, AbstractNode<K,K> right) {
            super(left, key, right);
        }

        private SetNode(AbstractNode<K,K> left, K key, AbstractNode<K,K> right, int height) {
            super(left, key, right, height);
        }

        @Override
        void updateValue(K value) {
            // value is key. key can not be updated
        }

        @Override
        final AbstractNodeChange<K,K> change(AbstractNode<K,K> left, AbstractNode<K,K> right) {
            return new SetNodeChange<K>(left, super.key, right);
        }

        @Override
        AbstractBalancedNode<K,K> create(AbstractNode<K,K> left, AbstractNode<K,K> right) {
            return new SetNode<K>(left, super.key, right);
        }

        @Override
        public K value() {
            return super.key;
        }

    }

    private static class SetLeafNode<K extends Comparable<K>> extends AbstractLeafNode<K,K> {

        SetLeafNode(K valueToInsert) {
            super(valueToInsert);
        }

        @Override
        void updateValue(AbstractLeafNode<K,K> source) {
            // value is key. key can not be updated
        }

        @Override
        public K value() {
            return super.key;
        }

        @Override
        AbstractNodeChange<K,K> change(AbstractNode<K,K> left, AbstractNode<K,K> right) {
            return new SetNodeChange<K>(left, super.key, right);
        }

    }

    public static class Set<K extends Comparable<K>> extends AbstractTree<K,K> implements Iterable<K> {

        void insertIntoTree(K keyToInsert) {
            if(keyToInsert == null){
                //throw new NullPointerException("can not insert null");
                return;
            }
            super.insert(new SetLeafNode<>(keyToInsert));
        }

        @Override
        public Iterator<K> iterator() {
            return new SetIterator<>(this.getRoot());
        }

        public Iterable<K> reverseIterable() {
            return () -> new ReverseSetIterator<>(this.getRoot());
        }

        public Iterator<K> reverseIterator() {
            return new ReverseSetIterator<>(this.getRoot());
        }

    }

    private static class MapNodeChange<K extends Comparable<K>,V> extends AbstractNodeChange<K,V> {

        private final V value;

        MapNodeChange(AbstractNode<K,V> left, K key, V value, AbstractNode<K,V> right) {
            super(left, key, right);
            this.value = value;
        }

        @Override
        AbstractNode<K,V> implement() {
            return new MapNode<K,V>(super.left, super.key, this.value, super.right, super.height);
        }

    }

    private static class MapNode<K extends Comparable<K>, V> extends AbstractBalancedNode<K,V> {

        private V value;

        private MapNode(AbstractNode<K,V> left, K key, V value, AbstractNode<K,V> right) {
            super(left, key, right);
            this.value = value;
        }

        private MapNode(AbstractNode<K,V> left, K key, V value, AbstractNode<K,V> right, int height) {
            super(left, key, right, height);
            this.value = value;
        }

        @Override
        void updateValue(V value) {
            this.value = value;
        }

        @Override
        public V value() {
            return this.value;
        }

        @Override
        AbstractNodeChange<K,V> change(AbstractNode<K,V> left, AbstractNode<K,V> right) {
            return new MapNodeChange<K,V>(left, super.key, this.value, right);
        }

        @Override
        AbstractBalancedNode<K,V> create(AbstractNode<K,V> left, AbstractNode<K,V> right) {
            return new MapNode<K,V>(left, super.key, this.value, right);
        }

    }

    private static class MapLeafNode<K extends Comparable<K>, V> extends AbstractLeafNode<K,V> {

        private V value;

        private MapLeafNode(K key, V value) {
            super(key);
            this.value = value;
        }

        @Override
        void updateValue(AbstractLeafNode<K,V> source) {
            super.key = source.key;
            this.value = source.value();
        }

        @Override
        public V value() {
            return this.value;
        }

        @Override
        AbstractNodeChange<K,V> change(AbstractNode<K,V> left, AbstractNode<K,V> right) {
            return new MapNodeChange<K,V>(left, super.key, this.value, right);
        }

    }

    public static class Map<K extends Comparable<K>, V> extends AbstractTree<K,V> implements Iterable<Entry<K,V>> {

        void insertIntoTree(K keyToInsert, V valueToInsert) {
            if(keyToInsert == null){
                //throw new NullPointerException("can not insert null");
                return;
            }
            super.insert(new MapLeafNode<>(keyToInsert, valueToInsert));
        }

        @Override
        public Iterator<Entry<K,V>> iterator() {
            return new MapIterator<>(this.getRoot());
        }

        public Iterable<Entry<K,V>> reverseIterable() {
            return () -> new ReverseMapIterator<>(this.getRoot());
        }

        public Iterator<Entry<K,V>> reverseIterator() {
            return new ReverseMapIterator<>(this.getRoot());
        }

    }

    public static void main(String[] args) {
        Set<Integer> tree = new Set<>();

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

    private static record LinkedListEntry<K extends Comparable<K>, V>(
            AbstractNode<K, V> value,
            LinkedListEntry<K, V> next
    ) {}


    // InOrder Node iterator (yields AbstractNode<K,V>) — keine imports, vollqualifizierte Typen
    private static class NodeIterator<K extends Comparable<K>, V>  {

        //private final java.util.Deque<AbstractNode<K,V>> stack = new java.util.ArrayDeque<>();
        private LinkedListEntry<K,V> stack;

        NodeIterator(AbstractNode<K,V> root) {
            pushLeft(root);
        }

        private void pushLeft(AbstractNode<K,V> node) {
            while (node != null) {
                this.stack = new LinkedListEntry<>(node, stack);
                node = node.left();
            }
        }

        public boolean hasNext() {
            return this.stack != null;
        }

        public Entry<K,V> nextNode() {
            if (!hasNext()) {
                //throw new NoSuchElementException();
                return null;
            }
            AbstractNode<K,V> node = this.stack.value;
            this.stack = this.stack.next;
            pushLeft(node.right());
            return node;
        }

    }

    private static class ReverseNodeIterator<K extends Comparable<K>, V> {

        private LinkedListEntry<K,V> stack;

        ReverseNodeIterator(AbstractNode<K,V> root) {
            pushRight(root);
        }

        private void pushRight(AbstractNode<K,V> node) {
            while (node != null) {
                this.stack = new LinkedListEntry<>(node, stack);
                node = node.right();
            }
        }

        public boolean hasNext() {
            return this.stack != null;
        }

        public AbstractNode<K,V> nextNode() {
            if (!hasNext()) {
                //throw new NoSuchElementException();
                return null;
            }
            AbstractNode<K,V> node = this.stack.value;
            this.stack = this.stack.next;
            pushRight(node.left());
            return node;
        }

    }

    public static class SetIterator<K extends Comparable<K>> extends NodeIterator<K, K> implements java.util.Iterator<K> {

        SetIterator(AbstractNode<K, K> root) {
            super(root);
        }

        @Override
        public K next() {
            Entry<K,K> node = super.nextNode();
            return node.key();
        }
    }

    public static class ReverseSetIterator<K extends Comparable<K>> extends ReverseNodeIterator<K, K> implements java.util.Iterator<K> {

        ReverseSetIterator(AbstractNode<K, K> root) {
            super(root);
        }

        @Override
        public K next() {
            AbstractNode<K,K> node = super.nextNode();
            return node.key();
        }
    }

    public static class MapIterator<K extends Comparable<K>, V> extends NodeIterator<K, V> implements java.util.Iterator<Entry<K,V>> {

        MapIterator(AbstractNode<K, V> root) {
            super(root);
        }

        @Override
        public Entry<K, V> next() {
            return super.nextNode();
        }
    }

    public static class ReverseMapIterator<K extends Comparable<K>, V> extends ReverseNodeIterator<K, V> implements java.util.Iterator<Entry<K,V>> {

        ReverseMapIterator(AbstractNode<K, V> root) {
            super(root);
        }

        @Override
        public Entry<K, V> next() {
            return super.nextNode();
        }
    }

}