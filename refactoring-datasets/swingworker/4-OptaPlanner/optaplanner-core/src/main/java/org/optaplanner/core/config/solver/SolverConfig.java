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

package org.optaplanner.core.config.solver;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.config.heuristic.policy.HeuristicConfigPolicy;
import org.optaplanner.core.config.phase.SolverPhaseConfig;
import org.optaplanner.core.config.score.director.ScoreDirectorFactoryConfig;
import org.optaplanner.core.config.solver.random.RandomType;
import org.optaplanner.core.config.solver.termination.TerminationConfig;
import org.optaplanner.core.config.util.ConfigUtils;
import org.optaplanner.core.impl.domain.entity.descriptor.EntityDescriptor;
import org.optaplanner.core.impl.solver.recaller.BestSolutionRecaller;
import org.optaplanner.core.impl.domain.policy.DescriptorPolicy;
import org.optaplanner.core.impl.domain.solution.descriptor.SolutionDescriptor;
import org.optaplanner.core.impl.phase.AbstractSolverPhase;
import org.optaplanner.core.impl.phase.SolverPhase;
import org.optaplanner.core.impl.score.definition.ScoreDefinition;
import org.optaplanner.core.impl.score.director.ScoreDirectorFactory;
import org.optaplanner.core.impl.solution.Solution;
import org.optaplanner.core.impl.solver.BasicPlumbingTermination;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.solver.random.DefaultRandomFactory;
import org.optaplanner.core.impl.solver.random.RandomFactory;
import org.optaplanner.core.impl.solver.termination.Termination;

@XStreamAlias("solver")
public class SolverConfig {

    protected static final long DEFAULT_RANDOM_SEED = 0L;

    // Warning: all fields are null (and not defaulted) because they can be inherited
    // and also because the input config file should match the output config file

    protected EnvironmentMode environmentMode = null;
    protected RandomType randomType = null;
    protected Long randomSeed = null;
    protected Class<? extends RandomFactory> randomFactoryClass = null;

    protected Class<? extends Solution> solutionClass = null;
    @XStreamImplicit(itemFieldName = "planningEntityClass")
    protected List<Class<?>> planningEntityClassList = null;

    @XStreamAlias("scoreDirectorFactory")
    protected ScoreDirectorFactoryConfig scoreDirectorFactoryConfig = null;

    @XStreamAlias("termination")
    private TerminationConfig terminationConfig;

    @XStreamImplicit()
    protected List<SolverPhaseConfig> solverPhaseConfigList = null;

    public EnvironmentMode getEnvironmentMode() {
        return environmentMode;
    }

    public void setEnvironmentMode(EnvironmentMode environmentMode) {
        this.environmentMode = environmentMode;
    }

    public RandomType getRandomType() {
        return randomType;
    }

    public void setRandomType(RandomType randomType) {
        this.randomType = randomType;
    }

