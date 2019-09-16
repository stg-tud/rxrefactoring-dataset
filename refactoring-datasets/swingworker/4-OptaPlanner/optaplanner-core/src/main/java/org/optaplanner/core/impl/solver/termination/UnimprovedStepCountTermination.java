/*
 * Copyright 2010 JBoss Inc
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

package org.optaplanner.core.impl.solver.termination;

import org.optaplanner.core.impl.phase.scope.AbstractSolverPhaseScope;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

public class UnimprovedStepCountTermination extends AbstractTermination {

    private final int unimprovedStepCountLimit;

    public UnimprovedStepCountTermination(int unimprovedStepCountLimit) {
        this.unimprovedStepCountLimit = unimprovedStepCountLimit;
        if (unimprovedStepCountLimit < 0) {
            throw new IllegalArgumentException("The unimprovedStepCountLimit (" + unimprovedStepCountLimit
                    + ") cannot be negative.");
        }
    }

    // ************************************************************************
    // Terminated methods
    // ************************************************************************

    public boolean isSolverTerminated(DefaultSolverScope solverScope) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " can only be used for phase termination.");
    }

    public boolean isPhaseTerminated(AbstractSolverPhaseScope phaseScope) {
        int unimprovedStepCount = calculateUnimprovedStepCount(phaseScope);
        return unimprovedStepCount >= unimprovedStepCountLimit;
    }

    protected int calculateUnimprovedStepCount(AbstractSolverPhaseScope phaseScope) {
        int bestStepIndex = phaseScope.getBestSolutionStepIndex();
        int lastStepIndex = phaseScope.getLastCompletedStepScope().getStepIndex();
        return lastStepIndex - bestStepIndex;
    }

    // ************************************************************************
    // Time gradient methods
    // ************************************************************************

    public double calculateSolverTimeGradient(DefaultSolverScope solverScope) {
        throw new UnsupportedOperationException(
                getClass().getSimpleName() + " can only be used for phase termination.");
    }

    public double calculatePhaseTimeGradient(AbstractSolverPhaseScope phaseScope) {
        int unimprovedStepCount = calculateUnimprovedStepCount(phaseScope);
        double timeGradient = ((double) unimprovedStepCount) / ((double) unimprovedStepCountLimit);
        return Math.min(timeGradient, 1.0);
    }

}
