/*
 * Copyright 2011 JBoss Inc
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

package org.optaplanner.benchmark.impl.statistic.calculatecount;

import java.util.ArrayList;
import java.util.List;

import org.optaplanner.benchmark.impl.result.SingleBenchmarkResult;
import org.optaplanner.benchmark.impl.statistic.ProblemStatisticType;
import org.optaplanner.benchmark.impl.statistic.SingleStatistic;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.impl.phase.event.SolverPhaseLifecycleListenerAdapter;
import org.optaplanner.core.impl.phase.scope.AbstractStepScope;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.solver.scope.DefaultSolverScope;

public class CalculateCountSingleStatistic extends SingleStatistic<CalculateCountStatisticPoint> {

    private final long timeMillisThresholdInterval;

    private final CalculateCountSingleStatisticListener listener;

    private List<CalculateCountStatisticPoint> pointList;

    public CalculateCountSingleStatistic(SingleBenchmarkResult singleBenchmarkResult) {
        this(singleBenchmarkResult, 1000L);
    }

    public CalculateCountSingleStatistic(SingleBenchmarkResult singleBenchmarkResult, long timeMillisThresholdInterval) {
        super(singleBenchmarkResult, ProblemStatisticType.CALCULATE_COUNT_PER_SECOND);
        if (timeMillisThresholdInterval <= 0L) {
            throw new IllegalArgumentException("The timeMillisThresholdInterval (" + timeMillisThresholdInterval
                    + ") must be bigger than 0.");
        }
        this.timeMillisThresholdInterval = timeMillisThresholdInterval;
        listener = new CalculateCountSingleStatisticListener();
        pointList = new ArrayList<CalculateCountStatisticPoint>();
    }

    public List<CalculateCountStatisticPoint> getPointList() {
        return pointList;
    }

    @Override
    public void setPointList(List<CalculateCountStatisticPoint> pointList) {
        this.pointList = pointList;
    }

    // ************************************************************************
    // Lifecycle methods
    // ************************************************************************

    public void open(Solver solver) {
        ((DefaultSolver) solver).addSolverPhaseLifecycleListener(listener);
    }

    public void close(Solver solver) {
        ((DefaultSolver) solver).removeSolverPhaseLifecycleListener(listener);
    }

    private class CalculateCountSingleStatisticListener extends SolverPhaseLifecycleListenerAdapter {

        private long nextTimeMillisThreshold = timeMillisThresholdInterval;
        private long lastTimeMillisSpent = 0L;
        private long lastCalculateCount = 0L;

        @Override
        public void stepEnded(AbstractStepScope stepScope) {
            long timeMillisSpent = stepScope.getPhaseScope().calculateSolverTimeMillisSpent();
            if (timeMillisSpent >= nextTimeMillisThreshold) {
                DefaultSolverScope solverScope = stepScope.getPhaseScope().getSolverScope();
                long calculateCount = solverScope.getCalculateCount();
                long calculateCountInterval = calculateCount - lastCalculateCount;
                long timeMillisSpentInterval = timeMillisSpent - lastTimeMillisSpent;
                if (timeMillisSpentInterval == 0L) {
                    // Avoid divide by zero exception on a fast CPU
                    timeMillisSpentInterval = 1L;
                }
                long averageCalculateCountPerSecond = calculateCountInterval * 1000L / timeMillisSpentInterval;
                pointList.add(new CalculateCountStatisticPoint(timeMillisSpent, averageCalculateCountPerSecond));
                lastCalculateCount = calculateCount;

                lastTimeMillisSpent = timeMillisSpent;
                nextTimeMillisThreshold += timeMillisThresholdInterval;
                if (nextTimeMillisThreshold < timeMillisSpent) {
                    nextTimeMillisThreshold = timeMillisSpent;
                }
            }
        }

    }

    // ************************************************************************
    // CSV methods
    // ************************************************************************

    @Override
    protected String getCsvHeader() {
        return CalculateCountStatisticPoint.buildCsvLine("timeMillisSpent", "calculateCountPerSecond");
    }

    @Override
    protected CalculateCountStatisticPoint createPointFromCsvLine(ScoreDefinition scoreDefinition,
            List<String> csvLine) {
        return new CalculateCountStatisticPoint(Long.valueOf(csvLine.get(0)),
                Long.valueOf(csvLine.get(1)));
    }

}
