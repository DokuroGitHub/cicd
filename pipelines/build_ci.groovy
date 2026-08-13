def config, buildFn, utilsFn, notifyFn

pipeline {
    agent any

    parameters {
        string(name: 'BRANCH', defaultValue: 'main')
        string(name: 'SERVICE', defaultValue: '')
        string(name: 'REPO_URL', defaultValue: '')
        string(name: 'CONFIG_REPO_URL', defaultValue: '')
    }

    environment {
        REGISTRY_USER = 'dovt58'
        IMAGE = "${REGISTRY_USER}/${params.SERVICE}"
        TAG = "ci-${BUILD_NUMBER}"
    }

    stages {
        stage('Init') {
            steps {
                script {
                    buildFn = load 'src/core/build.groovy'
                    utilsFn = load 'src/core/utils.groovy'
                    notifyFn = load 'src/integrations/notify.groovy'
                    utilsFn.validateParams(params)
                    config = utilsFn.loadServiceConfig(params.SERVICE)
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    buildFn.checkout(params.REPO_URL, params.BRANCH)
                    buildFn.checkoutConfig(params.CONFIG_REPO_URL)
                }
            }
        }

        stage('Build & Push') {
            steps {
                script {
                    buildFn.buildAndPush(
                        image: env.IMAGE,
                        tag: env.TAG,
                        environment: 'ci',
                        context: config.dockerContext ?: '.',
                        dockerfile: config.dockerfile ?: 'Dockerfile'
                    )
                }
            }
        }
    }

    post {
        success { script { notifyFn.success(service: params.SERVICE, tag: env.TAG, environment: 'ci') } }
        failure { script { notifyFn.failure(service: params.SERVICE, environment: 'ci') } }
    }
}
