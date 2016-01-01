/**
 * This file is part of journey-neo4j-plugin. journey-neo4j-plugin is a neo4j server extension that provids out-of-box action path analysis features on top of the graph database.
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
