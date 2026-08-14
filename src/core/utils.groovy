def validateParams(Map params, List required) {
    required.each { key ->
        if (!params[key]?.trim()) { error("${key} is required") }
    }
}

def buildConfig(Map params) {
    return [
        dockerfile:    params.DOCKERFILE ?: 'Dockerfile',
        dockerContext: params.DOCKER_CONTEXT ?: '.',
        port:          params.PORT ?: '8080',
        containerPort: params.CONTAINER_PORT ?: '8080',
        deploy: [
            ci:         [host: params.DEPLOY_HOST ?: '', port: params.SSH_PORT ?: '22'],
            uat:        [host: params.DEPLOY_HOST ?: '', port: params.SSH_PORT ?: '22'],
            production: [host: params.DEPLOY_HOST ?: '', port: params.SSH_PORT ?: '22']
        ]
    ]
}

return this
