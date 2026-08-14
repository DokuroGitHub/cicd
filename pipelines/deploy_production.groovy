def buildFn, deployFn, utilsFn, notifyFn

pipeline {
    agent any

    parameters {
        string(name: 'SERVICE', defaultValue: '')
        string(name: 'IMAGE_TAG', defaultValue: '')
        string(name: 'ENV_REPO_URL', defaultValue: '')
        string(name: 'DEPLOY_HOST', defaultValue: '')
        string(name: 'SSH_PORT', defaultValue: '22')
        string(name: 'PORT', defaultValue: '8080')
        string(name: 'CONTAINER_PORT', defaultValue: '8080')
    }

    environment {
        REGISTRY_USER = 'dovt58'
        IMAGE = "${REGISTRY_USER}/${params.SERVICE}"
    }

    stages {
        stage('Init') {
            steps {
                script {
                    buildFn = load 'src/core/build.groovy'
                    deployFn = load 'src/core/deploy.groovy'
                    utilsFn = load 'src/core/utils.groovy'
                    notifyFn = load 'src/integrations/notify.groovy'
                    utilsFn.validateParams(params, ['SERVICE', 'IMAGE_TAG', 'DEPLOY_HOST'])
                }
            }
        }

        stage('Checkout Env') {
            steps {
                script {
                    buildFn.checkoutEnv(params.ENV_REPO_URL)
                }
            }
        }

        stage('Approval') {
            steps {
                input message: "Deploy ${env.IMAGE}:${params.IMAGE_TAG} to PRODUCTION?", ok: 'Deploy'
            }
        }

        stage('Deploy') {
            steps {
                script {
                    def config = utilsFn.buildConfig(params)
                    deployFn.deployViaSSH(
                        service: params.SERVICE,
                        image: env.IMAGE,
                        tag: params.IMAGE_TAG,
                        environment: 'production',
                        config: config
                    )
                }
            }
        }
    }

    post {
        success { script { notifyFn.success(service: params.SERVICE, tag: params.IMAGE_TAG, environment: 'production') } }
        failure { script { notifyFn.failure(service: params.SERVICE, environment: 'production') } }
    }
}
