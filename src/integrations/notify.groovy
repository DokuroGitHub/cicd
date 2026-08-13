def success(Map args) {
    echo "SUCCESS: ${args.service}:${args.tag} → ${args.environment}"
}

def failure(Map args) {
    echo "FAILED: ${args.service} → ${args.environment} (Build #${BUILD_NUMBER})"
}

return this
