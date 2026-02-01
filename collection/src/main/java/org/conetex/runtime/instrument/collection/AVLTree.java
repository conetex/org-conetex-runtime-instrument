package org.conetex.runtime.instrument.collection;

public class AVLTree<T extends Comparable<T>> {

    static abstract class AbstractNode<K extends Comparable<K>, V> {

        abstract K key();

        abstract V value();

        abstract  int height();

        abstract AbstractNode<K, V> left();

        abstract AbstractNode<K, V> right();

        abstract SetNodeFactory<K, V> insert(SetLeafNode<K, V> nodeToInsert);

        abstract AbstractNode<K, V> find(K keyToFind);

        abstract SetNodeFactory<K, V> delete(K keyToDelete);

        abstract SetNodeFactory<K, V> createNew(AbstractNode<K, V> left, AbstractNode<K, V> right);

        abstract SetNode<K, V> createFinal(AbstractNode<K, V> left, AbstractNode<K, V> right);

    }

    private static abstract class AbstractBalancedNode<K extends Comparable<K>, V> extends AbstractNode<K,V> {

        protected int height;

        @Override
        protected final int height() {
            return this.height;
        }

        void updateHeight() {
            if(this.left() == null){
                if(this.right() == null){
                    this.height = 1;
                }
                else{
                    this.height = 1 + this.right().height();
                }
            }
            else{
                if(this.right() == null){
                    this.height = 1 + this.left().height();
                }
                else{
                    this.height = 1 + ((this.left().height() > this.right().height()) ? this.left().height() : this.right().height());
                }
            }
        }

    }

    private abstract static class SetNodeFactory<K extends Comparable<K>, V> {

        private AbstractNode<K,V> left;

        private AbstractNode<K,V> right;

        protected K key;

        protected SetNodeFactory(AbstractNode<K,V> left, K key, AbstractNode<K,V> right) {
            this.left = left;
            this.right = right;
            this.key = key;
            this.updateHeight();
        }

        protected int height;

        protected final int height() {
            return this.height;
        }

        void updateHeight() {
            if(this.left == null){
                if(this.right == null){
                    this.height = 1;
                }
                else{
                    this.height = 1 + this.right.height();
                }
            }
            else{
                if(this.right == null){
                    this.height = 1 + this.left.height();
                }
                else{
                    this.height = 1 + ((this.left.height() > this.right.height()) ? this.left.height() : this.right.height());
                }
            }
        }

        public abstract AbstractNode<K,V> createFinal();

        public abstract AbstractNode<K,V> createFinal(AbstractNode<K,V> left, AbstractNode<K,V> right);

        // LL
        private SetNodeFactory<K, V> unusedMrotateRightNew(SetNodeFactory<K, V> newRoot) {
            // Perform rotation
            // newRoot is new ==> we can change it
            newRoot.right = this.createFinal(newRoot.right, this.right);
            // Update heights
            newRoot.updateHeight();
            return newRoot;
        }

        // LL
        private SetNodeFactory<K, V> unusedMrotateRightNew(SetNode<K, V> newRoot) {
            // Perform rotation
            AbstractNode<K, V> newThis = this.createFinal(newRoot.right, this.right);
            // newRoot is not new ==> make it new
            return newRoot.createNew(newRoot.left, newThis);
        }

        // RR
        private SetNodeFactory<K, V> unusedMrotateLeftNew(SetNodeFactory<K, V> newRoot) {
            // newRoot is new ==> we can change it
            // Perform rotation
            newRoot.left = this.createFinal(this.left, newRoot.left);
            newRoot.updateHeight();
            return newRoot;
        }

        // RR
        private SetNodeFactory<K, V> unusedMrotateLeftNew(SetNode<K, V> newRoot) {
            // Perform rotation
            AbstractNode<K,V> newThis = this.createFinal(this.left, newRoot.left);
            // newRoot is not new ==> make it new
            return newRoot.createNew(newThis, newRoot.right);
        }

