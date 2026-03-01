package org.conetex.runtime.instrument.collection;

import java.util.Iterator;

/**
 * A utility container class that provides an in-memory, self-balancing AVL tree
 * implementation and related types.  This class groups the tree node
 * abstractions and two concrete public data structures:
 * {@link AvlTree.Set} (a set of keys) and {@link AvlTree.Map} (a key/value map),
 * together with iterators and entry types used to traverse and access stored
 * elements.
 *
 * <p><b>Behavior</b>
 * <ul>
 *   <li>Elements are ordered by their natural ordering (keys must implement {@code Comparable}).</li>
 *   <li>Insert operations replace existing entries with equal keys rather than creating duplicates.</li>
 *   <li>Null keys are ignored by the public insert/delete/find entry points (no {@code NullPointerException} is thrown by those methods).</li>
 * </ul>
 *
 * <p><b>Threading</b>
 * <p>The implementation uses internal synchronization at the tree and node level
 * to coordinate concurrent modifications.  Typical operations (insert, delete,
 * find, and iteration via the provided iterators) are implemented with
 * synchronization to allow safe concurrent use in common scenarios; however,
 * callers should not rely on external atomicity across multiple method calls
 * (for example, a separate check-then-act sequence) without their own
 * synchronization.
 *
 * <p><b>Performance</b>
 * <p>Operations maintain AVL balance invariants; expected time complexity for
 * single-key operations (insert, delete, find) is O(log n) where n is the
 * number of elements in the tree.
 *
 * <p><b>Usage</b>
 * <pre>
 *   AvlTree.Set&lt;Integer&gt; set = new AvlTree.Set&lt;&gt;();
 *   set.insertIntoTree(42);
 *
 *   AvlTree.Map&lt;String, Integer&gt; map = new AvlTree.Map&lt;&gt;();
 *   map.insertIntoTree("key", 123);
 * </pre>
 *
 * @see AvlTree.Set
 * @see AvlTree.Map
 * @see AvlTree.Entry
 */
public class AvlTree {

    public interface ValueFactory<V> {
        V create();
    }

    public interface ValueUpdate<V> {
        V update(V existingValue);
    }

    private AvlTree(){}

    /**
     * Represents a key/value pair stored in the AVL tree.
     *
     * @param <K> the key type, must be Comparable
     * @param <V> the value type
     */
    public interface Entry<K extends Comparable<K>, V> {

        /**
         * Returns the key associated with this entry.
         *
         * @return the key of this entry
         */
        K key();

        /**
         * Returns the value associated with this entry.
         *
         * @return the value of this entry
         */
        V value();

    }

    static abstract class AbstractNode<K extends Comparable<K>, V> implements Entry<K, V>
    {

        abstract int height();

        abstract AbstractNode<K,V> left();

        abstract AbstractNode<K,V> right();

        abstract AbstractNodeChange<K,V> insert(K keyToInsert, ValueFactory<V> v, ValueUpdate<V> u);

        abstract AbstractNode<K,V> find(K keyToFind);

        abstract AbstractNodeChange<K,V> delete(K keyToDelete);

        abstract AbstractNodeChange<K,V> change(AbstractNode<K,V> left, AbstractNode<K,V> right);

        abstract AbstractLeafNode<K,V> createLeaf(K keyToInsert, ValueFactory<V> valueFactory);

        abstract void updateValue(ValueUpdate<V> valueUpdater);

        abstract void setValue(V value);

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

