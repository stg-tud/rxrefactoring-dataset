/*
 * Copyright 2014 JBoss Inc
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

package org.optaplanner.core.impl.exhaustivesearch.node.bounder;

import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.impl.score.director.ScoreDirector;
import org.optaplanner.core.impl.solution.Solution;

public interface ScoreBounder {

    /**
     * In OR terms, this is called the lower bound if they minimize, and upper bound if they maximize.
     * Because we always maximize the {@link Score}, calling it lower bound would be a contradiction.
     * @param scoreDirector never null, use {@link ScoreDirector#getWorkingSolution()} to get the working {@link Solution}
     * @param score never null, the {@link Score} of the working {@link Solution}
     * @param uninitializedVariableCount >= 0, the number of uninitialized variables in the working {@link Solution}.
     * If 0, this method should return the score parameter.
     * @return never null, never worse than the best possible {@link Score} we can get
     * by initializing the uninitialized variables of the working {@link Solution}.
     */
    Score calculateOptimisticBound(ScoreDirector scoreDirector, Score score, int uninitializedVariableCount);

    /**
     * In OR terms, this is called the upper bound if they minimize, and lower bound if they maximize.
     * Because we always maximize the {@link Score}, calling it upper bound would be a contradiction.
     * @param scoreDirector never null, use {@link ScoreDirector#getWorkingSolution()} to get the working {@link Solution}
     * @param score never null, the {@link Score} of the working {@link Solution}
     * @param uninitializedVariableCount >= 0, the number of uninitialized variables in the working {@link Solution}.
     * If 0, this method should return the score parameter.
     * @return never null, never better than the worst possible {@link Score} we can get
     * by initializing the uninitialized variables of the working {@link Solution}.
     */
    Score calculatePessimisticBound(ScoreDirector scoreDirector, Score score, int uninitializedVariableCount);

}
