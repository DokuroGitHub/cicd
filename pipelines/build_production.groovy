def config, buildFn, deployFn, utilsFn, notifyFn

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
        TAG = "prod-${BUILD_NUMBER}"
        DEPLOY_ENV = 'production'
    }

    stages {
        stage('Init') {
            steps {
                script {
                    buildFn = load 'src/core/build.groovy'
                    deployFn = load 'src/core/deploy.groovy'
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
                        environment: env.DEPLOY_ENV,
                        context: config.dockerContext ?: '.',
                        dockerfile: config.dockerfile ?: 'Dockerfile'
                    )
                }
            }
        }

        stage('Approval') {
            steps {
                input message: "Deploy ${params.SERVICE}:${env.TAG} to PRODUCTION?", ok: 'Deploy'
            }
        }

        stage('Deploy') {
            steps {
                script {
                    deployFn.deployViaSSH(
                        service: params.SERVICE,
                        image: env.IMAGE,
                        tag: env.TAG,
                        environment: env.DEPLOY_ENV,
                        config: config
                    )
                }
            }
        }
    }

    post {
        success { script { notifyFn.success(service: params.SERVICE, tag: env.TAG, environment: env.DEPLOY_ENV) } }
        failure { script { notifyFn.failure(service: params.SERVICE, environment: env.DEPLOY_ENV) } }
    }
}