        // LR
        private SetNodeFactory<K, V> unusedMrotateLeftRightNew(SetNodeFactory<K, V> newLeft) {
            // newLeft is new ==> update it
            AbstractNode<K,V> newLeftRight = newLeft.right; // but remember data before update
            newLeft.right = newLeft.right.left();
            newLeft.updateHeight();
            return newLeftRight.createNew(
                    newLeft.createFinal(),
                    this.createFinal(newLeftRight.right(), this.right)
            );
        }

        // LR
        private SetNodeFactory<K, V> unusedMrotateLeftRightNew(SetNode<K, V> newLeft) {
            // newLeft is not new ==> make it new
            return newLeft.right.createNew(
                    newLeft.createFinal(newLeft.left, newLeft.right.left()),
                    this.createFinal(newLeft.right.right(), this.right)
            );
        }

        // RL
        private SetNodeFactory<K, V> unusedMrotateRightLeftNew(SetNodeFactory<K, V> newRight) {
            // newRight is new ==> update it
            AbstractNode<K,V> newRightLeft = newRight.left; // but remember data before update
            newRight.left = newRight.left.right();
            newRight.updateHeight();
            return newRightLeft.createNew(
                    this.createFinal(this.left, newRightLeft.left()),
                    newRight.createFinal()
            );
        }

        // RL
        private SetNodeFactory<K, V> unusedMrotateRightLeftNew(SetNode<K, V> newRight) {
            // newRight is not new ==> make it new
            return newRight.left.createNew(
                    this.createFinal(this.left, newRight.left.left()),
                    newRight.createFinal(newRight.left.right(), newRight.right)
            );
        }

    }

    private static class SetNodeFactoryCreated<K extends Comparable<K>,V> extends SetNodeFactory<K,V>{

        private AbstractNode<K,V> alreadyCreatedNode;

        protected SetNodeFactoryCreated(AbstractNode<K,V> alreadyCreatedNode) {
            super(null, null, null);
            this.alreadyCreatedNode = alreadyCreatedNode;
        }

        @Override
        public AbstractNode<K, V> createFinal() {
            return this.alreadyCreatedNode;
        }

        @Override
        public AbstractNode<K, V> createFinal(AbstractNode<K, V> left, AbstractNode<K, V> right) {
            return this.alreadyCreatedNode.createFinal(left, right);
        }
    }

    private static class SetNodeFactoryNull<K extends Comparable<K>,V> extends SetNodeFactory<K,V> {

        protected SetNodeFactoryNull() {
            super(null, null, null);
        }

        @Override
        public AbstractNode<K, V> createFinal() {
            return null;
        }

        @Override
        public AbstractNode<K, V> createFinal(AbstractNode<K, V> left, AbstractNode<K, V> right) {
            return null;
        }
    }

    private static class SetNodeFactoryCopy<K extends Comparable<K>> extends SetNodeFactory<K,K> {

        public SetNodeFactoryCopy(AbstractNode<K, K> left, K key, AbstractNode<K, K> right) {
            super(left, key, right);
        }

        public AbstractNode<K,K> createFinal() {
            return new SetNodeN<K>(this.key, super.left, super.right);
        }

