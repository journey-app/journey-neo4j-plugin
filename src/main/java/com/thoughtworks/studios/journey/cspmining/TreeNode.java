package com.thoughtworks.studios.journey.cspmining;

import com.thoughtworks.studios.journey.models.RelTypes;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import org.neo4j.graphdb.*;
import org.neo4j.graphdb.traversal.*;
import org.neo4j.function.Function;

import java.util.LinkedList;
import java.util.List;

import static com.thoughtworks.studios.journey.utils.GraphDbUtils.getEndNodes;
import static com.thoughtworks.studios.journey.utils.GraphDbUtils.getSingleStartNode;
import static org.neo4j.helpers.collection.Iterables.*;

public class TreeNode {
    public static final String PROP_NAMES = "names";
    public static final String PROP_JOURNEY_IDS = "journeyIds";
    private static final String PROP_JOURNEY_COUNT = "journeyCount";
    private static final String PROP_SUPPORT_BASE_COUNT = "supportBaseCount";
    public static final String PROP_TREE_CATEGORY = "treeCategory";
    private Node neoNode;

    public static Function<Node, TreeNode> factoryFun() {
        return new Function<Node, TreeNode>() {
            @Override
            public TreeNode apply(Node node) {
                return new TreeNode(node);
            }
        };
    }

    public TreeNode(Node neoNode) {
        this.neoNode = neoNode;
    }

    public LongSet getJourneyIdsSet() {
        if (neoNode.hasProperty(PROP_JOURNEY_IDS)) {
            return new LongOpenHashSet((long[]) neoNode.getProperty(PROP_JOURNEY_IDS));
        } else {
            return new LongOpenHashSet();
        }
    }


    public String[] getNames() {
        return (String[]) neoNode.getProperty(PROP_NAMES);
    }

    public Iterable<TreeNode> depthFirstNodes(boolean orderedExpanding) {
        TraversalDescription tr = traversalDescription()
                .order(BranchOrderingPolicies.POSTORDER_DEPTH_FIRST)
                .evaluator(Evaluators.excludeStartPosition())
                .uniqueness(Uniqueness.NODE_PATH);

        if (orderedExpanding) {
            tr = tr.expand(new PathExpander() {
                @Override
                public Iterable<Relationship> expand(Path path, BranchState state) {
                    Iterable<Relationship> relationships = path.endNode().getRelationships(Direction.OUTGOING, RelTypes.SUFFIX_CHILD);
                    return sort(relationships, new Function<Relationship, Comparable>() {
                        @Override
                        public Comparable apply(Relationship relationship) {
                            return relationship.getId();
                        }
                    });
                }

                @Override
                public PathExpander reverse() {
                    throw new RuntimeException("not implemented");
                }
            });
        } else {
            tr = tr.expand(PathExpanders.forTypeAndDirection(RelTypes.SUFFIX_CHILD, Direction.OUTGOING));
        }

        return map(factoryFun(), tr.traverse(neoNode).nodes());
    }

    private TraversalDescription traversalDescription() {
        return neoNode.getGraphDatabase().traversalDescription();
    }

    public void setNames(String[] names) {
        this.neoNode.setProperty(PROP_NAMES, names);
    }

    public void addChild(TreeNode child) {
        Relationship oldRelationship = child.neoNode.getSingleRelationship(RelTypes.SUFFIX_CHILD, Direction.INCOMING);
        if (oldRelationship != null) {
            oldRelationship.delete();
        }
        neoNode.createRelationshipTo(child.neoNode, RelTypes.SUFFIX_CHILD);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNode treeNode = (TreeNode) o;

        return neoNode.equals(treeNode.neoNode);

    }

    @Override
    public int hashCode() {
        return neoNode.hashCode();
    }

    public void addJourneyId(long journeyId) {
        LongSet journeyIds = getJourneyIdsSet();
        journeyIds.add(journeyId);
        neoNode.setProperty(PROP_JOURNEY_COUNT, (long) journeyIds.size());
        neoNode.setProperty(PROP_JOURNEY_IDS, journeyIds.toArray(new Long[journeyIds.size()]));
    }

    public void delete() {
        for (TreeNode child : children()) {
            child.delete();
        }

        Iterable<Relationship> relationships = neoNode.getRelationships(Direction.INCOMING);
        for (Relationship relationship : relationships) {
            relationship.delete();
        }
        neoNode.delete();
    }

    Iterable<TreeNode> children() {
        return map(factoryFun(), getEndNodes(neoNode, RelTypes.SUFFIX_CHILD));
    }

    public long getJourneyCount() {
        return (long) neoNode.getProperty(PROP_JOURNEY_COUNT);
    }

    public void setJourneyCount(long journeyCount) {
        neoNode.setProperty(PROP_JOURNEY_COUNT, journeyCount);
    }


    boolean hasChildren() {
        return neoNode.getDegree(RelTypes.SUFFIX_CHILD, Direction.OUTGOING) > 0;
    }

    public TreeNode getParent() {
        Node parentNeoNode = getSingleStartNode(neoNode, RelTypes.SUFFIX_CHILD);
        return parentNeoNode == null ? null : new TreeNode(parentNeoNode);
    }

    public List<TreeNode> pathToRoot() {
        List<TreeNode> nodes = new LinkedList<>();
        TreeNode cursor = this;

        while (!cursor.isRoot()) {
            nodes.add(cursor);
            cursor = cursor.getParent();
        }
        return nodes;
    }

    public boolean isRoot() {
        return neoNode.getDegree(RelTypes.SUFFIX_CHILD, Direction.INCOMING) == 0;
    }

    public void setSupportBaseCount(long supportBaseCount) {
        neoNode.setProperty(PROP_SUPPORT_BASE_COUNT, supportBaseCount);
    }

    public long getSupportBaseCount() {
        return (long) neoNode.getProperty(PROP_SUPPORT_BASE_COUNT);
    }

    public void setTreeCategory(TreeCategory treeCategory) {
        neoNode.setProperty(PROP_TREE_CATEGORY, treeCategory.name());
    }

    public TreeCategory getTreeCategory() {
        return TreeCategory.valueOf((String) neoNode.getProperty(PROP_TREE_CATEGORY));
    }

}
