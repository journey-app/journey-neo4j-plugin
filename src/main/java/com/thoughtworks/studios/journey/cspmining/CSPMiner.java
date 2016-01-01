package com.thoughtworks.studios.journey.cspmining;

import com.thoughtworks.studios.journey.utils.LongSetUtils;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.*;

public class CSPMiner {

    private final float minSupport;
    private final long absoluteSupportBase;
    private final long relativeSupportBase;
    private SuffixTree tree;

    public CSPMiner(SuffixTree tree, float threshold, boolean absoluteSupport) {
        this.tree = tree;
        absoluteSupportBase = tree.getSupportBase();
        relativeSupportBase = tree.getJourneyCount();
        this.minSupport = absoluteSupport ? absoluteSupportBase * threshold : relativeSupportBase * threshold;
    }

    public Set<Pattern> suffixPatterns() {
        final Set<Pattern> patterns = new HashSet<>();
        Map<TreeNode, LongSet> rollingUpNodes = new HashMap<>();

        for (TreeNode node : tree.depthFirstNodes(false)) {
            LongSet supports = node.getJourneyIdsSet();
            LongSet rolling = rollingUpNodes.remove(node);
            if (rolling != null) {
                supports = LongSetUtils.union(supports, rolling);
            }

            if (supports.size() >= minSupport) {
                patterns.add(createPattern(tree.pathTo(node), supports));
            } else {
                TreeNode parent = node.getParent();
                if (!tree.getRoot().equals(parent)) {
                    LongSet existing = rollingUpNodes.get(parent);
                    rollingUpNodes.put(parent, LongSetUtils.union(existing, supports));
                }
            }
        }

        return patterns;

    }

    private Pattern createPattern(List<String> actions, LongSet journeys) {
        Pattern pattern = new Pattern(actions, journeys);
        pattern.setTotalJourneys(absoluteSupportBase);
        pattern.setTotalLeadActionInvolvedJourneys(relativeSupportBase);
        return pattern;
    }
}