        public AbstractNode<K,K> createFinal(AbstractNode<K,K> left, AbstractNode<K,K> right) {
            return new SetNodeN<K>(this.key, left, right);
        }

    }

/*
    private static class SetNodeFactoryX<K extends Comparable<K>>  extends AbstractBalancedNode<K,K>
    {

        private AbstractNode<K,K> left;

        private AbstractNode<K,K> right;

        protected K key;

        private SetNodeFactoryX(K key, AbstractNode<K,K> left, AbstractNode<K,K> right) {
            this.left = left;
            this.right = right;
            this.key = key;
            super.updateHeight();
        }

        @Override
        protected AbstractNode<K,K> left() {
            return this.left;
        }

        @Override
        protected AbstractNode<K,K> right() {
            return this.right;
        }

        @Override
        public K key() {
            return this.key;
        }

        @Override
        protected K value() {
            return this.key;
        }

        @Override
        public AbstractNode<K,K> find(K keyToFind) {
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


        // Insert a key into the AVL tree and return the new root of the subtree
        @Override
        protected AbstractNode<K,K> insert(SetLeafNode<K> nodeToInsert) {
            int cmp = nodeToInsert.key.compareTo(this.key);
            if (cmp < 0){
                AbstractNode<K,K> oldLeft;
                while(true) {
                    synchronized (this) {
                        if (this.left == null) {
                            return new SetNodeFactory<>(this.key, nodeToInsert, this.right);
                        }
                        oldLeft = this.left;
                    }
                    AbstractNode<K,K> newLeft = oldLeft.insert(nodeToInsert);
                    synchronized (this) {
                        if (this.left != oldLeft) {
                            // another thread changed this.left ==> retry
                            continue;
                        }
                        // no other thread changed this.left in between
                        if (newLeft instanceof AVLTree.AbstractBalancedNode<K,K> newLeftA && newLeftA.height - 1 > (this.right == null ? 0 : this.right.height())) {
                            // left heavy
                            if (newLeft.right() == null || (newLeft.left() != null && newLeft.left().height() >= newLeft.right().height())) {
                                // LL
                                //return this.rotateRightNew(newLeft, oldLeft);
                                return newLeftA.callRotateRight(this);
                            }
                            // left.right HIGHER ==> LR
                            //return this.rotateLeftRightNew(newLeft, oldLeft);
                            return newLeftA.callRotateLeftRight(this);
                        }
                        return new SetNodeFactory<>(this.key, newLeft, this.right);
                    }
                }
            }
            if (cmp > 0){
                AbstractNode<K,K> oldRight;
                while(true) {
                    synchronized (this) {
                        if (this.right == null) {
                            return new SetNodeFactory<>(this.key, this.left, nodeToInsert);
                        }
                        oldRight = this.right;
                    }
                    AbstractNode<K,K> newRight = oldRight.insert(nodeToInsert);
                    synchronized (this) {
                        if (this.right == oldRight) {
                            // no other thread changed this.right in between
                            if (newRight instanceof AVLTree.AbstractBalancedNode<K,K> newRightA && newRightA.height - 1 > (this.left == null ? 0 : this.left.height())){
                                // right heavy
                                if (newRight.left() == null || (newRight.right() != null && newRight.left().height() <= newRight.right().height())) {
                                    // RR
                                    //return this.rotateLeftNew(newRight, oldRight);
                                    return newRightA.callRotateLeft(this);
                                }
                                // right.left HIGHER ==> RL
                                //return this.rotateRightLeftNew(newRight, oldRight);
                                return newRightA.callRotateRightLeft(this);
                            }
                            return new SetNodeFactory<>(this.key, this.left, newRight);
                        }   // else: another thread changed this.left ==> retry
                    }
                }
            }
            // replace data
            synchronized (this) {
                return new SetNodeFactory<>(this.key, this.left, this.right);
            }

        }







    }
*/
    private static class SetNodeN<K extends Comparable<K>> extends SetNode<K,K> {

        private SetNodeN(K keyToInsert, AbstractNode<K,K> left, AbstractNode<K,K> right) {
            super(keyToInsert, left, right);
        }

        final SetNodeFactory<K, K> createNew(AbstractNode<K, K> left, AbstractNode<K, K> right) {
            return new SetNodeFactoryCopy<K>(left, super.key, right);
        }

        @Override
        SetNode<K, K> createFinal(AbstractNode<K, K> left, AbstractNode<K, K> right) {
            return new SetNodeN<K>(this.key(), left, right);
        }

        @Override
        protected K value() {
            return super.key;
        }

    }

    private abstract static class SetNode<K extends Comparable<K>, V> extends AbstractBalancedNode<K,V> {
        private final AbstractNode<K,V> left;
        private final AbstractNode<K,V> right;
        protected final K key;

        private SetNode(K keyToInsert, AbstractNode<K,V> left, AbstractNode<K,V> right) {
            this.left = left;
            this.right = right;
            this.key = keyToInsert;
            super.updateHeight();
        }

        @Override
        public K key() {
            return this.key;
        }

