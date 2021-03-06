/*
 * Copyright (c) 2009, 2016, Oracle and/or its affiliates. All rights reserved.
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
package org.graalvm.compiler.java;

import org.graalvm.compiler.core.common.spi.ConstantFieldProvider;
import org.graalvm.compiler.nodes.StructuredGraph;
import org.graalvm.compiler.nodes.graphbuilderconf.GraphBuilderConfiguration;
import org.graalvm.compiler.nodes.graphbuilderconf.IntrinsicContext;
import org.graalvm.compiler.nodes.spi.StampProvider;
import org.graalvm.compiler.phases.BasePhase;
import org.graalvm.compiler.phases.OptimisticOptimizations;
import org.graalvm.compiler.phases.tiers.HighTierContext;

import jdk.vm.ci.meta.ConstantReflectionProvider;
import jdk.vm.ci.meta.MetaAccessProvider;
import jdk.vm.ci.meta.ResolvedJavaMethod;

/**
 * Parses the bytecodes of a method and builds the IR graph.
 */
public class GraphBuilderPhase extends BasePhase<HighTierContext> {

    private final GraphBuilderConfiguration graphBuilderConfig;

    public GraphBuilderPhase(GraphBuilderConfiguration config) {
        this.graphBuilderConfig = config;
    }

    @Override
    public boolean checkContract() {
        return false;
    }

    @Override
    protected void run(StructuredGraph graph, HighTierContext context) {
        new Instance(context.getMetaAccess(), context.getStampProvider(), context.getConstantReflection(), context.getConstantFieldProvider(), graphBuilderConfig, context.getOptimisticOptimizations(),
                        null).run(graph);
    }

    public GraphBuilderConfiguration getGraphBuilderConfig() {
        return graphBuilderConfig;
    }

    // Fully qualified name is a workaround for JDK-8056066
    public static class Instance extends org.graalvm.compiler.phases.Phase {

        protected final MetaAccessProvider metaAccess;
        protected final StampProvider stampProvider;
        protected final ConstantReflectionProvider constantReflection;
        protected final ConstantFieldProvider constantFieldProvider;
        protected final GraphBuilderConfiguration graphBuilderConfig;
        protected final OptimisticOptimizations optimisticOpts;
        private final IntrinsicContext initialIntrinsicContext;

        public Instance(MetaAccessProvider metaAccess, StampProvider stampProvider, ConstantReflectionProvider constantReflection, ConstantFieldProvider constantFieldProvider,
                        GraphBuilderConfiguration graphBuilderConfig, OptimisticOptimizations optimisticOpts, IntrinsicContext initialIntrinsicContext) {
            this.graphBuilderConfig = graphBuilderConfig;
            this.optimisticOpts = optimisticOpts;
            this.metaAccess = metaAccess;
            this.stampProvider = stampProvider;
            this.constantReflection = constantReflection;
            this.constantFieldProvider = constantFieldProvider;
            this.initialIntrinsicContext = initialIntrinsicContext;
        }

        @Override
        public boolean checkContract() {
            return false;
        }

        @Override
        protected void run(StructuredGraph graph) {
            createBytecodeParser(graph, null, graph.method(), graph.getEntryBCI(), initialIntrinsicContext).buildRootMethod();
        }

        /* Hook for subclasses of Instance to provide a subclass of BytecodeParser. */
        protected BytecodeParser createBytecodeParser(StructuredGraph graph, BytecodeParser parent, ResolvedJavaMethod method, int entryBCI, IntrinsicContext intrinsicContext) {
            return new BytecodeParser(this, graph, parent, method, entryBCI, intrinsicContext);
        }
    }
}