    public Long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(Long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public Class<? extends RandomFactory> getRandomFactoryClass() {
        return randomFactoryClass;
    }

    public void setRandomFactoryClass(Class<? extends RandomFactory> randomFactoryClass) {
        this.randomFactoryClass = randomFactoryClass;
    }

    public Class<? extends Solution> getSolutionClass() {
        return solutionClass;
    }

    public void setSolutionClass(Class<? extends Solution> solutionClass) {
        this.solutionClass = solutionClass;
    }

    public List<Class<?>> getPlanningEntityClassList() {
        return planningEntityClassList;
    }

    public void setPlanningEntityClassList(List<Class<?>> planningEntityClassList) {
        this.planningEntityClassList = planningEntityClassList;
    }

    public ScoreDirectorFactoryConfig getScoreDirectorFactoryConfig() {
        return scoreDirectorFactoryConfig;
    }

    public void setScoreDirectorFactoryConfig(ScoreDirectorFactoryConfig scoreDirectorFactoryConfig) {
        this.scoreDirectorFactoryConfig = scoreDirectorFactoryConfig;
    }

    public TerminationConfig getTerminationConfig() {
        return terminationConfig;
    }

    public void setTerminationConfig(TerminationConfig terminationConfig) {
        this.terminationConfig = terminationConfig;
    }

    public List<SolverPhaseConfig> getSolverPhaseConfigList() {
        return solverPhaseConfigList;
    }

    public void setSolverPhaseConfigList(List<SolverPhaseConfig> solverPhaseConfigList) {
        this.solverPhaseConfigList = solverPhaseConfigList;
    }

    // ************************************************************************
    // Builder methods
    // ************************************************************************

    public Solver buildSolver() {
        DefaultSolver solver = new DefaultSolver();
        BasicPlumbingTermination basicPlumbingTermination = new BasicPlumbingTermination();
        solver.setBasicPlumbingTermination(basicPlumbingTermination);
        EnvironmentMode environmentMode = this.environmentMode == null ? EnvironmentMode.REPRODUCIBLE
                : this.environmentMode;

        RandomFactory randomFactory;
        if (randomFactoryClass != null) {
            if (randomType != null || randomSeed != null) {
                throw new IllegalArgumentException(
                        "The solverConfig with randomFactoryClass (" + randomFactoryClass
                                + ") has a non-null randomType (" + randomType
                                + ") or a non-null randomSeed (" + randomSeed + ").");
            }
            randomFactory = ConfigUtils.newInstance(this, "randomFactoryClass", randomFactoryClass);
        } else {
            RandomType randomType_ = randomType == null ? RandomType.JDK : randomType;
            Long randomSeed_ = randomSeed;
            if (randomSeed == null && environmentMode != EnvironmentMode.PRODUCTION) {
                randomSeed_ = DEFAULT_RANDOM_SEED;
            }
            randomFactory = new DefaultRandomFactory(randomType_, randomSeed_);
        }
        solver.setRandomFactory(randomFactory);
        SolutionDescriptor solutionDescriptor = buildSolutionDescriptor();
        ScoreDirectorFactoryConfig scoreDirectorFactoryConfig_
                = scoreDirectorFactoryConfig == null ? new ScoreDirectorFactoryConfig()
                : scoreDirectorFactoryConfig;
        ScoreDirectorFactory scoreDirectorFactory = scoreDirectorFactoryConfig_.buildScoreDirectorFactory(
                environmentMode, solutionDescriptor);
        solver.setScoreDirectorFactory(scoreDirectorFactory);
        HeuristicConfigPolicy configPolicy = new HeuristicConfigPolicy(
                environmentMode, scoreDirectorFactory);
        TerminationConfig terminationConfig_ = terminationConfig == null ? new TerminationConfig()
                : terminationConfig;
        Termination termination = terminationConfig_.buildTermination(configPolicy, basicPlumbingTermination);
        solver.setTermination(termination);
        BestSolutionRecaller bestSolutionRecaller = buildBestSolutionRecaller(environmentMode);
        solver.setBestSolutionRecaller(bestSolutionRecaller);
        if (ConfigUtils.isEmptyCollection(solverPhaseConfigList)) {
            throw new IllegalArgumentException(
                    "Configure at least 1 phase (for example <localSearch>) in the solver configuration.");
        }
        List<SolverPhase> solverPhaseList = new ArrayList<SolverPhase>(solverPhaseConfigList.size());
        int phaseIndex = 0;
        for (SolverPhaseConfig solverPhaseConfig : solverPhaseConfigList) {
            SolverPhase solverPhase = solverPhaseConfig.buildSolverPhase(phaseIndex, configPolicy,
                    bestSolutionRecaller, termination);
            solverPhaseList.add(solverPhase);
            phaseIndex++;
        }
        solver.setSolverPhaseList(solverPhaseList);
        return solver;
    }

    protected BestSolutionRecaller buildBestSolutionRecaller(EnvironmentMode environmentMode) {
        BestSolutionRecaller bestSolutionRecaller = new BestSolutionRecaller();
        if (environmentMode.isNonIntrusiveFullAsserted()) {
            bestSolutionRecaller.setAssertBestScoreIsUnmodified(true);
        }
        return bestSolutionRecaller;
    }

    protected SolutionDescriptor buildSolutionDescriptor() {
        if (solutionClass == null) {
            throw new IllegalArgumentException("Configure a <solutionClass> in the solver configuration.");
        }
        DescriptorPolicy descriptorPolicy = new DescriptorPolicy();
        SolutionDescriptor solutionDescriptor = new SolutionDescriptor(solutionClass);
        solutionDescriptor.processAnnotations(descriptorPolicy);
        if (ConfigUtils.isEmptyCollection(planningEntityClassList)) {
            throw new IllegalArgumentException(
                    "Configure at least 1 <planningEntityClass> in the solver configuration.");
        }
        for (Class<?> planningEntityClass : planningEntityClassList) {
            EntityDescriptor entityDescriptor = new EntityDescriptor(
                    solutionDescriptor, planningEntityClass);
            solutionDescriptor.addEntityDescriptor(entityDescriptor);
            entityDescriptor.processAnnotations(descriptorPolicy);
        }
        solutionDescriptor.afterAnnotationsProcessed(descriptorPolicy);
        return solutionDescriptor;
    }

    public void inherit(SolverConfig inheritedConfig) {
        environmentMode = ConfigUtils.inheritOverwritableProperty(environmentMode, inheritedConfig.getEnvironmentMode());
        randomType = ConfigUtils.inheritOverwritableProperty(randomType, inheritedConfig.getRandomType());
        randomSeed = ConfigUtils.inheritOverwritableProperty(randomSeed, inheritedConfig.getRandomSeed());
        randomFactoryClass = ConfigUtils.inheritOverwritableProperty(
                randomFactoryClass, inheritedConfig.getRandomFactoryClass());
        solutionClass = ConfigUtils.inheritOverwritableProperty(solutionClass, inheritedConfig.getSolutionClass());
        planningEntityClassList = ConfigUtils.inheritMergeableListProperty(
                planningEntityClassList, inheritedConfig.getPlanningEntityClassList());
        if (scoreDirectorFactoryConfig == null) {
            scoreDirectorFactoryConfig = inheritedConfig.getScoreDirectorFactoryConfig();
        } else if (inheritedConfig.getScoreDirectorFactoryConfig() != null) {
            scoreDirectorFactoryConfig.inherit(inheritedConfig.getScoreDirectorFactoryConfig());
        }
        if (terminationConfig == null) {
            terminationConfig = inheritedConfig.getTerminationConfig();
        } else if (inheritedConfig.getTerminationConfig() != null) {
            terminationConfig.inherit(inheritedConfig.getTerminationConfig());
        }
        solverPhaseConfigList = ConfigUtils.inheritMergeableListProperty(
                solverPhaseConfigList, inheritedConfig.getSolverPhaseConfigList());
    }

}