        @Override
        protected AbstractNode<K,V> left() {
            return this.left;
        }

        @Override
        protected AbstractNode<K,V> right() {
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
        SetNodeFactory<K,V> delete(K valueToRemove) {
            int cmp = valueToRemove.compareTo(this.key);

            if (cmp < 0) {
                // go left
                AbstractNode<K,V> oldLeft, oldRight;
                while(true) {
                    synchronized (this) {
                        if (this.left == null) {
                            return null;
                        }
                        oldLeft = this.left;
                        oldRight = this.right;
                    }
                    SetNodeFactory<K,V> newLeft = oldLeft.delete(valueToRemove);
                    synchronized (this) {
                        if (this.left == oldLeft) {
                            if(newLeft == null){
                                return null;
                            }
                            this.left = newLeft.createFinal();
                            if (this.right != null && this.right instanceof AVLTree.SetNode<K,V> rightA && rightA.height > ((newLeft == null) ? 0 : newLeft.height()) + 1) {
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
                            this.updateHeight();
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
                        return new SetNodeFactoryNull<>();
                    }
                    if (this.left == null) {
                        // delete this node - successor is right
                        return new SetNodeFactory<>(null, null, null){
                            @Override
                            public AbstractNode<K, V> createFinal() {
                                return SetNode.this.right;
                            }

                            @Override
                            public AbstractNode<K, V> createFinal(AbstractNode<K, V> left, AbstractNode<K, V> right) {
                                return SetNode.this.right;
                            }
                        };
                    }
                    if (this.right == null) {
                        // delete this node - successor is left
                        return new SetNodeFactoryCreated<>(this.left);
                    }
                    // delete this node - new value is smallest (left) at right
                    AbstractNode<K,V> newValueNode = this.right;
                    while (newValueNode.left() != null) {
                        newValueNode = newValueNode.left();
                    }
                    // copy data from successor to this
                    //this.takeOverKeyValue(newValueNode);
                    this.key = newValueNode.key();
                    // remove successor
                    valueToRemove = newValueNode.key();
                }
            }

            // go right
            AbstractNode<K,V> oldLeft, oldRight;
            while(true) {
                synchronized (this) {
                    if (this.right == null) {
                        return null;
                    }
                    oldLeft = this.left;
                    oldRight = this.right;
                }
                SetNodeFactory<K,V> newRight = oldRight.delete(valueToRemove);
                synchronized (this) {
                    if (this.right == oldRight) {
                        if(oldRight == null){
                            return null;
                        }
                        this.right = newRight.createFinal();
                        if ( this.left != null && this.left instanceof AVLTree.SetNode<K,V> leftA && leftA.height > ((newRight == null) ? 0 : newRight.height()) + 1) {
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
                        this.updateHeight();
                        return null;
                    }
                }
            }

        }

        @Override
        abstract SetNodeFactory<K, V> createNew(AbstractNode<K, V> left, AbstractNode<K, V> right);

        // Insert a key into the AVL tree and return the new root of the subtree
        @Override
        protected SetNodeFactory<K,V> insert(SetLeafNode<K,V> nodeToInsert) {
            int cmp = nodeToInsert.key.compareTo(this.key);
            if (cmp < 0){
                AbstractNode<K,V> oldLeft;
                while(true) {
                    synchronized (this) {
                        if (this.left == null) {
                            //return new SetNodeFactoryN<>(nodeToInsert, this.key, this.right);
                            return this.createNew(nodeToInsert, this.right);
                        }
                        oldLeft = this.left;
                    }
                    SetNodeFactory<K,V> newLeft = oldLeft.insert(nodeToInsert);
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
                        this.left = newLeft.createFinal();
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
                            return this.createNew(this.left, nodeToInsert);
                        }
                        oldRight = this.right;
                    }
                    SetNodeFactory<K,V> newRight = oldRight.insert(nodeToInsert);
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
                            this.right = newRight.createFinal();
                            return null;
                            //return new SetNodeFactoryN<>(this.left, this.key, newRight);
                        }   // else: another thread changed this.left ==> retry
                    }
                }
            }
            // replace data todo
            synchronized (this) {
                return null;//new SetNodeFactory<>(this.left, this.key, this.right);
            }

        }

