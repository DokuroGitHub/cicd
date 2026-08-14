def buildFn, deployFn, utilsFn, notifyFn

pipeline {
    agent any

    parameters {
        string(name: 'SERVICE', defaultValue: '')
        string(name: 'REPO_URL', defaultValue: '')
        string(name: 'BRANCH', defaultValue: '')
        string(name: 'ENV_REPO_URL', defaultValue: '')
        string(name: 'DEPLOY_HOST', defaultValue: '')
        string(name: 'SSH_PORT', defaultValue: '22')
        string(name: 'PORT', defaultValue: '8080')
        string(name: 'CONTAINER_PORT', defaultValue: '8080')
        string(name: 'DOCKERFILE', defaultValue: 'Dockerfile')
        string(name: 'DOCKER_CONTEXT', defaultValue: '.')
    }

    environment {
        REGISTRY_USER = 'dovt58'
        IMAGE = "${REGISTRY_USER}/${params.SERVICE}"
        TAG = "uat-${BUILD_NUMBER}"
    }

    stages {
        stage('Init') {
            steps {
                script {
                    buildFn = load 'src/core/build.groovy'
                    deployFn = load 'src/core/deploy.groovy'
                    utilsFn = load 'src/core/utils.groovy'
                    notifyFn = load 'src/integrations/notify.groovy'
                    utilsFn.validateParams(params, ['SERVICE', 'REPO_URL', 'BRANCH', 'DEPLOY_HOST'])
                }
            }
        }

        stage('Checkout') {
            steps {
                script {
                    buildFn.checkout(params.REPO_URL, params.BRANCH)
                    buildFn.checkoutEnv(params.ENV_REPO_URL)
                }
            }
        }

        stage('Test') {
            steps {
                dir("source") {
                    script {
                        if (fileExists('Makefile')) {
                            sh "make test"
                        }
                    }
                }
            }
        }

        stage('Build & Push') {
            steps {
                script {
                    def config = utilsFn.buildConfig(params)
                    buildFn.buildAndPush(
                        image: env.IMAGE,
                        tag: env.TAG,
                        dockerfile: config.dockerfile,
                        context: config.dockerContext
                    )
                }
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def config = utilsFn.buildConfig(params)
                    deployFn.deployViaSSH(
                        service: params.SERVICE,
                        image: env.IMAGE,
                        tag: env.TAG,
                        environment: 'uat',
                        config: config
                    )
                }
            }
        }
    }

    post {
        success { script { notifyFn.success(service: params.SERVICE, tag: env.TAG, environment: 'uat') } }
        failure { script { notifyFn.failure(service: params.SERVICE, environment: 'uat') } }
    }
}
