package com.nilesh.cym.temp;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TreeNode {
    static TreeNode rootNode;
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("enter root node: ");
        int root = sc.nextInt();
        TreeNode node = new TreeNode(root);
        TreeNode.rootNode = node;
        node.insert(node, sc);

        List<Integer> res = new ArrayList<>();
        node.inorderTraversal(TreeNode.rootNode, res);
        System.out.println(res);
        res.clear();

        node.preorderTraversal(TreeNode.rootNode, res);
        System.out.println(res);
        res.clear();

        node.postorderTraversal(TreeNode.rootNode, res);
        System.out.println(res);
    }
    int val;
    TreeNode left;
    TreeNode right;

    public TreeNode(int val){
        this.val = val;
    }

    public void insert(TreeNode root, Scanner sc){
        System.out.println("want to insert left child for "+root.val+" enter (true/false): ");
        boolean ifYes = sc.nextBoolean();
        TreeNode leftChild;
        if(ifYes){
            System.out.println("insert left child for "+root.val+": ");
            int left = sc.nextInt();
            leftChild = new TreeNode(left);
            root.left = leftChild;
            insert(leftChild, sc);
        }
        System.out.println("want to insert right child for "+root.val+" enter(true/false): ");
        ifYes = sc.nextBoolean();
        if(ifYes){
            System.out.println("insert right child for "+root.val+": ");
            int right = sc.nextInt();
            TreeNode rightChild = new TreeNode(right);
            root.right = rightChild;
            insert(rightChild, sc);
        }
    }

    public void inorderTraversal(TreeNode root, List<Integer> res){
        if(root==null) return;
        inorderTraversal(root.left, res);
        res.add(root.val);
        inorderTraversal(root.right, res);
    }

    public void preorderTraversal(TreeNode root, List<Integer> res){
        if(root==null) return;
        res.add(root.val);
        inorderTraversal(root.left, res);
        inorderTraversal(root.right, res);
    }

    public void postorderTraversal(TreeNode root, List<Integer> res){
        if(root==null) return;
        inorderTraversal(root.left, res);
        inorderTraversal(root.right, res);
        res.add(root.val);

    }
}