        /**
         * Returns the key stored in this balanced node.
         *
         * @return the node key
         */
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
                            if (this.right != null && this.right instanceof AvlTree.AbstractBalancedNode<K,V> rightA && rightA.height > newLeft.height + 1) {
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
                    this.key = newValueNode.key();
                    this.setValue(newValueNode.value());
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
                        if (this.left != null && this.left instanceof AvlTree.AbstractBalancedNode<K,V> leftA && leftA.height > newRight.height + 1) {
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
        AbstractNodeChange<K,V> insert(K keyToInsert, ValueFactory<V> v, ValueUpdate<V> u) {
            int cmp = keyToInsert.compareTo(this.key);
            if (cmp < 0){
                AbstractNode<K,V> oldLeft;
                while(true) {
                    synchronized (this) {
                        if (this.left == null) {
                            //return new SetNodeFactoryN<>(nodeToInsert, this.key, this.right);
                            return this.change(this.createLeaf(keyToInsert, v), this.right);
                        }
                        oldLeft = this.left;
                    }
                    AbstractNodeChange<K,V> newLeft = oldLeft.insert(keyToInsert, v, u);
                    synchronized (this) {
                        if (this.left != oldLeft) {
                            // another thread changed this.left ==> retry
                            continue;
                        }
                        // no other thread changed this.left in between
                        if(newLeft == null){
                            return null;
                        }
                        if ( //newLeft instanceof AvlTree.AbstractBalancedNode<K,K> newLeftA &&
                                newLeft.height - 1 > (this.right == null ? 0 : this.right.height())) {
                            // left heavy
                            if (newLeft.right == null || (newLeft.left != null && newLeft.left.height() >= newLeft.right.height())) {
                                // LL
                                return this.rotateRightNew(newLeft);
                            }
                            // left.right HIGHER ==> LR
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
                            return this.change(this.left, this.createLeaf(keyToInsert, v));
                        }
                        oldRight = this.right;
                    }
                    AbstractNodeChange<K,V> newRight = oldRight.insert(keyToInsert, v, u);
                    synchronized (this) {
                        if (this.right == oldRight) {
                            // no other thread changed this.right in between
                            if(newRight == null){
                                return null;
                            }
                            if (//newRight instanceof AvlTree.AbstractBalancedNode<K,K> newRightA &&
                                    newRight.height - 1 > (this.left == null ? 0 : this.left.height())){
                                // right heavy
                                if (newRight.left == null || (newRight.right != null && newRight.left.height() <= newRight.right.height())) {
                                    // RR
                                    return this.rotateLeftNew(newRight);
                                }
                                // right.left HIGHER ==> RL
                                return this.rotateRightLeftNew(newRight);
                            }
                            this.right = newRight.implement();
                            return null;
                        }   // else: another thread changed this.left ==> retry
                    }
                }
            }
            // insert in this node ==> replace data
            synchronized (this) {
                this.updateValue( u );
                return null;//new SetNodeFactory<>(this.left, this.key, this.right);
            }

        }

        abstract void updateValue(ValueUpdate<V> valueUpdater);

        abstract void setValue(V newValue);

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
        AbstractNodeChange<K,V> insert(K keyToInsert, ValueFactory<V> v, ValueUpdate<V> u) {
            synchronized (this) {
                int cmp = keyToInsert.compareTo(this.key);
                if (cmp < 0){
                    return this.change(this.createLeaf(keyToInsert, v), null);
                }
                if (cmp > 0){
                    return this.change(null, this.createLeaf(keyToInsert, v));
                }
                // replace data
                this.updateValue( u );
                return null;
            }
        }

        /**
         * Returns the value stored in this leaf node.
         *
         * @return the leaf node value
         */
        @Override
        public abstract V value();

        abstract void updateValue(ValueUpdate<V> valueUpdater);

        abstract void setValue(V v);

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

        abstract AbstractLeafNode<K,V> create(K keyToInsert, ValueFactory<V> valueFactory);

        void insert(K keyToInsert, ValueFactory<V> v, ValueUpdate<V> u) {

            while(true) {
                AbstractNode<K,V> theRoot;
                synchronized(this) {
                    if(this.root == null){
                        this.root = this.create(keyToInsert, v);
                        return;
                    }
                    theRoot = this.root;
                }
                AbstractNodeChange<K,V> newRoot = theRoot.insert(keyToInsert, v, u);
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

        //abstract AbstractLeafNode<K,V> createNewLeaf(K key, V value);

        private AbstractNode<K,V> find(K keyToFind) {
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

            return theRoot.find(keyToFind);
        }

        public V findInTree(K keyToFind) {
            AbstractNode<K,V> result = this.find(keyToFind);
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
            return new SetNode<>(super.left, super.key, super.right, super.height);
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
        void updateValue(ValueUpdate<K> valueUpdater) {
            // value is key. key can not be updated
        }

        @Override
        void setValue(K value) {
            // value is key. key can not be updated
        }

        @Override
        final AbstractNodeChange<K,K> change(AbstractNode<K,K> left, AbstractNode<K,K> right) {
            return new SetNodeChange<>(left, super.key, right);
        }

        @Override
        SetLeafNode<K> createLeaf(K keyToInsert, ValueFactory<K> valueFactory) {
            return new SetLeafNode<>(keyToInsert);
        }

        @Override
        AbstractBalancedNode<K,K> create(AbstractNode<K,K> left, AbstractNode<K,K> right) {
            return new SetNode<>(left, super.key, right);
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
        void updateValue(ValueUpdate<K> valueUpdater) {
            // value is key. key can not be updated
        }

        @Override
        void setValue(K value) {
            // value is key. key can not be updated
        }

        @Override
        public K value() {
            return super.key;
        }

        @Override
        AbstractNodeChange<K,K> change(AbstractNode<K,K> left, AbstractNode<K,K> right) {
            return new SetNodeChange<>(left, super.key, right);
        }

        @Override
        SetLeafNode<K> createLeaf(K keyToInsert, ValueFactory<K> valueFactory) {
            return new SetLeafNode<>(keyToInsert);
        }

    }

    /**
     * A set backed by an AVL tree. Elements are stored as keys; duplicates replace existing entries.
     *
     * @param <K> the element type, must implement Comparable
     */
    public static class Set<K extends Comparable<K>> extends AbstractTree<K,K> implements Iterable<K> {

        /**
         * Creates a new, empty AvlTree-backed set.
         *
         * <p>The constructed set is initially empty and ready to accept elements via
         * {@link AvlTree.Set#insertIntoTree(K)}. Elements are ordered by their
         * natural ordering (keys must implement {@code Comparable}).
         *
         * <p>Note: this constructor runs in constant time and performs no I/O. The
         * set's instance methods use internal synchronization for concurrent access;
         * callers should apply external synchronization if they require atomicity
         * across multiple operations.
         */
        public Set() {}

        /**
         * Insert a key into the set-backed AVL tree.
         * If the key is null, the method returns without inserting.
         *
         * @param keyToInsert the key to insert into the tree
         */
        void insertIntoTree(K keyToInsert) {
            if(keyToInsert == null){
                //throw new NullPointerException("can not insert null");
                return;
            }
            super.insert(
                keyToInsert,
                ()->{return keyToInsert;},
                (K existingKey)->{return existingKey;}
            );
        }

        /**
         * Returns an iterator over the elements in this set in ascending (in-order) order.
         *
         * @return an iterator over the keys in ascending order
         */
        @Override
        public Iterator<K> iterator() {
            return new SetIterator<>(this.getRoot());
        }

        /**
         * Returns an iterable that iterates over the elements in descending (reverse in-order) order.
         *
         * @return an iterable for reverse-order traversal of keys
         */
        public Iterable<K> reverseIterable() {
            return () -> new ReverseSetIterator<>(this.getRoot());
        }

        /**
         * Returns an iterator that iterates over the elements in descending (reverse in-order) order.
         *
         * @return an iterator for reverse-order traversal of keys
         */
        public Iterator<K> reverseIterator() {
            return new ReverseSetIterator<>(this.getRoot());
        }

        @Override
        SetLeafNode<K> create(K key, ValueFactory<K> valueFactory) {
            return new SetLeafNode<>(key);
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
            return new MapNode<>(super.left, super.key, this.value, super.right, super.height);
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
        void updateValue(ValueUpdate<V> valueUpdater) {
            this.value = valueUpdater.update(this.value);
        }

        @Override
        void setValue(V value) {
            this.value = value;
        }

        @Override
        public V value() {
            return this.value;
        }

        @Override
        AbstractNodeChange<K,V> change(AbstractNode<K,V> left, AbstractNode<K,V> right) {
            return new MapNodeChange<>(left, super.key, this.value, right);
        }

        @Override
        MapLeafNode<K, V> createLeaf(K keyToInsert, ValueFactory<V> valueFactory) {
            return new MapLeafNode<>(keyToInsert, valueFactory.create());
        }

        @Override
        AbstractBalancedNode<K,V> create(AbstractNode<K,V> left, AbstractNode<K,V> right) {
            return new MapNode<>(left, super.key, this.value, right);
        }

    }

    private static class MapLeafNode<K extends Comparable<K>, V> extends AbstractLeafNode<K,V> {

        private V value;

        private MapLeafNode(K key, V value) {
            super(key);
            this.value = value;
        }

        @Override
        void updateValue(ValueUpdate<V> valueUpdater) {
            this.value = valueUpdater.update(this.value);
        }

        @Override
        void setValue(V value) {
            this.value = value;
        }

        @Override
        public V value() {
            return this.value;
        }

        @Override
        AbstractNodeChange<K,V> change(AbstractNode<K,V> left, AbstractNode<K,V> right) {
            return new MapNodeChange<>(left, super.key, this.value, right);
        }

        @Override
        MapLeafNode<K, V> createLeaf(K keyToInsert, ValueFactory<V> valueFactory) {
            return new MapLeafNode<>(keyToInsert, valueFactory.create());
        }

    }

    /**
     * A map backed by an AVL tree. Keys are ordered by their natural ordering.
     *
     * @param <K> the key type, must implement Comparable
     * @param <V> the value type
     */
    public static class Map<K extends Comparable<K>, V> extends AbstractTree<K,V> implements Iterable<Entry<K,V>> {

        /**
         * Creates a new, empty AvlTree-backed map.
         *
         * <p>The constructed map is initially empty and ready to accept key/value
         * pairs via {@link AvlTree.Map#insertIntoTree(K, V)}. Keys are
         * ordered by their natural ordering (they must implement {@code Comparable}).
         *
         * <p>Note: this constructor does not perform any I/O and runs in constant
         * time. The map's instance methods use internal synchronization for
         * concurrent access; callers should still apply external synchronization if
         * they require atomicity across multiple operations.
         */
        public Map() {}

        /**
         * Insert a key/value pair into the map-backed AVL tree.
         * If the key is null, the method returns without inserting.
         *
         * @param keyToInsert the key to insert
         * @param valueToInsert the value associated with the key
         */
        void insertIntoTree(K keyToInsert, V valueToInsert) {
            if(keyToInsert == null){
                //throw new NullPointerException("can not insert null");
                return;
            }
            super.insert(
                keyToInsert,
                ()    -> {return valueToInsert;},
                (V a) -> {return valueToInsert;}
            );
        }

        public void insertIntoTree(K keyToInsert, ValueFactory<V> v, ValueUpdate<V> u) {
            if(keyToInsert == null){
                //throw new NullPointerException("can not insert null");
                return;
            }
            super.insert(
                    keyToInsert,
                    v,
                    u
            );
        }

        /**
         * Returns an iterator over the map entries in ascending (in-order) key order.
         *
         * @return an iterator over entries (key/value pairs) in ascending key order
         */
        @Override
        public Iterator<Entry<K,V>> iterator() {
            return new MapIterator<>(this.getRoot());
        }

        /**
         * Returns an iterable that iterates over the map entries in descending (reverse in-order) key order.
         *
         * @return an iterable for reverse-order traversal of entries
         */
        public Iterable<Entry<K,V>> reverseIterable() {
            return () -> new ReverseMapIterator<>(this.getRoot());
        }

        /**
         * Returns an iterator that iterates over the map entries in descending (reverse in-order) key order.
         *
         * @return an iterator for reverse-order traversal of entries
         */
        public Iterator<Entry<K,V>> reverseIterator() {
            return new ReverseMapIterator<>(this.getRoot());
        }

        @Override
        MapLeafNode<K, V> create(K keyToInsert, ValueFactory<V> valueFactory) {
            return new MapLeafNode<>(keyToInsert, valueFactory.create());
        }

    }

    private record LinkedListEntry<K extends Comparable<K>, V>(
            AbstractNode<K, V> value,
            LinkedListEntry<K, V> next
    ) {}

    private static abstract class AbstractNodeIterator<K extends Comparable<K>, V>  {

        private LinkedListEntry<K,V> stack;

        AbstractNodeIterator() {}

        /**
         * Returns {@code true} if the reverse-order iterator has more elements to yield.
         *
         * <p>This method inspects the iterator's internal stack to determine whether
         * there are remaining nodes to traverse in reverse in-order sequence.
         *
         * <p>Note: the result reflects the iterator's current state and may change if
         * the underlying tree is modified concurrently. The iterator's methods are not
         * safe for concurrent modification without external synchronization.
         *
         * @return {@code true} if there is at least one more element available, {@code false} otherwise
         */
        public final boolean hasNext() {
            return this.stack != null;
        }

    }

    // InOrder Node iterator (yields AbstractNode<K,V>) — no imports, full qualified types
    private static class NodeIterator<K extends Comparable<K>, V> extends AbstractNodeIterator<K,V> {

        NodeIterator(AbstractNode<K,V> root) {
            pushLeft(root);
        }

        private void pushLeft(AbstractNode<K,V> node) {
            while (node != null) {
                super.stack = new LinkedListEntry<>(node, super.stack);
                node = node.left();
            }
        }

        /**
         * Returns the next node in in-order traversal and advances the iterator.
         *
         * <p>If the iterator has no more elements this method returns {@code null}
         * (the implementation does not throw {@code NoSuchElementException}).
         *
         * <p>The returned object implements {@link AvlTree.Entry} and provides access
         * to the node's key and value. The iterator's internal stack is advanced so
         * subsequent calls continue the traversal.
         *
         * <p>Note: the result reflects the iterator's state at the time of the call
         * and may become invalid if the underlying tree is modified concurrently.
         *
         * @return the next {@code Entry<K,V>} in ascending (in-order) order, or {@code null} if none
         */
        public Entry<K,V> nextNode() {
            if (hasNext()) {
                AbstractNode<K,V> node = super.stack.value;
                super.stack = super.stack.next;
                pushLeft(node.right());
                return node;
            }
            //throw new NoSuchElementException();
            return null;
        }

    }

    private static class ReverseNodeIterator<K extends Comparable<K>, V> extends AbstractNodeIterator<K,V> {

        ReverseNodeIterator(AbstractNode<K,V> root) {
            pushRight(root);
        }

        private void pushRight(AbstractNode<K,V> node) {
            while (node != null) {
                super.stack = new LinkedListEntry<>(node, super.stack);
                node = node.right();
            }
        }

        /**
         * Returns the next node in reverse in-order traversal and advances the iterator.
         *
         * <p>If the iterator has no more elements this method returns {@code null}
         * (the implementation does not throw {@code NoSuchElementException}).
         *
         * <p>The returned value is the internal {@code AbstractNode<K,V>} representing
         * the next element; callers can use {@code key()} and {@code value()} on the
         * returned node. The iterator's internal stack is advanced so subsequent calls
         * continue the reverse traversal.
         *
         * <p>Note: the result reflects the iterator's state at the time of the call
         * and may become invalid if the underlying tree is modified concurrently.
         *
         * @return the next {@code AbstractNode<K,V>} in descending (reverse in-order) order, or {@code null} if none
         */
        public AbstractNode<K,V> nextNode() {
            if (!hasNext()) {
                //throw new NoSuchElementException();
                return null;
            }
            AbstractNode<K,V> node = super.stack.value;
            super.stack = super.stack.next;
            pushRight(node.left());
            return node;
        }

    }

    /**
     * Iterator over set elements in ascending order.
     *
     * @param <K> element type
     */
    public static class SetIterator<K extends Comparable<K>> extends NodeIterator<K, K> implements java.util.Iterator<K> {

        SetIterator(AbstractNode<K, K> root) {
            super(root);
        }

        /**
         * Returns the next key in ascending order.
         *
         * @return the next key
         */
        @Override
        public K next() {
            Entry<K,K> node = super.nextNode();
            return node.key();
        }
    }

    /**
     * Iterator over set elements in descending order.
     *
     * @param <K> element type
     */
    public static class ReverseSetIterator<K extends Comparable<K>> extends ReverseNodeIterator<K, K> implements java.util.Iterator<K> {

        ReverseSetIterator(AbstractNode<K, K> root) {
            super(root);
        }

        /**
         * Returns the next key in descending order.
         *
         * @return the next key
         */
        @Override
        public K next() {
            AbstractNode<K,K> node = super.nextNode();
            return node.key();
        }
    }

    /**
     * Iterator over map entries in ascending key order.
     *
     * @param <K> key type
     * @param <V> value type
     */
    public static class MapIterator<K extends Comparable<K>, V> extends NodeIterator<K, V> implements java.util.Iterator<Entry<K,V>> {

        MapIterator(AbstractNode<K, V> root) {
            super(root);
        }

        /**
         * Returns the next map entry (key/value pair) in ascending key order.
         *
         * @return the next entry
         */
        @Override
        public Entry<K, V> next() {
            return super.nextNode();
        }
    }

    /**
     * Iterator over map entries in descending key order.
     *
     * @param <K> key type
     * @param <V> value type
     */
    public static class ReverseMapIterator<K extends Comparable<K>, V> extends ReverseNodeIterator<K, V> implements java.util.Iterator<Entry<K,V>> {

        ReverseMapIterator(AbstractNode<K, V> root) {
            super(root);
        }

        /**
         * Returns the next map entry (key/value pair) in descending key order.
         *
         * @return the next entry
         */
        @Override
        public Entry<K, V> next() {
            return super.nextNode();
        }
    }

}