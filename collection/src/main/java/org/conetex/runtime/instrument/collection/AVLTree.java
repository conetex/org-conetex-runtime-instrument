package org.conetex.runtime.instrument.collection;

public class AVLTree<T extends Comparable<T>> {

    private Node<T> root;

    public void insertIntoTree(T valueToInsert) {
        if(valueToInsert == null){
            throw new NullPointerException("can not insert null");
        }
        if(this.root == null){
            this.root = new Node<T>(valueToInsert);
        }
        else{
            this.root = this.root.insert(valueToInsert);
        }
    }

    public void deleteFromTree(T valueToDelete) {
        if (valueToDelete == null) {
            return;
        }
        if (this.root != null) {
            this.root = this.root.delete(valueToDelete);
        }
    }

    public Entry<T> findInTree(T valueToFind) {
        if(valueToFind == null){
            throw new NullPointerException("can not find null");
        }
        if(this.root == null){
            return null;
        }
        else{
            return this.root.find(valueToFind);
        }
    }

    public static interface Entry<I extends Comparable<I>>{
        public I data();
    }

    private static class Node<D extends Comparable<D>> implements Entry<D> {

        private D data;
        private int height;

        private final Object monitorLeft = new Object();
        private final Object monitorRight = new Object();

        private Node<D> left;
        private Node<D> right;

        private Node(D newData) {
            setData(newData);
            this.height = 1;
        }

        // Insert a key into the AVL tree and return the new root of the subtree
        private Node<D> insert(D valueToInsert) {
            int cmp = valueToInsert.compareTo(this.data);
            if (cmp < 0){
                if(this.left == null){
                    return this.setLeftUpdateHeight( new Node<D>(valueToInsert) );
                }
                else{
                    Node<D> newLeft = this.left.insert(valueToInsert);
                    if( newLeft.height - 1 > (this.right == null ? 0 : this.right.height) ){
                        // left heavy
                        if ( newLeft.right == null || (newLeft.left != null && newLeft.left.height >= newLeft.right.height) ) {
                            // LL
                            //return newLeft.rootRotateRight(this);
                            return this.rotateRight(newLeft);
                        }
                        // left.right HIGHER ==> LR
                        //return newLeft.right.rootRotateLeft(newLeft).rootRotateRight( this );
                        return this.rotateLeftRight(newLeft);
                    }
                    return this.setLeftUpdateHeight( newLeft );
                }
            }
            if (cmp > 0){
                if(this.right == null){
                    return this.setRightUpdateHeight( new Node<D>(valueToInsert) );
                }
                else{
                    Node<D> newRight = this.right.insert(valueToInsert);
                    if( newRight.height - 1 > (this.left == null ? 0 : this.left.height) ){
                        // right heavy
                        if( newRight.left == null || (newRight.right != null && newRight.left.height <= newRight.right.height) ){
                            // RR
                            //return newRight.rootRotateLeft(this);
                            return this.rotateLeft(newRight);
                        }
                        // right.left HIGHER ==> RL
                        //return newRight.left.rootRotateRight(newRight).rootRotateLeft( this );
                        return this.rotateRightLeft(newRight);
                    }
                    return this.setRightUpdateHeight( newRight );
                }
            }
            // replace data
            this.setData(valueToInsert);
            return this;
        }


        private Node<D> updateHeight() {
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
            return this;
        }


        // LL
        private synchronized Node<D> rotateRight(Node<D> newRoot){
            return newRoot.rootRotateRightUpdateHeight(this);
        }

        private synchronized Node<D> rootRotateRightUpdateHeight(Node<D> newRight){
            return this.rootRotateRight(newRight).updateHeight();
        }

        private Node<D> rootRotateRight(Node<D> newRight) {
            //synchronized (this.monitorRight) {
                Node<D> newRightLeft = this.right;

                // Perform rotation
                this.right = newRight;
                newRight.left = newRightLeft;

                // Update heights
                newRight.updateHeight();

                // Return newRight.left
                return this;
            //}
        }


        // RR
        private synchronized Node<D> rotateLeft(Node<D> newRoot){
            return newRoot.rootRotateLeftUpdateHeight(this);
        }

        private synchronized Node<D> rootRotateLeftUpdateHeight(Node<D> newLeft) {
            return this.rootRotateLeft(newLeft).updateHeight();
        }

        private Node<D> rootRotateLeft(Node<D> newLeft) {
            //synchronized (this.monitorLeft) {
                Node<D> newLeftRight = this.left;

                // Perform rotation
                this.left = newLeft;
                newLeft.right = newLeftRight;

                // Update heights
                newLeft.updateHeight();

                // Return newLeft.right
                return this;
            //}
        }


