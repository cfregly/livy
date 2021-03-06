/*
 * Licensed to Cloudera, Inc. under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  Cloudera, Inc. licenses this file
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

package com.cloudera.livy.spark.batch

import com.cloudera.livy.sessions.batch.BatchSession
import com.cloudera.livy.spark.{SparkProcess, SparkProcessBuilder, SparkProcessBuilderFactory}
import com.cloudera.livy.yarn.LivyYarnClient

class BatchSessionYarnFactory(client: LivyYarnClient, factory: SparkProcessBuilderFactory)
  extends BatchSessionFactory(factory) {

  protected override def create(id: Int, owner: String, process: SparkProcess): BatchSession =
    BatchSessionYarn(client, id, owner, process)

  override def sparkBuilder(request: CreateBatchRequest): SparkProcessBuilder = {
    val builder = super.sparkBuilder(request)
    builder.master("yarn-cluster")
    builder
  }
}
