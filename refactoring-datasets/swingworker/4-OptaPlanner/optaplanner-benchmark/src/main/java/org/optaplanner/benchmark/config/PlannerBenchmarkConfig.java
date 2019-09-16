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

package org.optaplanner.benchmark.config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.optaplanner.benchmark.api.PlannerBenchmark;
import org.optaplanner.benchmark.config.report.BenchmarkReportConfig;
import org.optaplanner.benchmark.impl.result.PlannerBenchmarkResult;
import org.optaplanner.benchmark.impl.PlannerBenchmarkRunner;
import org.optaplanner.benchmark.impl.result.ProblemBenchmarkResult;
import org.optaplanner.benchmark.impl.result.SolverBenchmarkResult;
import org.optaplanner.core.config.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XStreamAlias("plannerBenchmark")
public class PlannerBenchmarkConfig {

    public static final String PARALLEL_BENCHMARK_COUNT_AUTO = "AUTO";
    /**
     * @see Runtime#availableProcessors()
     */
    public static final String AVAILABLE_PROCESSOR_COUNT = "availableProcessorCount";

    protected final transient Logger logger = LoggerFactory.getLogger(getClass());

    private String name = null;
    private File benchmarkDirectory = null;

    private String parallelBenchmarkCount = null;
    private Long warmUpMillisecondsSpentLimit = null;
    private Long warmUpSecondsSpentLimit = null;
    private Long warmUpMinutesSpentLimit = null;
    private Long warmUpHoursSpentLimit = null;

    @XStreamAlias("benchmarkReport")
    private BenchmarkReportConfig benchmarkReportConfig = null;

    @XStreamAlias("inheritedSolverBenchmark")
    private SolverBenchmarkConfig inheritedSolverBenchmarkConfig = null;

    @XStreamImplicit(itemFieldName = "solverBenchmark")
    private List<SolverBenchmarkConfig> solverBenchmarkConfigList = null;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getBenchmarkDirectory() {
        return benchmarkDirectory;
    }

    public void setBenchmarkDirectory(File benchmarkDirectory) {
        this.benchmarkDirectory = benchmarkDirectory;
    }

    /**
     * Using multiple parallel benchmarks can decrease the reliability of the results.
     * <p/>
     * If there aren't enough processors available, it will be decreased.
     * @return null, {@value #PARALLEL_BENCHMARK_COUNT_AUTO}
     * or a JavaScript calculation using {@value #AVAILABLE_PROCESSOR_COUNT}.
     */
    public String getParallelBenchmarkCount() {
        return parallelBenchmarkCount;
    }

    public void setParallelBenchmarkCount(String parallelBenchmarkCount) {
        this.parallelBenchmarkCount = parallelBenchmarkCount;
    }

    public Long getWarmUpMillisecondsSpentLimit() {
        return warmUpMillisecondsSpentLimit;
    }

    public void setWarmUpMillisecondsSpentLimit(Long warmUpMillisecondsSpentLimit) {
        this.warmUpMillisecondsSpentLimit = warmUpMillisecondsSpentLimit;
    }

    public Long getWarmUpSecondsSpentLimit() {
        return warmUpSecondsSpentLimit;
    }

    public void setWarmUpSecondsSpentLimit(Long warmUpSecondsSpentLimit) {
        this.warmUpSecondsSpentLimit = warmUpSecondsSpentLimit;
    }

    public Long getWarmUpMinutesSpentLimit() {
        return warmUpMinutesSpentLimit;
    }

    public void setWarmUpMinutesSpentLimit(Long warmUpMinutesSpentLimit) {
        this.warmUpMinutesSpentLimit = warmUpMinutesSpentLimit;
    }

    public Long getWarmUpHoursSpentLimit() {
        return warmUpHoursSpentLimit;
    }

    public void setWarmUpHoursSpentLimit(Long warmUpHoursSpentLimit) {
        this.warmUpHoursSpentLimit = warmUpHoursSpentLimit;
    }

    public BenchmarkReportConfig getBenchmarkReportConfig() {
        return benchmarkReportConfig;
    }

    public void setBenchmarkReportConfig(BenchmarkReportConfig benchmarkReportConfig) {
        this.benchmarkReportConfig = benchmarkReportConfig;
    }

