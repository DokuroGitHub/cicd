def buildFn, deployFn, utilsFn, notifyFn

pipeline {
    agent any

    parameters {
        string(name: 'SERVICE', defaultValue: '')
        string(name: 'IMAGE_TAG', defaultValue: '')
        string(name: 'CONFIG_REPO_URL', defaultValue: '')
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
                    if (!params.SERVICE?.trim()) { error("SERVICE is required") }
                    if (!params.IMAGE_TAG?.trim()) { error("IMAGE_TAG is required") }
                }
            }
        }

        stage('Checkout Config') {
            steps {
                script {
                    buildFn.checkoutConfig(params.CONFIG_REPO_URL)
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
                    def config = utilsFn.loadServiceConfig(params.SERVICE)
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
