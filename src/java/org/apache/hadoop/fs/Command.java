/**
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
package org.apache.hadoop.fs;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;

/** A base class for running a unix command like du or df*/
abstract public class Command {
  /** Run a command */
  protected void run() throws IOException { 
    Process process;
    process = Runtime.getRuntime().exec(getExecString());

    try {
      if (process.waitFor() != 0) {
        throw new IOException
          (new BufferedReader(new InputStreamReader(process.getErrorStream()))
           .readLine());
      }
      parseExecResult(new BufferedReader(
          new InputStreamReader(process.getInputStream())));
    } catch (InterruptedException e) {
      throw new IOException(e.toString());
    } finally {
      process.destroy();
    }
  }

  /** return an array comtaining the command name & its parameters */ 
  protected abstract String[] getExecString();
  
  /** Parse the execution result */
  protected abstract void parseExecResult(BufferedReader lines)
  throws IOException;

  /// A simple implementation of Command
  private static class SimpleCommandExecutor extends Command {
    
    private String[] command;
    private StringBuffer reply;
    
    SimpleCommandExecutor(String[] execString) {
      command = execString;
    }

    @Override
    protected String[] getExecString() {
      return command;
    }

    @Override
    protected void parseExecResult(BufferedReader lines) throws IOException {
      reply = new StringBuffer();
      char[] buf = new char[512];
      int nRead;
      while ( (nRead = lines.read(buf, 0, buf.length)) > 0 ) {
        reply.append(buf, 0, nRead);
      }
    }
    
    String getReply() {
      return (reply == null) ? "" : reply.toString();
    }
  }
  
  /** 
   * Static method to execute a command. Covers most of the simple cases 
   * without requiring the user to implement Command interface.
   */
  public static String execCommand(String[] cmd) throws IOException {
    SimpleCommandExecutor exec = new SimpleCommandExecutor(cmd);
    exec.run();
    return exec.getReply();
  }
}