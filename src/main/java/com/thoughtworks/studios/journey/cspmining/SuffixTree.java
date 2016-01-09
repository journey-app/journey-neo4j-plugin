/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provides out-of-box action path analysis features on top of the graph database.
 *
 * Copyright 2015 ThoughtWorks, Inc. and Pengchao Wang
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.thoughtworks.studios.journey.cspmining;

import com.thoughtworks.studios.journey.models.Application;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.function.Function;
import org.neo4j.helpers.Predicate;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static com.thoughtworks.studios.journey.utils.ArrayUtils.commonPrefix;
import static org.apache.commons.lang.ArrayUtils.subarray;
import static org.neo4j.helpers.collection.Iterables.*;

public class SuffixTree {
    public static final String TREE_NODE_LABEL = "SuffixTreeNode";
    private static final String TREE_ROOT_LABEL = "SuffixTreeRoot";
    private Application app;
    private TreeNode root;

    public static Iterable<SuffixTree> findByCategory(final Application app, final TreeCategory category) {
        Iterable<Node> rootNodes = app.getAllNodesWithLabel(TREE_ROOT_LABEL);

        Iterable<SuffixTree> allTrees = map(new Function<Node, SuffixTree>() {
            @Override
            public SuffixTree apply(Node rootNode) {
                return new SuffixTree(app, rootNode);
            }
        }, rootNodes);

        return filter(new Predicate<SuffixTree>() {
            @Override
            public boolean accept(SuffixTree tree) {
                return tree.getCategory().equals(category);
            }
        }, allTrees);
    }

    private TreeCategory getCategory() {
        return root.getTreeCategory();
    }

    public SuffixTree(Application app, Node rootNode) {
        this.app = app;
        this.root = new TreeNode(rootNode);
    }

    public SuffixTree(Application app, String rootAction, long supportBaseCount) {
        this.app = app;
        this.root = new TreeNode(this.app.graphDB().createNode(getLabel()));
        this.root.setNames(new String[]{rootAction});
        this.root.setSupportBaseCount(supportBaseCount);
    }

    public void setJourneysCount(long val) {
        this.root.setJourneyCount(val);
    }

    private Label getLabel() {
        return app.nameSpacedLabel(TREE_ROOT_LABEL);
    }

    public String getTreeName() {
        return this.root.getNames()[0];
    }


    public Iterable<TreeNode> depthFirstNodes(boolean orderedExpanding) {
        return root.depthFirstNodes(orderedExpanding);
    }

    public void addSuffix(List<String> suffix, long journeyId) {
        addSequenceWithNode(suffix.toArray(new String[suffix.size()]), root, journeyId);
    }

    private void addSequenceWithNode(String[] prefixNames, TreeNode currentNode, long journeyId) {
        if (prefixNames.length == 0) {
            return;
        }

        Iterable<TreeNode> children = currentNode.children();
        for (TreeNode child : children) {
            String[] childSequence = child.getNames();
            String[] sharedStartNames = commonPrefix(childSequence, prefixNames);

            if (sharedStartNames.length != 0) {
                if (sharedStartNames.length == childSequence.length) {
                    if (sharedStartNames.length == prefixNames.length) {
                        child.addJourneyId(journeyId);
                    } else {
                        addSequenceWithNode(trimSequence(prefixNames, sharedStartNames.length), child, journeyId);
                    }
                } else {
                    TreeNode commonNode = createChildNode(currentNode, sharedStartNames);
                    child.setNames(trimSequence(childSequence, sharedStartNames.length));
                    commonNode.addChild(child);
                    if (sharedStartNames.length == prefixNames.length) {
                        commonNode.addJourneyId(journeyId);
                    }
                    addSequenceWithNode(trimSequence(prefixNames, sharedStartNames.length), commonNode, journeyId);
                }
                return;
            }
        }

        TreeNode childNode = createChildNode(currentNode, prefixNames);
        childNode.addJourneyId(journeyId);
    }

    private String[] trimSequence(String[] originalSeq, int startIndex) {
        return (String[]) subarray(originalSeq, startIndex, originalSeq.length);
    }

    private TreeNode createChildNode(TreeNode parent, String[] names) {
        TreeNode child = new TreeNode(app.graphDB().createNode(app.nameSpacedLabel(TREE_NODE_LABEL)));
        child.setNames(names);
        parent.addChild(child);
        return child;
    }

    public void destroy() {
        root.delete();
    }

    public long getJourneyCount() {
        return root.getJourneyCount();
    }

    public long getSupportBase() {
        return root.getSupportBaseCount();
    }

    public void setCategory(TreeCategory treeCategory) {
        root.setTreeCategory(treeCategory);
    }

    public List<String> pathTo(TreeNode node) {
        List<String> path = new LinkedList<>();
        path.add(root.getNames()[0]);

        List<TreeNode> suffix = node.pathToRoot();

        for (TreeNode treeNode : reverse(suffix)) {
            Collections.addAll(path, treeNode.getNames());
        }
        return path;
    }

    public TreeNode getRoot() {
        return root;
    }
}
