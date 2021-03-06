// Licensed to Elasticsearch B.V. under one or more contributor
// license agreements. See the NOTICE file distributed with
// this work for additional information regarding copyright
// ownership. Elasticsearch B.V. licenses this file to you under
// the Apache License, Version 2.0 (the "License"); you may
// not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

/**
  Creates some environment variables to identified the repo and the change type (change, commit, PR, ...)

  githubEnv()
*/
def call(){
  if(!isUnix()){
    error('githubEnv: windows is not supported yet.')
  }

  setGitRepoEnvironment()
  setGitSha()
  getBaseCommit()
  setBuildCause()
}

def getBaseCommit(){
  def baseCommit = getGitCommitSha()

  // When a PR then gets its real commit from the ref spec
  if(isPR()) {
    baseCommit = sh(label: 'Get previous commit', script: "git rev-parse origin/pr/${env.CHANGE_ID}", returnStdout: true)?.trim()
  }

  // GIT_COMMIT is not set on regular pipelines
  if(env?.GIT_COMMIT == null){
    env.GIT_COMMIT = baseCommit
  }

  env.GIT_BASE_COMMIT = baseCommit
  log(level: 'DEBUG', text: "GIT_BASE_COMMIT = ${env.GIT_BASE_COMMIT}")
  return baseCommit
}

def setBuildCause() {
  if (env.CHANGE_TARGET){
    env.GIT_BUILD_CAUSE = "pr"
  } else {
    env.GIT_BUILD_CAUSE = sh (
      label: 'Get latest commits SHA',
      script: 'git rev-list HEAD --parents -1', // will have 2 shas if commit, 3 or more if merge
      returnStdout: true
    )?.split(" ").length > 2 ? "merge" : "commit"
  }
  log(level: 'INFO', text: "githubEnv: Found Git Build Cause: ${env.GIT_BUILD_CAUSE}")
}

def setGitRepoEnvironment() {
  if(!env?.GIT_URL){
    env.GIT_URL = getGitRepoURL()
  }

  def tmpUrl = env.GIT_URL

  if (env.GIT_URL.startsWith("git")){
    tmpUrl = tmpUrl - "git@github.com:"
  } else {
    tmpUrl = tmpUrl - "https://github.com/" - "http://github.com/"
  }

  def parts = tmpUrl.split("/")
  env.ORG_NAME = parts[0]
  env.REPO_NAME = parts[1] - ".git"
}

def setGitSha() {
  if(env?.GIT_SHA?.trim()){
    log(level: 'INFO', text: "githubEnv: GIT_SHA was already set. skipped.")
  } else {
    env.GIT_SHA = getGitCommitSha()
  }
}