        // LL
        private SetNodeFactory<K,V> rotateRightNew(SetNodeFactory<K,V> newRoot) {
            // Perform rotation
            // newRoot is new ==> we can change it
            newRoot.right = this.createFinal(newRoot.right, this.right);
            // Update heights
            newRoot.updateHeight();
            return newRoot;
        }

        // LL
        private SetNodeFactory<K,V> rotateRightNew(SetNode<K, V> newRoot) {
            // Perform rotation
            SetNode<K,V> newThis = this.createFinal(newRoot.right, this.right);
            // newRoot is not new ==> make it new
            return newRoot.createNew(newRoot.left, newThis);
       }

        // RR
        private SetNodeFactory<K,V> rotateLeftNew(SetNodeFactory<K,V> newRoot) {
            // newRoot is new ==> we can change it
            // Perform rotation
            newRoot.left = this.createFinal(this.left, newRoot.left);
            newRoot.updateHeight();
            return newRoot;
        }

        // RR
        private SetNodeFactory<K,V> rotateLeftNew(SetNode<K, V> newRoot) {
            // Perform rotation
            SetNode<K,V> newThis = this.createFinal(this.left, newRoot.left);
            // newRoot is not new ==> make it new
            return newRoot.createNew(newThis, newRoot.right);
        }

        // LR
        private SetNodeFactory<K,V> rotateLeftRightNew(SetNodeFactory<K,V> newLeft) {
            // newLeft is new ==> update it
            AbstractNode<K,V> newLeftRight = newLeft.right; // but remember data before update
            newLeft.right = newLeft.right.left();
            newLeft.updateHeight();
            return newLeftRight.createNew(
                newLeft.createFinal(),
                this.createFinal(newLeftRight.right(), this.right)
            );
        }

        // LR
        private SetNodeFactory<K,V> rotateLeftRightNew(SetNode<K, V> newLeft) {
            // newLeft is not new ==> make it new
            return newLeft.right.createNew(
                newLeft.createFinal(newLeft.left, newLeft.right.left()),
                this.createFinal(newLeft.right.right(), this.right)
            );
        }

        // RL
        private SetNodeFactory<K,V> rotateRightLeftNew(SetNodeFactory<K,V> newRight) {
            // newRight is new ==> update it
            AbstractNode<K,V> newRightLeft = newRight.left; // but remember data before update
            newRight.left = newRight.left.right();
            newRight.updateHeight();
            return newRightLeft.createNew(
                this.createFinal(this.left, newRightLeft.left()),
                newRight.createFinal()
            );
        }

        // RL
        private SetNodeFactory<K,V> rotateRightLeftNew(SetNode<K, V> newRight) {
            // newRight is not new ==> make it new
            return newRight.left.createNew(
                this.createFinal(this.left, newRight.left.left()),
                newRight.createFinal(newRight.left.right(), newRight.right)
            );
        }

    }

    public static class SetLeafNodeN<K extends Comparable<K>> extends SetLeafNode<K, K>{

        public SetLeafNodeN(K valueToInsert) {
            super(valueToInsert);
        }

        protected K value() {
            return this.key;
        }

        @Override
        SetNodeFactory<K, K> createNew(AbstractNode<K, K> left, AbstractNode<K, K> right) {
            return new SetNodeFactoryCopy<K>(left, super.key, right);
        }

        @Override
        SetNode<K, K> createFinal(AbstractNode<K, K> left, AbstractNode<K, K> right) {
            return new SetNodeN<K>(super.key, left, right);
        }
    }

    public static abstract class SetLeafNode<K extends Comparable<K>, V> extends AbstractNode<K, V> {
        protected K key;
        //protected int height;

        public SetLeafNode(K valueToInsert) {
            this.key = valueToInsert;
        }