    public SolverBenchmarkConfig getInheritedSolverBenchmarkConfig() {
        return inheritedSolverBenchmarkConfig;
    }

    public void setInheritedSolverBenchmarkConfig(SolverBenchmarkConfig inheritedSolverBenchmarkConfig) {
        this.inheritedSolverBenchmarkConfig = inheritedSolverBenchmarkConfig;
    }

    public List<SolverBenchmarkConfig> getSolverBenchmarkConfigList() {
        return solverBenchmarkConfigList;
    }

    public void setSolverBenchmarkConfigList(List<SolverBenchmarkConfig> solverBenchmarkConfigList) {
        this.solverBenchmarkConfigList = solverBenchmarkConfigList;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public PlannerBenchmark buildPlannerBenchmark() {
        validate();
        generateSolverBenchmarkConfigNames();
        inherit();

        PlannerBenchmarkResult plannerBenchmarkResult = new PlannerBenchmarkResult();
        plannerBenchmarkResult.setName(name);
        plannerBenchmarkResult.setAggregation(false);
        PlannerBenchmarkRunner plannerBenchmarkRunner = new PlannerBenchmarkRunner(plannerBenchmarkResult);
        plannerBenchmarkRunner.setBenchmarkDirectory(benchmarkDirectory);
        plannerBenchmarkResult.setParallelBenchmarkCount(resolveParallelBenchmarkCount());
        plannerBenchmarkResult.setWarmUpTimeMillisSpentLimit(calculateWarmUpTimeMillisSpentLimit());
        BenchmarkReportConfig benchmarkReportConfig_ = benchmarkReportConfig == null ? new BenchmarkReportConfig()
                : benchmarkReportConfig;
        plannerBenchmarkRunner.setBenchmarkReport(benchmarkReportConfig_.buildBenchmarkReport(plannerBenchmarkResult));

        List<SolverBenchmarkResult> solverBenchmarkResultList = new ArrayList<SolverBenchmarkResult>(solverBenchmarkConfigList.size());
        List<ProblemBenchmarkResult> unifiedProblemBenchmarkResultList = new ArrayList<ProblemBenchmarkResult>();
        plannerBenchmarkResult.setUnifiedProblemBenchmarkResultList(unifiedProblemBenchmarkResultList);
        for (SolverBenchmarkConfig solverBenchmarkConfig : solverBenchmarkConfigList) {
            SolverBenchmarkResult solverBenchmarkResult = solverBenchmarkConfig.buildSolverBenchmark(plannerBenchmarkResult);
            solverBenchmarkResultList.add(solverBenchmarkResult);
        }
        plannerBenchmarkResult.setSolverBenchmarkResultList(solverBenchmarkResultList);
        return plannerBenchmarkRunner;
    }

    protected void validate() {
        final String nameRegex = "^[\\w\\d _\\-\\.\\(\\)]+$";
        if (name != null && !name.matches(nameRegex)) {
            throw new IllegalStateException("The plannerBenchmark name (" + name
                    + ") is invalid because it does not follow the nameRegex (" + nameRegex + ")" +
                    " which might cause an illegal filename.");
        }
        if (ConfigUtils.isEmptyCollection(solverBenchmarkConfigList)) {
            throw new IllegalArgumentException(
                    "Configure at least 1 <solverBenchmark> in the <plannerBenchmark> configuration.");
        }
    }

    protected void generateSolverBenchmarkConfigNames() {
        Set<String> nameSet = new HashSet<String>(solverBenchmarkConfigList.size());
        Set<SolverBenchmarkConfig> noNameBenchmarkConfigSet = new LinkedHashSet<SolverBenchmarkConfig>(solverBenchmarkConfigList.size());
        for (SolverBenchmarkConfig solverBenchmarkConfig : solverBenchmarkConfigList) {
            if (solverBenchmarkConfig.getName() != null) {
                boolean unique = nameSet.add(solverBenchmarkConfig.getName());
                if (!unique) {
                    throw new IllegalStateException("The benchmark name (" + solverBenchmarkConfig.getName()
                            + ") is used in more than 1 benchmark.");
                }
            } else {
                noNameBenchmarkConfigSet.add(solverBenchmarkConfig);
            }
        }
        int generatedNameIndex = 0;
        for (SolverBenchmarkConfig solverBenchmarkConfig : noNameBenchmarkConfigSet) {
            String generatedName = "Config_" + generatedNameIndex;
            while (nameSet.contains(generatedName)) {
                generatedNameIndex++;
                generatedName = "Config_" + generatedNameIndex;
            }
            solverBenchmarkConfig.setName(generatedName);
            generatedNameIndex++;
        }
    }

    protected void inherit() {
        if (inheritedSolverBenchmarkConfig != null) {
            for (SolverBenchmarkConfig solverBenchmarkConfig : solverBenchmarkConfigList) {
                solverBenchmarkConfig.inherit(inheritedSolverBenchmarkConfig);
            }
        }
    }

    protected int resolveParallelBenchmarkCount() {
        int availableProcessorCount = Runtime.getRuntime().availableProcessors();
        int resolvedParallelBenchmarkCount;
        if (parallelBenchmarkCount == null) {
            resolvedParallelBenchmarkCount = 1;
        } else if (parallelBenchmarkCount.equals(PARALLEL_BENCHMARK_COUNT_AUTO)) {
            // TODO Tweak it based on experience
            if (availableProcessorCount <= 2) {
                resolvedParallelBenchmarkCount = 1;
            } else if (availableProcessorCount <= 4) {
                resolvedParallelBenchmarkCount = 2;
            } else {
                resolvedParallelBenchmarkCount = (availableProcessorCount / 2) + 1;
            }
        } else {
            String scriptLanguage = "JavaScript";
            ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName(scriptLanguage);
            scriptEngine.put(AVAILABLE_PROCESSOR_COUNT, availableProcessorCount);
            Object scriptResult;
            try {
                scriptResult = scriptEngine.eval(parallelBenchmarkCount);
            } catch (ScriptException e) {
                throw new IllegalArgumentException("The parallelBenchmarkCount (" + parallelBenchmarkCount
                        + ") is not " + PARALLEL_BENCHMARK_COUNT_AUTO + " and cannot be parsed in " + scriptLanguage
                        + " with the variables ([" + AVAILABLE_PROCESSOR_COUNT + "]).", e);
            }
            if (!(scriptResult instanceof Number)) {
                throw new IllegalArgumentException("The parallelBenchmarkCount (" + parallelBenchmarkCount
                        + ") is resolved to scriptResult (" + scriptResult + ") in " + scriptLanguage
                        + " and is not a Number.");
            }
            resolvedParallelBenchmarkCount = ((Number) scriptResult).intValue();
        }
        if (resolvedParallelBenchmarkCount < 1) {
            throw new IllegalArgumentException("The parallelBenchmarkCount (" + parallelBenchmarkCount
                    + ") resulted in a resolvedParallelBenchmarkCount (" + resolvedParallelBenchmarkCount
                    + ") that is lower than 1.");
        }
        if (resolvedParallelBenchmarkCount > availableProcessorCount) {
            logger.warn("Because the resolvedParallelBenchmarkCount (" + resolvedParallelBenchmarkCount
                    + ") is higher than the availableProcessorCount (" + availableProcessorCount
                    + "), it is reduced to availableProcessorCount.");
            resolvedParallelBenchmarkCount = availableProcessorCount;
        }
        return resolvedParallelBenchmarkCount;
    }

    protected long calculateWarmUpTimeMillisSpentLimit() {
        long warmUpTimeMillisSpentLimit = 0L;
        if (warmUpMillisecondsSpentLimit != null) {
            warmUpTimeMillisSpentLimit += warmUpMillisecondsSpentLimit;
        }
        if (warmUpSecondsSpentLimit != null) {
            warmUpTimeMillisSpentLimit += warmUpSecondsSpentLimit * 1000L;
        }
        if (warmUpMinutesSpentLimit != null) {
            warmUpTimeMillisSpentLimit += warmUpMinutesSpentLimit * 60000L;
        }
        if (warmUpHoursSpentLimit != null) {
            warmUpTimeMillisSpentLimit += warmUpHoursSpentLimit * 3600000L;
        }
        return warmUpTimeMillisSpentLimit;
    }

}
