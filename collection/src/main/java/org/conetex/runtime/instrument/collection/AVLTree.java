package org.conetex.runtime.instrument.collection;

public class AVLTree<T extends Comparable<T>> {

    public static class Set<T extends Comparable<T>> {

        private Node<T,T> root;

        public void insertIntoTree(T valueToInsert) {
            if(valueToInsert == null){
                throw new NullPointerException("can not insert null");
            }

            Node<T, T> theRoot;
            synchronized(this) {
                if(this.root == null){
                    this.root = new Key<>(valueToInsert);
                    return;
                }
                theRoot = this.root;
            }

            Node<T,T> newRoot = theRoot.insert(new Key<>(valueToInsert));
            synchronized(this) {
                this.root = newRoot;
            }
        }

        public void deleteFromTree(T keyToDelete) {
            if(keyToDelete == null){
                throw new NullPointerException("can not delete null");
            }

            Node<T,T> theRoot;
            synchronized(this) {
                if(this.root == null){
                    return;
                }
                theRoot = this.root;
            }

            Node<T,T> newRoot = theRoot.delete(keyToDelete);
            synchronized(this) {
                this.root = newRoot;
            }
        }

        public T findInTree(T keyToFind) {
            if(keyToFind == null){
                throw new NullPointerException("can not find null");
            }

            Node<T,T> theRoot;
            synchronized(this) {
                if(this.root == null){
                    return null;
                }
                theRoot = this.root;
            }

            Node<T,T> result = theRoot.find(keyToFind);
            if(result == null){
                return null;
            }
            return result.value();
        }

        // Utility functions for traversal
        void preOrder(Node<?,?> node) {
            if (node != null) {
                System.out.print(node.key + " ");
                preOrder(node.left);
                preOrder(node.right);
            }
        }

        void inOrder(Node<?,?> node) {
            if (node != null) {
                inOrder(node.left);
                System.out.print(node.key + " ");
                inOrder(node.right);
            }
        }

        void postOrder(Node<?,?> node) {
            if (node != null) {
                postOrder(node.left);
                postOrder(node.right);
                System.out.print(node.key + " ");
            }
        }

        private static class Key<K extends Comparable<K>> extends Node<K, K> implements Entry<K, K>{

            private Key(K newKey, Node<K, K> left, Node<K, K> right) {
                super(newKey, left, right);
            }

            @Override
            Node<K, K> create(Node<K, K> left, Node<K, K> right) {
                return new Key<>(super.key, left, right);
            }

            private Key(K newKey) {
                super(newKey);
            }

            @Override
            Node<K, K> create() {
                return new Key<>(super.key);
            }

            @Override
            public K key() {
                return super.key;
            }

            @Override
            public K value() {
                return super.key;
            }

        }
    }

    public static class Map<T extends Comparable<T>, V> {

        private Node<T,V> root;

        public void insertIntoTree(T valueToInsert, V v) {
            if(valueToInsert == null){
                throw new NullPointerException("can not insert null");
            }

            Node<T, V> theRoot;
            synchronized(this) {
                if(this.root == null){
                    this.root = new KeyValue<T,V>(valueToInsert, v);
                    return;
                }
                theRoot = this.root;
            }

            Node<T,V> newRoot = theRoot.insert(new KeyValue<T,V>(valueToInsert, v));
            synchronized(this) {
                this.root = newRoot;
            }
        }

        public void deleteFromTree(T keyToDelete) {
            if(keyToDelete == null){
                throw new NullPointerException("can not delete null");
            }

            Node<T,V> theRoot;
            synchronized(this) {
                if(this.root == null){
                    return;
                }
                theRoot = this.root;
            }

            Node<T,V> newRoot = theRoot.delete(keyToDelete);
            synchronized(this) {
                this.root = newRoot;
            }
        }

        public V findInTree(T keyToFind) {
            if(keyToFind == null){
                throw new NullPointerException("can not find null");
            }

            Node<T,V> theRoot;
            synchronized(this) {
                if(this.root == null){
                    return null;
                }
                theRoot = this.root;
            }

            Node<T,V> result = theRoot.find(keyToFind);
            if(result == null){
                return null;
            }
            return result.value();
        }

        private static class KeyValue<K extends Comparable<K>, V> extends Node<K, V> implements Entry<K, V>{

            private V value;

            private KeyValue(K newKey, V newValue, Node<K, V> left, Node<K, V> right) {
                super(newKey, left, right);
                this.value = newValue;
            }

            private KeyValue(K newKey, V newValue) {
                super(newKey);
                this.value = newValue;
            }

            Node<K, V> create(Node<K, V> left, Node<K, V> right) {
                return new KeyValue<>(super.key, this.value, left, right);
            }

            Node<K, V> create() {
                return new KeyValue<>(super.key, this.value);
            }

            @Override
            public K key() {
                return super.key;
            }

            @Override
            public V value() {
                return this.value;
            }

        }
    }




    private static abstract class Node<K extends Comparable<K>, V>{

        private K key;
        private int height;

        private Node<K, V> left;
        private Node<K, V> right;

