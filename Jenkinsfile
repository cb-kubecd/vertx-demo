pipeline {
  agent any
  environment {
    ORG = 'cb-kubecd'
    APP_NAME = 'vertx-demo'
    CHARTMUSEUM_CREDS = credentials('jenkins-x-chartmuseum')
    SONARCLOUD_CREDS = credentials('sonarcloud')
    MAVEN_OPTS = '-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn'
  }
  stages {
    stage('CI Build and push snapshot') {
      when {
        branch 'PR-*'
      }
      environment {
        PREVIEW_VERSION = "0.0.0-SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER"
        PREVIEW_NAMESPACE = "$APP_NAME-$BRANCH_NAME".toLowerCase()
        HELM_RELEASE = "$PREVIEW_NAMESPACE".toLowerCase()
      }
      steps {
        sh 'git fetch --unshallow && git branch -m $BRANCH_NAME'
        sh "mvn versions:set -DnewVersion=$PREVIEW_VERSION"
        // TODO Prow does not report the branch used in the fork, and it is not clear sonar.pullrequest.branch matters anyway
        sh 'mvn -Dsonar.pullrequest.branch=$BRANCH_NAME -Dsonar.pullrequest.key=$PULL_NUMBER -Dsonar.pullrequest.base=$PULL_BASE_REF -Dsonar.pullrequest.provider=github -Dsonar.pullrequest.github.repository=$REPO_OWNER/$REPO_NAME -Dmaven.test.failure.ignore install'
        sh 'jx step stash -c junit -p "target/surefire-reports/TEST-*.xml" --basedir target/surefire-reports'
        sh "export VERSION=$PREVIEW_VERSION && skaffold build -f skaffold.yaml"
        sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:$PREVIEW_VERSION"
        dir('charts/preview') {
          sh "make preview"
          sh "jx preview --app $APP_NAME --dir ../.."
        }
      }
    }
    stage('Build Release') {
      when {
        branch 'master'
      }
      steps {
        sh 'git fetch --unshallow && git branch -m $BRANCH_NAME'
        git 'https://github.com/cb-kubecd/vertx-demo.git'

        // so we can retrieve the version in later steps
        sh "echo \$(jx-release-version) > VERSION"
        sh "mvn versions:set -DnewVersion=\$(cat VERSION)"
        sh "jx step tag --version \$(cat VERSION)"
        sh 'mvn -Dsonar.branch.name=master clean deploy'
        sh 'jx step stash -c junit -p "target/surefire-reports/TEST-*.xml" --basedir target/surefire-reports'
        sh "export VERSION=`cat VERSION` && skaffold build -f skaffold.yaml"
        sh "jx step post build --image $DOCKER_REGISTRY/$ORG/$APP_NAME:\$(cat VERSION)"
      }
    }
    stage('Promote to Environments') {
      when {
        branch 'master'
      }
      steps {
        dir('charts/vertx-demo') {
          sh "jx step changelog --version v\$(cat ../../VERSION)"

          // release the helm chart
          sh "jx step helm release"

          // promote through all 'Auto' promotion Environments
          sh "jx promote -b --all-auto --timeout 1h --version \$(cat ../../VERSION)"
        }
      }
    }
  }
}
