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

package com.cloudera.livy.spark.interactive

import java.util.concurrent.TimeUnit

import scala.concurrent.{Await, ExecutionContext, ExecutionContextExecutor, Future}
import scala.concurrent.duration._

import com.cloudera.livy.sessions.SessionState
import com.cloudera.livy.sessions.interactive.InteractiveSession
import com.cloudera.livy.spark.SparkProcess
import com.cloudera.livy.yarn.LivyYarnClient

private class InteractiveSessionYarn(
    id: Int,
    owner: String,
    client: LivyYarnClient,
    process: SparkProcess,
    request: CreateInteractiveRequest)
  extends InteractiveWebSession(id, owner, process, request) {

  private val job = Future {
    client.getJobFromProcess(process)
  }

  job.onFailure { case _ =>
    _state = SessionState.Error()
  }

  override def logLines(): IndexedSeq[String] = process.inputLines

  override def stop(): Future[Unit] = {
    super.stop().andThen {
      case _ =>
        try {
          val job_ = Await.result(job, Duration(10, TimeUnit.SECONDS))
          job_.waitForFinish(10000).getOrElse {
            job_.stop()
          }
        } catch {
          case e: Throwable =>
            _state = SessionState.Error()
            throw e
        }
    }
  }
}
