def checkout(String repoUrl, String branch) {
    dir("source") {
        git branch: branch, url: repoUrl, credentialsId: 'git-credentials'
    }
}

def checkoutConfig(String configRepoUrl) {
    if (!configRepoUrl?.trim()) { return }
    dir("config-source") {
        git branch: 'main', url: configRepoUrl, credentialsId: 'git-credentials'
    }
}

def buildAndPush(Map args) {
    def environment = args.environment ?: 'ci'

    dir("source") {
        if (fileExists("../config-source/env.${environment}")) {
            sh "cp ../config-source/env.${environment} .env"
        }
        withCredentials([usernamePassword(
            credentialsId: 'dockerhub-credentials',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASS'
        )]) {
            sh """
                echo \$DOCKER_PASS | docker login -u \$DOCKER_USER --password-stdin
                docker build -t ${args.image}:${args.tag} -f ${args.dockerfile ?: 'Dockerfile'} ${args.context ?: '.'}
                docker push ${args.image}:${args.tag}
                docker logout
            """
        }
    }
    echo "Pushed: ${args.image}:${args.tag}"
}

return this