        // LR
        private synchronized Node<D> rotateLeftRight(Node<D> newLeft) {
            return newLeft.rightRotateLeftRight(this);
        }

        private synchronized Node<D> rightRotateLeftRight(Node<D> newRight) {
            return this.right.rootRotateLeftRight(this, newRight);
        }

        private synchronized Node<D> rootRotateLeftRight(Node<D> newLeft, Node<D> newRight) {
            return this.rootRotateLeft(newLeft).rootRotateRight(newRight).updateHeight();
        }


        // RL
        private synchronized Node<D> rotateRightLeft(Node<D> newRight) {
            return newRight.leftRotateRightLeft(this);
        }

        private synchronized Node<D> leftRotateRightLeft(Node<D> newLeft) {
            return this.left.rootRotateRightLeft(this, newLeft);
        }

        private synchronized Node<D> rootRotateRightLeft(Node<D> newRight, Node<D> newLeft) {
            return this.rootRotateRight(newRight).rootRotateLeft( newLeft ).updateHeight();
        }


        // rebalancing not needed
        private synchronized Node<D> setRightUpdateHeight(Node<D> newRight) {
            //synchronized (this.monitorRight){
                this.right = newRight;
                this.updateHeight();
                return this;
            //}
        }

        private synchronized Node<D> setLeftUpdateHeight(Node<D> newLeft) {
            //synchronized (this.monitorLeft){
                this.left = newLeft;
                this.updateHeight();
                return this;
            //}
        }





        private void setData(D newData) {
            this.data = newData;
        }



        private Node<D> find(D valueToFind) {
            int cmp = valueToFind.compareTo(this.data);
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

        private Node<D> delete(D valueToRemove) {
            int cmp = valueToRemove.compareTo(this.data);

            if (cmp < 0) {
                // go left
                if (this.left != null) {
                    this.left = this.left.delete(valueToRemove);
                }
            } else if (cmp > 0) {
                // go right
                if (this.right != null) {
                    this.right = this.right.delete(valueToRemove);
                }
            } else {
                // found node
                if (this.left == null && this.right == null) {
                    // delete this node - successor not needed
                    return null;
                } else if (this.left == null) {
                    // delete this node - successor is right
                    return this.right;
                } else if (this.right == null) {
                    // delete this node - successor is left
                    return this.left;
                } else {
                    // delete this node - new value is smallest (left) at right
                    Node<D> newValueNode = this.right;
                    while (newValueNode.left != null) {
                        newValueNode = newValueNode.left;
                    }
                    // copy data from successor to this
                    this.data = newValueNode.data;
                    // remove successor
                    this.right = this.right.delete(newValueNode.data);
                }
            }

            // Balance
            int leftHeight = (this.left == null) ? 0 : this.left.height;
            int rightHeight = (this.right == null) ? 0 : this.right.height;

            if (leftHeight > rightHeight + 1) {
                // left heavy
                Node<D> newLeft = this.left;
                if ( newLeft.right == null || (newLeft.left != null && newLeft.left.height >= newLeft.right.height) ) {
                    // LL
                    return newLeft.rootRotateRightUpdateHeight(this);
                } else {
                    // LR
                    return newLeft.right.rootRotateLeftUpdateHeight(newLeft).rootRotateRightUpdateHeight(this);
                }
            }
            if (rightHeight > leftHeight + 1) {
                // right heavy
                Node<D> newRight = this.right;
                if ( newRight.left == null || (newRight.right != null && newRight.left.height <= newRight.right.height) ) {
                    // RR
                    return newRight.rootRotateLeftUpdateHeight(this);
                } else {
                    // RL
                    return newRight.left.rootRotateRightUpdateHeight(newRight).rootRotateLeftUpdateHeight(this);
                }
            }
            this.updateHeight();
            return this;
        }




        @Override
        public D data() {
            return this.data;
        }
    }

    // Utility functions for traversal
    void preOrder(Node<?> node) {
        if (node != null) {
            System.out.print(node.data + " ");
            preOrder(node.left);
            preOrder(node.right);
        }
    }

    void inOrder(Node<?> node) {
        if (node != null) {
            inOrder(node.left);
            System.out.print(node.data + " ");
            inOrder(node.right);
        }
    }

    void postOrder(Node<?> node) {
        if (node != null) {
            postOrder(node.left);
            postOrder(node.right);
            System.out.print(node.data + " ");
        }
    }

    public static void main(String[] args) {
        AVLTree<Integer> tree = new AVLTree<>();

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