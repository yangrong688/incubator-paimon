---
title: "CDC Ingestion"
weight: 7
type: docs
aliases:
- /how-to/cdc-ingestion.html
---
<!--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

# CDC Ingestion

Paimon supports synchronizing changes from different databases using change data capture (CDC). This feature requires Flink and its [CDC connectors](https://ververica.github.io/flink-cdc-connectors/).

## MySQL

### Synchronizing Tables

By using [MySqlSyncTableAction](/api/java/org/apache/paimon/flink/action/cdc/mysql/MySqlSyncTableAction) in a Flink DataStream job or directly through `flink run`, users can synchronize one or multiple tables from MySQL into one Paimon table.

To use this feature through `flink run`, run the following shell command.

```bash
<FLINK_HOME>/bin/flink run \
    -c org.apache.paimon.flink.action.FlinkActions \
    /path/to/paimon-flink-**-{{< version >}}.jar \
    mysql-sync-table
    --warehouse <warehouse-path> \
    --database <database-name> \
    --table <table-name> \
    [--partition-keys <partition-keys>] \
    [--primary-keys <primary-keys>] \
    [--mysql-conf <mysql-cdc-source-conf> [--mysql-conf <mysql-cdc-source-conf> ...]] \
    [--paimon-conf <paimon-table-sink-conf> [--paimon-conf <paimon-table-sink-conf> ...]]
```

* `--warehouse` is the path to Paimon warehouse.
* `--database` is the database name in Paimon catalog.
* `--table` is the Paimon table name.
* `--partition-keys` are the partition keys for Paimon table. If there are multiple partition keys, connect them with comma, for example `dt,hh,mm`.
* `--primary-keys` are the primary keys for Paimon table. If there are multiple primary keys, connect them with comma, for example `buyer_id,seller_id`.
* `--mysql-conf` is the configuration for Flink CDC MySQL table sources. Each configuration should be specified in the format `key=value`. `hostname`, `username`, `password`, `database-name` and `table-name` are required configurations, others are optional. See its [document](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/mysql-cdc.html#connector-options) for a complete list of configurations.
* `--paimon-conf` is the configuration for Paimon table sink. Each configuration should be specified in the format `key=value`. See [here]({{< ref "maintenance/configurations" >}}) for a complete list of configurations.

If the Paimon table you specify does not exist, this action will will automatically create the table. Its schema will be derived from all specified MySQL tables. If the Paimon table already exists, its schema will be compared against the schema of all specified MySQL tables.

This action supports a limited number of schema changes. Unsupported schema changes will be ignored. Currently supported schema changes includes:

* Adding columns.

* Altering column types. More specifically,

  * altering from a string type (char, varchar, text) to another string type with longer length,
  * altering from a binary type (binary, varbinary, blob) to another binary type with longer length,
  * altering from an integer type (tinyint, smallint, int, bigint) to another integer type with wider range,
  * altering from a floating-point type (float, double) to another floating-point type with wider range,
  
  are supported. Other type changes will cause exceptions.

Example

```bash
<FLINK_HOME>/bin/flink run \
    -c org.apache.paimon.flink.action.FlinkActions \
    /path/to/paimon-flink-**-{{< version >}}.jar \
    mysql-sync-table \
    --warehouse hdfs:///path/to/warehouse \
    --database test_db \
    --table test_table \
    --partition-keys pt \
    --primary-keys pt,uid \
    --mysql-conf hostname=127.0.0.1 \
    --mysql-conf username=root \
    --mysql-conf password=123456 \
    --mysql-conf database-name=source_db \
    --mysql-conf table-name='source_table_.*' \
    --paimon-conf bucket=4 \
    --paimon-conf changelog-producer=input \
    --paimon-conf sink.parallelism=4
```