        private Node(K newKey) {
            this.key = newKey;
            this.left = null;
            this.right = null;
            this.height = 1;
        }

        private Node(K newKey, Node<K, V> left, Node<K, V> right) {
            this.key = newKey;
            this.left = left;
            this.right = right;
            this.updateHeight();
        }

        abstract Node<K, V> create(Node<K, V> left, Node<K, V> right);

        abstract Node<K, V> create();

        public abstract V value();

        public K key() {
            return this.key;
        }

        // Insert a key into the AVL tree and return the new root of the subtree
        private Node<K, V> insert(Node<K, V> nodeToInsert) {
            int cmp = nodeToInsert.key.compareTo(this.key);
            if (cmp < 0){
                Node<K, V> oldLeft;
                while(true) {
                    synchronized (this) {
                        if (this.left == null) {
                            return this.setLeftUpdateHeight(nodeToInsert);
                        }
                        oldLeft = this.left;
                    }
                    Node<K, V> newLeft = oldLeft.insert(nodeToInsert);
                    synchronized (this) {
                        if (this.left != oldLeft) {
                            // another thread changed this.left ==> retry
                            continue;
                        }
                        // no other thread changed this.left in between
                        if (newLeft.height - 1 > (this.right == null ? 0 : this.right.height)) {
                            // left heavy
                            if (newLeft.right == null || (newLeft.left != null && newLeft.left.height >= newLeft.right.height)) {
                                // LL
                                return this.rotateRightNew(newLeft, oldLeft);
                            }
                            // left.right HIGHER ==> LR
                            return this.rotateLeftRightNew(newLeft, oldLeft);
                        }
                        return this.setLeftUpdateHeight(newLeft);
                    }
                }
            }
            if (cmp > 0){
                Node<K, V> oldRight;
                while(true) {
                    synchronized (this) {
                        if (this.right == null) {
                            return this.setRightUpdateHeight(nodeToInsert);
                        }
                        oldRight = this.right;
                    }
                    Node<K, V> newRight = oldRight.insert(nodeToInsert);
                    synchronized (this) {
                        if (this.right == oldRight) {
                            // no other thread changed this.right in between
                            if (newRight.height - 1 > (this.left == null ? 0 : this.left.height)) {
                                // right heavy
                                if (newRight.left == null || (newRight.right != null && newRight.left.height <= newRight.right.height)) {
                                    // RR
                                    return this.rotateLeftNew(newRight, oldRight);
                                }
                                // right.left HIGHER ==> RL
                                return this.rotateRightLeftNew(newRight, oldRight);
                            }
                            return this.setRightUpdateHeight(newRight);
                        }   // else: another thread changed this.left ==> retry
                    }
                }
            }
            // replace data
            this.key = nodeToInsert.key;
            synchronized (this) {
                nodeToInsert.left = this.left;
                nodeToInsert.right = this.right;
            }
            return nodeToInsert;
        }

        // LL
        private Node<K, V> rotateRightNew(Node<K, V> newRoot, Node<K, V> oldLeft) {

            // Perform rotation
            Node<K, V> newThis = this.create(newRoot.right, this.right);

            if(newRoot == oldLeft){
                // newRoot is not new ==> make it new
                return newRoot.create(newRoot.left, newThis);
            }
            else{
                // newRoot is new ==> we can change it
                newRoot.right = newThis;
                // Update heights
                newRoot.updateHeight();
                return newRoot;
            }

        }

        // RR
        private Node<K, V> rotateLeftNew(Node<K, V> newRoot, Node<K, V> oldRight) {

            // Perform rotation
            Node<K, V> newThis = this.create(this.left, newRoot.left);

            if(newRoot == oldRight){
                // newRoot is not new ==> make it new
                return newRoot.create(newThis, newRoot.right);
            }
            else{
                // newRoot is new ==> we can change it
                newRoot.left = newThis;
                newRoot.updateHeight();
                return newRoot;
            }

        }

        // LR
        private Node<K, V> rotateLeftRightNew(Node<K, V> newLeft, Node<K, V> oldLeft) {
            if(newLeft == oldLeft){
                // newLeft is not new ==> make it new
                return newLeft.right.create(
                        newLeft.create(newLeft.left, newLeft.right.left),
                        this.create(newLeft.right.right, this.right)
                );
            }
            else{
                // newLeft is new ==> update it
                Node<K, V> newLeftRight = newLeft.right; // but remember data before update
                newLeft.right = newLeft.right.left;
                newLeft.updateHeight();
                return newLeftRight.create(
                        newLeft,
                        this.create(newLeft.right.right, this.right)
                );
            }
        }

        // RL
        private Node<K, V> rotateRightLeftNew(Node<K, V> newRight, Node<K, V> oldRight) {
            if(newRight == oldRight){
                // newRight is not new ==> make it new
                return newRight.left.create(
                        this.create(this.left, newRight.left.left),
                        newRight.create(newRight.left.right, newRight.right)
                );
            }
            else{
                // newRight is new ==> update it
                Node<K, V> newRightLeft = newRight.left; // but remember data before update
                newRight.left = newRight.left.right;
                newRight.updateHeight();
                return newRightLeft.create(
                        this.create(this.left, newRight.left.left),
                        newRight
                );
            }
        }

