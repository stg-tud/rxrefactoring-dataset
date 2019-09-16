/*
 * Copyright 2011 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.impl.domain.variable.descriptor;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.valuerange.CountableValueRange;
import org.optaplanner.core.api.domain.variable.PlanningVariable;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.common.PropertyAccessor;
import org.optaplanner.core.impl.domain.common.ReflectionPropertyAccessor;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;
import org.optaplanner.core.impl.domain.valuerange.descriptor.CompositeValueRangeDescriptor;
import org.optaplanner.core.impl.domain.valuerange.descriptor.FromEntityPropertyValueRangeDescriptor;
import org.optaplanner.core.impl.domain.valuerange.descriptor.FromSolutionPropertyValueRangeDescriptor;
import org.optaplanner.core.impl.domain.valuerange.descriptor.ValueRangeDescriptor;
import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.ComparatorSelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionFilter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterOrder;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.SelectionSorterWeightFactory;
import org.optaplanner.core.impl.heuristic.selector.common.decorator.WeightFactorySelectionSorter;
import org.optaplanner.core.impl.heuristic.selector.entity.decorator.NullValueReinitializeVariableEntityFilter;
import org.optaplanner.core.impl.solution.Solution;

public class GenuineVariableDescriptor {

    private final EntityDescriptor entityDescriptor;

    private final PropertyAccessor variablePropertyAccessor;
    private boolean chained;

    private ValueRangeDescriptor valueRangeDescriptor;
    private boolean nullable;
    private SelectionFilter reinitializeVariableEntityFilter;
    private SelectionSorter increasingStrengthSorter;

    private List<ShadowVariableDescriptor> shadowVariableDescriptorList = new ArrayList<ShadowVariableDescriptor>(4);
    private List<VariableListener> nonMappedByVariableListeners;

    public GenuineVariableDescriptor(EntityDescriptor entityDescriptor,
            PropertyDescriptor propertyDescriptor) {
        this.entityDescriptor = entityDescriptor;
        variablePropertyAccessor = new ReflectionPropertyAccessor(propertyDescriptor);
    }

    public void processAnnotations(DescriptorPolicy descriptorPolicy) {
        processPropertyAnnotations(descriptorPolicy);
    }

    private void processPropertyAnnotations(DescriptorPolicy descriptorPolicy) {
        PlanningVariable planningVariableAnnotation = variablePropertyAccessor.getReadMethod()
                .getAnnotation(PlanningVariable.class);
        // Keep in sync with ShadowVariableDescriptor.processPropertyAnnotations()
        processMappedBy(descriptorPolicy, planningVariableAnnotation);
        processNullable(descriptorPolicy, planningVariableAnnotation);
        processChained(descriptorPolicy, planningVariableAnnotation);
        processValueRangeRefs(descriptorPolicy, planningVariableAnnotation);
        processStrength(descriptorPolicy, planningVariableAnnotation);
        processVariableListeners(descriptorPolicy, planningVariableAnnotation);
    }

    private void processMappedBy(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        String mappedBy = planningVariableAnnotation.mappedBy();
        if (!mappedBy.equals("")) {
            throw new IllegalStateException("Impossible state: the " + EntityDescriptor.class
                    + " would never try to build a " + GenuineVariableDescriptor.class
                    + " for a shadow variable with mappedBy (" + mappedBy + ").");
        }
    }

    private void processNullable(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        nullable = planningVariableAnnotation.nullable();
        if (nullable && variablePropertyAccessor.getPropertyType().isPrimitive()) {
            throw new IllegalArgumentException("The planningEntityClass ("
                    + entityDescriptor.getEntityClass()
                    + ") has a PlanningVariable annotated property (" + variablePropertyAccessor.getName()
                    + ") with nullable (" + nullable + "), which is not compatible with the primitive propertyType ("
                    + variablePropertyAccessor.getPropertyType() + ").");
        }
        Class<? extends SelectionFilter> reinitializeVariableEntityFilterClass
                = planningVariableAnnotation.reinitializeVariableEntityFilter();
        if (reinitializeVariableEntityFilterClass == PlanningVariable.NullReinitializeVariableEntityFilter.class) {
            reinitializeVariableEntityFilterClass = null;
        }
        if (reinitializeVariableEntityFilterClass != null) {
            reinitializeVariableEntityFilter = ConfigUtils.newInstance(this,
                    "reinitializeVariableEntityFilterClass", reinitializeVariableEntityFilterClass);
        } else {
            reinitializeVariableEntityFilter = new NullValueReinitializeVariableEntityFilter(this);
        }
    }

    private void processChained(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        chained = planningVariableAnnotation.chained();
        if (chained && !variablePropertyAccessor.getPropertyType().isAssignableFrom(
                entityDescriptor.getEntityClass())) {
            throw new IllegalArgumentException("The planningEntityClass ("
                    + entityDescriptor.getEntityClass()
                    + ") has a PlanningVariable annotated property (" + variablePropertyAccessor.getName()
                    + ") with chained (" + chained + ") and propertyType (" + variablePropertyAccessor.getPropertyType()
                    + ") which is not a superclass/interface of or the same as the planningEntityClass ("
                    + entityDescriptor.getEntityClass() + ").");
        }
        if (chained && nullable) {
            throw new IllegalArgumentException("The planningEntityClass ("
                    + entityDescriptor.getEntityClass()
                    + ") has a PlanningVariable annotated property (" + variablePropertyAccessor.getName()
                    + ") with chained (" + chained + "), which is not compatible with nullable (" + nullable + ").");
        }
    }

    private void processValueRangeRefs(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        String[] valueRangeProviderRefs = planningVariableAnnotation.valueRangeProviderRefs();
        if (ArrayUtils.isEmpty(valueRangeProviderRefs)) {
            throw new IllegalArgumentException("The planningEntityClass (" + entityDescriptor.getEntityClass()
                    + ") has a " + PlanningVariable.class.getSimpleName()
                    + " annotated property (" + variablePropertyAccessor.getName()
                    + ") that has no valueRangeProviderRefs (" + Arrays.toString(valueRangeProviderRefs) + ").");
        }
        List<ValueRangeDescriptor> valueRangeDescriptorList
                = new ArrayList<ValueRangeDescriptor>(valueRangeProviderRefs.length);
        boolean addNullInValueRange = nullable && valueRangeProviderRefs.length == 1;
        for (String valueRangeProviderRef : valueRangeProviderRefs) {
            valueRangeDescriptorList.add(buildValueRangeDescriptor(descriptorPolicy, valueRangeProviderRef, addNullInValueRange));
        }
        if (valueRangeDescriptorList.size() == 1) {
            valueRangeDescriptor = valueRangeDescriptorList.get(0);
        } else {
            valueRangeDescriptor = new CompositeValueRangeDescriptor(this, nullable, valueRangeDescriptorList);
        }
    }

    private ValueRangeDescriptor buildValueRangeDescriptor(DescriptorPolicy descriptorPolicy,
            String valueRangeProviderRef, boolean addNullInValueRange) {
        if (descriptorPolicy.hasFromSolutionValueRangeProvider(valueRangeProviderRef)) {
            Method readMethod = descriptorPolicy.getFromSolutionValueRangeProvider(valueRangeProviderRef);
            return new FromSolutionPropertyValueRangeDescriptor(this, addNullInValueRange, readMethod);
        } else if (descriptorPolicy.hasFromEntityValueRangeProvider(valueRangeProviderRef)) {
            Method readMethod = descriptorPolicy.getFromEntityValueRangeProvider(valueRangeProviderRef);
            return new FromEntityPropertyValueRangeDescriptor(this, addNullInValueRange, readMethod);
        } else {
            throw new IllegalArgumentException("The planningEntityClass ("
                    + entityDescriptor.getEntityClass()
                    + ") has a " + PlanningVariable.class.getSimpleName()
                    + ") annotated property (" + variablePropertyAccessor.getName()
                    + ") with a valueRangeProviderRef (" + valueRangeProviderRef
                    + ") that does not exist on a registered " + PlanningSolution.class.getSimpleName()
                    + " or " + PlanningEntity.class.getSimpleName() + ".\n"
                    + "The valueRangeProviderRef (" + valueRangeProviderRef
                    + ") does not appear in valueRangeProvideIds (" + descriptorPolicy.getValueRangeProviderIds()
                    + ").");
        }
    }

    private void processStrength(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        Class<? extends Comparator> strengthComparatorClass = planningVariableAnnotation.strengthComparatorClass();
        if (strengthComparatorClass == PlanningVariable.NullStrengthComparator.class) {
            strengthComparatorClass = null;
        }
        Class<? extends SelectionSorterWeightFactory> strengthWeightFactoryClass
                = planningVariableAnnotation.strengthWeightFactoryClass();
        if (strengthWeightFactoryClass == PlanningVariable.NullStrengthWeightFactory.class) {
            strengthWeightFactoryClass = null;
        }
        if (strengthComparatorClass != null && strengthWeightFactoryClass != null) {
            throw new IllegalStateException("The planningEntityClass ("
                    + entityDescriptor.getEntityClass()  + ") property ("
                    + variablePropertyAccessor.getName()
                    + ") cannot have a strengthComparatorClass (" + strengthComparatorClass.getName()
                    + ") and a strengthWeightFactoryClass (" + strengthWeightFactoryClass.getName()
                    + ") at the same time.");
        }
        if (strengthComparatorClass != null) {
            Comparator<Object> strengthComparator = ConfigUtils.newInstance(this,
                    "strengthComparatorClass", strengthComparatorClass);
            increasingStrengthSorter = new ComparatorSelectionSorter(
                    strengthComparator, SelectionSorterOrder.ASCENDING);
        }
        if (strengthWeightFactoryClass != null) {
            SelectionSorterWeightFactory strengthWeightFactory = ConfigUtils.newInstance(this,
                    "strengthWeightFactoryClass", strengthWeightFactoryClass);
            increasingStrengthSorter = new WeightFactorySelectionSorter(
                    strengthWeightFactory, SelectionSorterOrder.ASCENDING);
        }
    }

    private void processVariableListeners(DescriptorPolicy descriptorPolicy, PlanningVariable planningVariableAnnotation) {
        Class<? extends VariableListener>[] variableListenerClasses
                = planningVariableAnnotation.variableListenerClasses();
        nonMappedByVariableListeners = new ArrayList<VariableListener>(variableListenerClasses.length);
        for (Class<? extends VariableListener> variableListenerClass : variableListenerClasses) {
            nonMappedByVariableListeners.add(
                    ConfigUtils.newInstance(this, "variableListenerClass", variableListenerClass));
        }
    }

    public void afterAnnotationsProcessed(DescriptorPolicy descriptorPolicy) {
        // Do nothing
    }

    public void registerShadowVariableDescriptor(ShadowVariableDescriptor shadowVariableDescriptor) {
        shadowVariableDescriptorList.add(shadowVariableDescriptor);
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    public EntityDescriptor getEntityDescriptor() {
        return entityDescriptor;
    }

    public String getVariableName() {
        return variablePropertyAccessor.getName();
    }

    public Class<?> getVariablePropertyType() {
        return variablePropertyAccessor.getPropertyType();
    }

    public boolean matchesEntityVariable(Object entity, String variableName) {
        return variableName.equals(getVariableName()) && entityDescriptor.matchesEntity(entity);
    }

    public boolean isChained() {
        return chained;
    }

    public boolean isNullable() {
        return nullable;
    }

    public SelectionFilter getReinitializeVariableEntityFilter() {
        return reinitializeVariableEntityFilter;
    }

    public ValueRangeDescriptor getValueRangeDescriptor() {
        return valueRangeDescriptor;
    }

    public List<VariableListener> buildVariableListenerList() {
        List<VariableListener> variableListenerList = new ArrayList<VariableListener>(
                shadowVariableDescriptorList.size() + nonMappedByVariableListeners.size());
        // Always trigger the build-in shadow variables first
        for (ShadowVariableDescriptor shadowVariableDescriptor : shadowVariableDescriptorList) {
            variableListenerList.add(shadowVariableDescriptor.buildVariableListener());
        }
        // Always trigger the non build-in shadow variables last
        for (VariableListener variableListener : nonMappedByVariableListeners) {
            variableListenerList.add(variableListener);
        }
        return variableListenerList;
    }

    // ************************************************************************
    // Extraction methods
    // ************************************************************************

    /**
     * A {@link PlanningVariable#nullable()} value is always considered initialized, but it can still be reinitialized
     * with {@link PlanningVariable#reinitializeVariableEntityFilter()}.
     * @param entity never null
     * @return true if the variable on that entity is initialized
     */
    public boolean isInitialized(Object entity) {
        if (nullable) {
            return true;
        }
        Object variable = getValue(entity);
        return variable != null;
    }

    public Object getValue(Object entity) {
        return variablePropertyAccessor.executeGetter(entity);
    }

    public void setValue(Object entity, Object value) {
        variablePropertyAccessor.executeSetter(entity, value);
    }

    public SelectionSorter getIncreasingStrengthSorter() {
        return increasingStrengthSorter;
    }

    public long getValueCount(Solution solution, Object entity) {
        if (!valueRangeDescriptor.isCountable()) {
            // TODO report this better than just ignoring it
            return 0L;
        }
        return ((CountableValueRange<?>) valueRangeDescriptor.extractValueRange(solution, entity)).getSize();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + variablePropertyAccessor.getName()
                + " of " + entityDescriptor.getEntityClass().getName() + ")";
    }

}
