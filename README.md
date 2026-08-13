# CICD

Jenkins pipeline scripts loaded via SCM.

## Structure

```
pipelines/          → Jenkins loads these (Script Path)
src/core/           → Shared build, deploy, utils logic
src/integrations/   → Notifications
config/             → Service definitions
```

## Jenkins Job Setup

1. New Pipeline job → Pipeline script from SCM
2. Repo: `git@github.com:DokuroGitHub/cicd.git`
3. Script Path: `pipelines/build_ci.groovy` (or `build_uat`, `build_production`)
4. Parameters: `BRANCH`, `SERVICE`, `REPO_URL`

## Credentials Required

- `git-credentials` — Git clone access
- `dockerhub-credentials` — Docker Hub (dovt58)
- `deploy-ssh-key` — SSH key for deploy server

## Pipeline Flow

```
CI:         Checkout → Build → Push to Docker Hub
UAT:        Checkout → Build → Push → Deploy via SSH
Production: Checkout → Build → Push → Approval → Deploy via SSH
```
