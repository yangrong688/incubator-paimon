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

package org.apache.paimon.flink.source;

import org.apache.paimon.table.source.ReadBuilder;
import org.apache.paimon.table.source.TableRead;

import org.apache.flink.api.connector.source.Source;
import org.apache.flink.api.connector.source.SourceReader;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.api.connector.source.SplitEnumerator;
import org.apache.flink.api.connector.source.SplitEnumeratorContext;
import org.apache.flink.table.data.RowData;

import javax.annotation.Nullable;

/** A Flink {@link Source} for paimon. */
public abstract class FlinkSource
        implements Source<RowData, FileStoreSourceSplit, PendingSplitsCheckpoint> {

    private static final long serialVersionUID = 1L;

    protected final ReadBuilder readBuilder;

    @Nullable protected final Long limit;

    public FlinkSource(ReadBuilder readBuilder, @Nullable Long limit) {
        this.readBuilder = readBuilder;
        this.limit = limit;
    }

    @Override
    public SourceReader<RowData, FileStoreSourceSplit> createReader(SourceReaderContext context) {
        return createSourceReader(context, readBuilder.newRead(), limit);
    }

    public FileStoreSourceReader<?> createSourceReader(
            SourceReaderContext context, TableRead read, @Nullable Long limit) {
        return new FileStoreSourceReader<>(RecordsFunction.forSingle(), context, read, limit);
    }

    @Override
    public SplitEnumerator<FileStoreSourceSplit, PendingSplitsCheckpoint> createEnumerator(
            SplitEnumeratorContext<FileStoreSourceSplit> context) throws Exception {
        return restoreEnumerator(context, null);
    }

    @Override
    public FileStoreSourceSplitSerializer getSplitSerializer() {
        return new FileStoreSourceSplitSerializer();
    }

    @Override
    public PendingSplitsCheckpointSerializer getEnumeratorCheckpointSerializer() {
        return new PendingSplitsCheckpointSerializer(getSplitSerializer());
    }
}
