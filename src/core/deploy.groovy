def deployViaSSH(Map args) {
    def host = args.config.deploy?."${args.environment}"?.host
    def port = args.config.deploy?."${args.environment}"?.port ?: '22'

    if (!host) {
        error("No deploy host for '${args.service}' in '${args.environment}'")
    }

    withCredentials([sshUserPrivateKey(
        credentialsId: 'deploy-ssh-key',
        keyFileVariable: 'SSH_KEY',
        usernameVariable: 'SSH_USER'
    )]) {
        sh """
            ssh -i \$SSH_KEY -o StrictHostKeyChecking=no -p ${port} \$SSH_USER@${host} << 'EOF'
                docker pull ${args.image}:${args.tag}
                docker stop ${args.service} || true
                docker rm ${args.service} || true
                docker run -d --name ${args.service} --restart unless-stopped \
                    -p ${args.config.port}:${args.config.containerPort} \
                    ${args.image}:${args.tag}
            EOF
        """
    }

    echo "Deployed: ${args.service}:${args.tag} → ${args.environment} (${host})"
}

return this
