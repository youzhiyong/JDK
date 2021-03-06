/*
 * Copyright (c) 2012, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */


package org.graalvm.compiler.replacements.nodes;

import static org.graalvm.compiler.nodeinfo.NodeCycles.CYCLES_0;
import static org.graalvm.compiler.nodeinfo.NodeSize.SIZE_0;

import java.util.ArrayList;

import org.graalvm.compiler.api.replacements.Snippet.VarargsParameter;
import org.graalvm.compiler.core.common.type.StampFactory;
import org.graalvm.compiler.graph.Node;
import org.graalvm.compiler.graph.NodeClass;
import org.graalvm.compiler.nodeinfo.NodeInfo;
import org.graalvm.compiler.nodes.FixedWithNextNode;
import org.graalvm.compiler.nodes.LoopBeginNode;

/**
 * Placeholder node to denote to snippet preparation that the following loop must be completely
 * unrolled.
 *
 * @see VarargsParameter
 */
@NodeInfo(cycles = CYCLES_0, size = SIZE_0)
public final class ExplodeLoopNode extends FixedWithNextNode {
    public static final NodeClass<ExplodeLoopNode> TYPE = NodeClass.create(ExplodeLoopNode.class);

    public ExplodeLoopNode() {
        super(TYPE, StampFactory.forVoid());
    }

    public LoopBeginNode findLoopBegin() {
        Node currentNext = next();
        ArrayList<Node> succs = new ArrayList<>();
        while (!(currentNext instanceof LoopBeginNode)) {
            assert currentNext != null : "cannot find loop after " + this;
            for (Node n : currentNext.cfgSuccessors()) {
                succs.add(n);
            }
            if (succs.size() == 1 && succs.get(0) != currentNext) {
                currentNext = succs.get(0);
            } else {
                return null;
            }
        }
        return (LoopBeginNode) currentNext;
    }

    /**
     * A call to this method must be placed immediately prior to the loop that is to be exploded.
     */
    @NodeIntrinsic
    public static native void explodeLoop();
}
