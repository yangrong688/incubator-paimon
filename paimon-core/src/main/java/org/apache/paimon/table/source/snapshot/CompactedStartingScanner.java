/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.paimon.table.source.snapshot;

import org.apache.paimon.CoreOptions;
import org.apache.paimon.operation.ScanKind;
import org.apache.paimon.utils.SnapshotManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/** {@link StartingScanner} for the {@link CoreOptions.StartupMode#COMPACTED_FULL} startup mode. */
public class CompactedStartingScanner implements StartingScanner {

    private static final Logger LOG = LoggerFactory.getLogger(CompactedStartingScanner.class);

    @Override
    @Nullable
    public Result scan(SnapshotManager snapshotManager, SnapshotSplitReader snapshotSplitReader) {
        Long startingSnapshotId = pick(snapshotManager);
        if (startingSnapshotId == null) {
            startingSnapshotId = snapshotManager.latestSnapshotId();
            if (startingSnapshotId == null) {
                LOG.debug("There is currently no snapshot. Wait for the snapshot generation.");
                return null;
            } else {
                LOG.debug(
                        "No compact snapshot found, reading from the latest snapshot {}.",
                        startingSnapshotId);
            }
        }

        return new Result(
                startingSnapshotId,
                snapshotSplitReader
                        .withKind(ScanKind.ALL)
                        .withSnapshot(startingSnapshotId)
                        .splits());
    }

    @Nullable
    protected Long pick(SnapshotManager snapshotManager) {
        return snapshotManager.latestCompactedSnapshotId();
    }
}
