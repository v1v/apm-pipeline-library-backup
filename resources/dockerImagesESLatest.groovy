#!/usr/bin/env groovy

@Library('apm@current') _

pipeline {
  agent { label 'docker' }
  environment {
    BASE_DIR="src"
    NOTIFY_TO = credentials('notify-to')
    JOB_GCS_BUCKET = credentials('gcs-bucket')
    PIPELINE_LOG_LEVEL='INFO'
  }
  options {
    timeout(time: 1, unit: 'HOURS')
    buildDiscarder(logRotator(numToKeepStr: '20', artifactNumToKeepStr: '20', daysToKeepStr: '30'))
    timestamps()
    ansiColor('xterm')
    disableResume()
    durabilityHint('PERFORMANCE_OPTIMIZED')
  }
  triggers {
    issueCommentTrigger('.*(?:jenkins\\W+)?run\\W+(?:the\\W+)?tests(?:\\W+please)?.*')
  }
  parameters {
    string(name: 'registry', defaultValue: "docker.elastic.co", description: "")
    string(name: 'tag_prefix', defaultValue: "observability-ci", description: "")
    string(name: 'version', defaultValue: "daily", description: "")
    string(name: 'elastic_stack', defaultValue: "8.0.0-SNAPSHOT", description: "")
    string(name: 'secret', defaultValue: "secret/apm-team/ci/elastic-observability-docker-elastic-co", description: "")
  }
  stages {
    stage('Get Docker images'){
      steps {
        script {
          if(params.secret != null && "${params.secret}" != ""){
             dockerLogin(secret: "${params.secret}", registry: "${params.registry}")
          }
          sh(label: 'Get Docker images', script: "./resources/scripts/getDockerImages.sh ${params.elastic_stack}")
        }
      }
    }
    stage('Push Docker images'){
      steps {
        script {
          if(params.secret != null && "${params.secret}" != ""){
             dockerLogin(secret: "${params.secret}", registry: "${params.registry}")
          }
          sh(label: 'Push Docker images', script: "./resources/scripts/pushDockerImages.sh ${params.elastic_stack} ${params.tag_prefix} ${params.version} ${params.registry}")
        }
      }
    }
  }
  post {
    success {
      echoColor(text: '[SUCCESS]', colorfg: 'green', colorbg: 'default')
    }
    aborted {
      echoColor(text: '[ABORTED]', colorfg: 'magenta', colorbg: 'default')
    }
    failure {
      echoColor(text: '[FAILURE]', colorfg: 'red', colorbg: 'default')
      step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: "${NOTIFY_TO}", sendToIndividuals: false])
    }
    unstable {
      echoColor(text: '[UNSTABLE]', colorfg: 'yellow', colorbg: 'default')
    }
  }
}