        // Insert a key into the AVL tree and return the new root of the subtree
        SetNodeFactory<K, V> insert(SetLeafNode<K, V> nodeToInsert) {
            synchronized (this) {
                int cmp = nodeToInsert.key.compareTo(this.key);
                if (cmp < 0){
                    return this.createNew(nodeToInsert, null);
                }
                if (cmp > 0){
                    return this.createNew(null, nodeToInsert);
                }
                // replace data todo
                this.key = nodeToInsert.key;
                return null;
            }
        }

        protected abstract V value();

        AbstractNode<K, V> find(K keyToFind) {
            if (keyToFind.compareTo(this.key) == 0) {
                return this;
            }
            return null;
        }

        SetNodeFactory<K, V> delete(K keyToDelete) {
            if (keyToDelete.compareTo(this.key) == 0) {
                // delete this
                return new SetNodeFactoryNull<>();
            }
            // nothing to do because keyToDelete was not found
            return null;
        }

        AbstractNode<K, V> left() {
            return null;
        }

        AbstractNode<K, V> right() {
            return null;
        }

        protected int height() {
            return 1;
        }

        @Override
        public K key() {
            return this.key;
        }

    }

    public static class Set<T extends Comparable<T>> {

        private AbstractNode<T,T> root;

        AbstractNode<T,T> getRoot(){
            return this.root;
        }

        public void insertIntoTree(T valueToInsert) {
            if(valueToInsert == null){
                throw new NullPointerException("can not insert null");
            }

            AbstractNode<T,T> theRoot;
            synchronized(this) {
                if(this.root == null){
                    this.root = new SetLeafNodeN<>(valueToInsert);
                    return;
                }
                theRoot = this.root;
            }

            SetNodeFactory<T,T> newRoot = theRoot.insert(new SetLeafNodeN<>(valueToInsert));
            // todo check root changed
            if(newRoot != null) {
                synchronized (this) {
                    this.root = newRoot.createFinal();
                }
            }
        }

        public void deleteFromTree(T keyToDelete) {
            if(keyToDelete == null){
                throw new NullPointerException("can not delete null");
            }

            AbstractNode<T,T> theRoot;
            synchronized(this) {
                if(this.root == null){
                    return;
                }
                theRoot = this.root;
            }

            SetNodeFactory<T,T> newRoot = theRoot.delete(keyToDelete);
            // todo check root changed
            if(newRoot != null){
                synchronized(this) {
                    this.root = newRoot.createFinal();;
                }
            }
        }

        public T findInTree(T keyToFind) {
            if(keyToFind == null){
                throw new NullPointerException("can not find null");
            }

            AbstractNode<T,T> theRoot;
            synchronized(this) {
                if(this.root == null){
                    return null;
                }
                theRoot = this.root;
            }

            AbstractNode<T,T> result = theRoot.find(keyToFind);
            if(result == null){
                return null;
            }
            return result.value();
        }

        // Utility functions for traversal
        void preOrder(AbstractNode<T,T> node) {
            if (node != null) {
                System.out.print(node.key() + " ");
                preOrder(node.left());
                preOrder(node.right());
            }
        }

        void inOrder(AbstractNode<T,T> node) {
            if (node != null) {
                inOrder(node.left());
                System.out.print(node.key() + " ");
                inOrder(node.right());
            }
        }

        void reverseOrder(AbstractNode<T,T> node) {
            if (node != null) {
                reverseOrder(node.right());
                System.out.print(node.key() + " ");
                reverseOrder(node.left());
            }
        }

        void postOrder(AbstractNode<T,T> node) {
            if (node != null) {
                postOrder(node.left());
                postOrder(node.right());
                System.out.print(node.key() + " ");
            }
        }

    }

    private static class Old {

        public static interface Entry<K extends Comparable<K>, V>{
            public K key();
            public V value();
        }

        public static class SetO<T extends Comparable<T>> {

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
                void takeOverKeyValue(Node<K, K> other) {
                    super.key = other.key;
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

                @Override
                void takeOverKeyValue(Node<K, V> other) {
                    super.key = other.key;
                    this.value = other.value();
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

            abstract void takeOverKeyValue(Node<K, V> other);

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
                        this.takeOverKeyValue(newValueNode);
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

}