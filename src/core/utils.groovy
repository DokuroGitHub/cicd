def validateParams(params) {
    if (!params.SERVICE?.trim()) { error("SERVICE is required") }
    if (!params.REPO_URL?.trim()) { error("REPO_URL is required") }
    if (!params.BRANCH?.trim()) { error("BRANCH is required") }
}

def loadServiceConfig(String serviceName) {
    def defaults = [dockerfile: 'Dockerfile', dockerContext: '.', port: '8080', containerPort: '8080', deploy: [:]]
    def configFile = "config/services.yaml"

    if (!fileExists(configFile)) { return defaults }

    def allConfig = readYaml(file: configFile)
    def svc = allConfig.services?.find { it.name == serviceName }

    return svc ?: defaults
}

return this
