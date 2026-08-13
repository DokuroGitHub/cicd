# CICD

Jenkins pipeline scripts loaded via SCM.

## Structure

```
pipelines/
├── build_ci.groovy          CI: test + build + push
├── build_uat.groovy         UAT: test + build + push (release branch)
└── deploy_production.groovy Production: deploy UAT image (no rebuild)
src/core/
├── build.groovy             Checkout, build, push
├── deploy.groovy            SSH deploy
└── utils.groovy             Validation, config
src/integrations/
└── notify.groovy            Notifications
config/
└── services.yaml            Service definitions (optional)
```

## Flow

```
CI (branch: ci/develop)  → build + push image ci-{N}
UAT (branch: release/*)  → build + push image uat-{N}
Production               → deploy image uat-{N} (no rebuild, same image tested in UAT)
```

## Jenkins Jobs

| Job | Script Path | Parameters |
|-----|-------------|------------|
| build-ci | `pipelines/build_ci.groovy` | BRANCH, SERVICE, REPO_URL, CONFIG_REPO_URL |
| build-uat | `pipelines/build_uat.groovy` | BRANCH, SERVICE, REPO_URL, CONFIG_REPO_URL |
| deploy-production | `pipelines/deploy_production.groovy` | SERVICE, IMAGE_TAG |

## Credentials

- `github-credentials` — GitHub access
- `gitlab-credentials` — GitLab access
- `dockerhub-credentials` — Docker Hub (dovt58)
- `deploy-ssh-key` — SSH key for deploy server
