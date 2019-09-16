/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.heuristic.selector.value.decorator;

import java.util.Iterator;

import org.optaplanner.core.impl.domain.variable.descriptor.GenuineVariableDescriptor;
import org.optaplanner.core.impl.heuristic.selector.common.iterator.UpcomingSelectionIterator;
import org.optaplanner.core.impl.heuristic.selector.value.AbstractValueSelector;
import org.optaplanner.core.impl.heuristic.selector.value.ValueSelector;

/**
 * Filters out every value that is a planning entity for which the planning variable
 * (on which this {@link ValueSelector} applies to) is uninitialized.
 * <p/>
 * Mainly used for chained planning variables.
 */
public class InitializedValueSelector extends AbstractValueSelector {

    protected final GenuineVariableDescriptor variableDescriptor;
    protected final ValueSelector childValueSelector;
    protected final boolean bailOutEnabled;

    public InitializedValueSelector(ValueSelector childValueSelector) {
        this.variableDescriptor = childValueSelector.getVariableDescriptor();
        this.childValueSelector = childValueSelector;
        bailOutEnabled = childValueSelector.isNeverEnding();
        solverPhaseLifecycleSupport.addEventListener(childValueSelector);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public GenuineVariableDescriptor getVariableDescriptor() {
        return childValueSelector.getVariableDescriptor();
    }

    public boolean isCountable() {
        return childValueSelector.isCountable();
    }

    public boolean isNeverEnding() {
        return childValueSelector.isNeverEnding();
    }

    public long getSize(Object entity) {
        // TODO use cached results
        return childValueSelector.getSize(entity);
    }

    public Iterator<Object> iterator(Object entity) {
        return new JustInTimeInitializedValueIterator(entity, childValueSelector.iterator(entity));
    }

    private class JustInTimeInitializedValueIterator extends UpcomingSelectionIterator<Object> {

        private final Object entity;
        private final Iterator<Object> childValueIterator;

        public JustInTimeInitializedValueIterator(Object entity, Iterator<Object> childValueIterator) {
            this.entity = entity;
            this.childValueIterator = childValueIterator;
        }

        @Override
        protected Object createUpcomingSelection() {
            Object next;
            long attemptsBeforeBailOut = bailOutEnabled ? determineBailOutSize(entity) : 0L;
            do {
                if (!childValueIterator.hasNext()) {
                    return noUpcomingSelection();
                }
                if (bailOutEnabled) {
                    // if childValueIterator is neverEnding and nothing is accepted, bail out of the infinite loop
                    if (attemptsBeforeBailOut <= 0L) {
                        logger.warn("Bailing out of neverEnding selector ({}) to avoid infinite loop.",
                                InitializedValueSelector.this);
                        return noUpcomingSelection();
                    }
                    attemptsBeforeBailOut--;
                }
                next = childValueIterator.next();
            } while (!accept(next));
            return next;
        }

    }

    protected long determineBailOutSize(Object entity) {
        return childValueSelector.getSize(entity) * 10L;
    }

    private boolean accept(Object value) {
        return value == null
                || !variableDescriptor.getEntityDescriptor().getEntityClass().isAssignableFrom(value.getClass())
                || variableDescriptor.isInitialized(value);
    }

    @Override
    public String toString() {
        return "Initialized(" + childValueSelector + ")";
    }

}
