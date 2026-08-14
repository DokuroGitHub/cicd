def getCredentialsId(String repoUrl) {
    if (repoUrl.contains('github.com')) { return 'github-credentials' }
    if (repoUrl.contains('fptshop.com.vn')) { return 'gitlab-credentials' }
    return 'github-credentials'
}

def checkout(String repoUrl, String branch) {
    dir("source") {
        git branch: branch, url: repoUrl, credentialsId: getCredentialsId(repoUrl)
    }
}

def checkoutEnv(String envRepoUrl) {
    if (!envRepoUrl?.trim()) { return }
    dir("env-source") {
        git branch: 'main', url: envRepoUrl, credentialsId: getCredentialsId(envRepoUrl)
    }
}

def buildAndPush(Map args) {
    dir("source") {
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
