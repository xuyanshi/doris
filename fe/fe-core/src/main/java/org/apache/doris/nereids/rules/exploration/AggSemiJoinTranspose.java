// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.nereids.rules.exploration;

import org.apache.doris.nereids.rules.Rule;
import org.apache.doris.nereids.rules.RuleType;
import org.apache.doris.nereids.rules.rewrite.logical.SemiJoinAggTranspose;
import org.apache.doris.nereids.trees.plans.GroupPlan;
import org.apache.doris.nereids.trees.plans.logical.LogicalJoin;

/**
 * Pull up SemiJoin through Agg.
 */
public class AggSemiJoinTranspose extends OneExplorationRuleFactory {
    public static final AggSemiJoinTranspose INSTANCE = new AggSemiJoinTranspose();

    @Override
    public Rule build() {
        return logicalAggregate(logicalJoin())
                .when(agg -> agg.child().getJoinType().isLeftSemiOrAntiJoin())
                .then(agg -> {
                    LogicalJoin<GroupPlan, GroupPlan> join = agg.child();
                    if (!SemiJoinAggTranspose.canTranspose(agg, join)) {
                        return null;
                    }
                    return join.withChildren(agg.withChildren(join.left()), join.right());
                })
                .toRule(RuleType.LOGICAL_AGG_SEMI_JOIN_TRANSPOSE);
    }
}