        // rebalancing not needed
        private Node<K, V> setRightUpdateHeight(Node<K, V> newRight) {
            this.right = newRight;
            this.updateHeight();
            return this;
        }

        private Node<K, V> setLeftUpdateHeight(Node<K, V> newLeft) {
            this.left = newLeft;
            this.updateHeight();
            return this;
        }

        private void updateHeight() {
            if(this.left == null){
                if(this.right == null){
                    this.height = 1;
                }
                else{
                    this.height = 1 + this.right.height;
                }
            }
            else{
                if(this.right == null){
                    this.height = 1 + this.left.height;
                }
                else{
                    this.height = 1 + ((this.left.height > this.right.height) ? this.left.height : this.right.height);
                }
            }
        }

        private Node<K, V> delete(K valueToRemove) {
            int cmp = valueToRemove.compareTo(this.key);

            if (cmp < 0) {
                // go left
                Node<K, V> oldLeft, oldRight;
                while(true) {
                    synchronized (this) {
                        if (this.left == null) {
                            return this;
                        }
                        oldLeft = this.left;
                        oldRight = this.right;
                    }
                    Node<K, V> newLeft = oldLeft.delete(valueToRemove);
                    synchronized (this) {
                        if (this.left == oldLeft) {
                            this.left = newLeft;
                            if (this.right != null && this.right.height > ((newLeft == null) ? 0 : newLeft.height) + 1) {
                                // right heavy
                                if (this.right.left == null || (this.right.right != null && this.right.left.height <= this.right.right.height)) {
                                    // RR
                                    return rotateLeftNew(this.right, oldRight);
                                } else {
                                    // RL
                                    return rotateRightLeftNew(this.right, oldRight);
                                }
                            }
                            this.updateHeight();
                            return this;
                        }
                    }
                }
            }

            if (cmp == 0) {
                // found node
                synchronized (this) {
                    if (this.left == null && this.right == null) {
                        // delete this node - successor not needed
                        return null;
                    }
                    if (this.left == null) {
                        // delete this node - successor is right
                        return this.right;
                    }
                    if (this.right == null) {
                        // delete this node - successor is left
                        return this.left;
                    }
                    // delete this node - new value is smallest (left) at right
                    Node<K, V> newValueNode = this.right;
                    while (newValueNode.left != null) {
                        newValueNode = newValueNode.left;
                    }
                    // copy data from successor to this
                    this.key = newValueNode.key;
                    // remove successor
                    valueToRemove = newValueNode.key;
                }
            }

            // go right
            Node<K, V> oldLeft, oldRight;
            while(true) {
                synchronized (this) {
                    if (this.right == null) {
                        return this;
                    }
                    oldLeft = this.left;
                    oldRight = this.right;
                }
                Node<K, V> newRight = oldRight.delete(valueToRemove);
                synchronized (this) {
                    if (this.right == oldRight) {
                        this.right = newRight;
                        if ( this.left != null && this.left.height > ((newRight == null) ? 0 : newRight.height) + 1) {
                            // left heavy
                            if ( this.left.right == null || (this.left.left != null && this.left.left.height >= this.left.right.height) ) {
                                // LL
                                return this.rotateRightNew(this.left, oldLeft);
                            } else {
                                // LR
                                return rotateLeftRightNew(this.left, oldLeft);
                            }
                        }
                        this.updateHeight();
                        return this;
                    }
                }
            }


        }

        private Node<K, V> find(K valueToFind) {
            int cmp = valueToFind.compareTo(this.key);
            if (cmp < 0) {
                // go left
                if (this.left == null) {
                    return null;
                }
                else{
                    return this.left.find(valueToFind);
                }
            }
            if (cmp > 0) {
                // go right
                if (this.right == null) {
                    return null;
                }
                else{
                    return this.right.find(valueToFind);
                }
            }
            return this;
        }

    }



    public static void main(String[] args) {
        AVLTree.Set<Integer> tree = new AVLTree.Set<>();

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
        tree.preOrder(tree.root);
        System.out.println();

        System.out.println("Inorder traversal of constructed AVL tree is : ");
        tree.inOrder(tree.root);
        System.out.println();

        System.out.println("Postorder traversal of constructed AVL tree is : ");
        tree.postOrder(tree.root);
        System.out.println();

        tree.deleteFromTree(19);
        tree.deleteFromTree(6);

        tree.deleteFromTree(7);

        tree.deleteFromTree(4);
        tree.deleteFromTree(16);

        System.out.println("Preorder traversal of constructed AVL tree is : ");
        tree.preOrder(tree.root);
        System.out.println();

        System.out.println("Inorder traversal of constructed AVL tree is : ");
        tree.inOrder(tree.root);
        System.out.println();

        System.out.println("Postorder traversal of constructed AVL tree is : ");
        tree.postOrder(tree.root);
        System.out.println();

    }

    public static interface Entry<K extends Comparable<K>, V>{
        public K key();
        public V value();
    }

}