pipeline {
    agent any
    parameters {
        choice(name: 'CREATE_OR_DESTROY', choices: ['create', 'destroy'], description: 'Select whether you want to create or destroy the service')
    }
    environment {
        SHORT_COMMIT_ID = "${GIT_COMMIT}".substring(0, 7)
        SERVICE_NAME = "fraud-checker-service"
        ECR_REPOSITORY_NAME = "bootcamp-2021-ecr/${SERVICE_NAME}"
        ECR_REPOSITORY_FULL_NAME = "038062473746.dkr.ecr.us-east-1.amazonaws.com/bootcamp-2021-ecr/${SERVICE_NAME}"
        ECR_IMAGE_ID = "${REPOSITORY_NAME}:${SHORT_COMMIT_ID}"
        AWS_REGION = "us-east-1"
    }

    stages {
        stage('Build & Test') {
            steps {
                sh './gradlew clean build'
            }
        }
        stage('Build docker image') {
            steps {
                sh '''
                    docker build -t ${SERVICE_NAME}:${SHORT_COMMIT_ID} .
                '''
            }
        }
        stage('Create ECR repo if doesnt exists') {
            steps {
                sh '''
                    aws ecr create-repository \\
                        --repository-name ${ECR_REPOSITORY_NAME} \\
                        --image-scanning-configuration scanOnPush=true --region ${AWS_REGION} || true
                '''
            }
        }
        stage('Push docker image to ECR') {
            steps {
                sh '''
                    eval $(aws ecr get-login --no-include-email --region us-east-1)
                    docker tag ${SERVICE_NAME}:${SHORT_COMMIT_ID} ${ECR_REPOSITORY_FULL_NAME}:${SHORT_COMMIT_ID}
                    docker tag ${SERVICE_NAME}:${SHORT_COMMIT_ID} ${ECR_REPOSITORY_FULL_NAME}:latest
                    docker push ${ECR_REPOSITORY_FULL_NAME}:${SHORT_COMMIT_ID}
                    docker push ${ECR_REPOSITORY_FULL_NAME}:latest
                '''
            }
        }
        stage('use-terraform-version') {
            steps {
                dir('infrastructure') {
                    script {
                        int tfenvUseExitCode = sh(
                            returnStatus: true,
                            label: 'tfenv-use',
                            script: '''
                                REQUIRED_TERRAFORM_VERSION=$(grep required_version *.tf | cut -d '\"' -f 2)
                                echo "Required terraform version is: ${REQUIRED_TERRAFORM_VERSION}";
                                tfenv install ${REQUIRED_TERRAFORM_VERSION};
                                tfenv use ${REQUIRED_TERRAFORM_VERSION};
                            '''
                        )
                        if (tfenvUseExitCode) {
                            error("Attempt to read & provision specific terraform version failed")
                        }
                    }
                }
            }
        }
        stage('terraform-plan-for-create') {
            when {
                expression { params.CREATE_OR_DESTROY == 'create' }
            }
            environment {
                terraformDirectory = "infrastructure"
            }
            steps {
                passImageIdVariableToTerraform(ECR_IMAGE_ID, terraformDirectory)
                dir(terraformDirectory) {
                    script {
                        ansiColor('xterm') {
                            env.tfPlanExitCode = sh(
                                returnStatus: true,
                                label: 'tf-plan',
                                script: '''
                                    export TF_IN_AUTOMATION=true
                                    export TF_INPUT=0
                                    terraform init
                                    terraform plan -detailed-exitcode -out tfplan 
                                '''
                            )
                        }
                        if (env.tfPlanExitCode == '0') {
                            echo "No changes. Infrastructure is up-to-date"
                        } else if (env.tfPlanExitCode == '1') {
                            error("Terraform plan failed with an error")
                        }
                    }
                    stash includes: "tfplan", name: "terraform-plan"
                    stash includes: ".terraform/**", name: "terraform-modules"
                }
            }
        }
        stage('terraform-plan-for-destroy') {
            when {
                expression { params.CREATE_OR_DESTROY == 'destroy' }
            }
            environment {
                terraformDirectory = "infrastructure"
            }
            steps {
                dir(terraformDirectory) {
                    script {
                        ansiColor('xterm') {
                            env.tfPlanExitCode = sh(
                                returnStatus: true,
                                label: 'tf-plan',
                                script: '''
                                    export TF_IN_AUTOMATION=true
                                    export TF_INPUT=0
                                    terraform init
                                    terraform plan -destroy -detailed-exitcode -out tfplan 
                                '''
                            )
                        }
                        if (env.tfPlanExitCode == '0') {
                            echo "No changes. Infrastructure is up-to-date"
                        } else if (env.tfPlanExitCode == '1') {
                            error("Terraform plan failed with an error")
                        }
                    }
                    stash includes: "tfplan", name: "terraform-plan"
                    stash includes: ".terraform/**", name: "terraform-modules"
                }
            }
        }
        stage("review-and-approve") {
            when {
                allOf {
                    expression { env.tfPlanExitCode == '2' }
                    expression { env.GIT_BRANCH == 'origin/master' }
                }
            }
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    input "Have you reviewed the terraform plan for changes? Do you want to proceed with it?"
                }
            }
        }
        stage('apply') {
            when {
                allOf {
                    expression { env.tfPlanExitCode == '2' }
                    expression { env.GIT_BRANCH == 'origin/master' }
                }
            }
            environment {
                terraformDirectory = "infrastructure"
            }
            steps {
                dir(terraformDirectory) {
                    unstash 'terraform-plan'
                    unstash 'terraform-modules'
                    script {
                        ansiColor('xterm') {
                            sh(
                                label: 'tf-apply',
                                script: '''
                                    export TF_IN_AUTOMATION=true
                                    export TF_INPUT=0
                                    export TF_LOG=DEBUG 
                                    export TF_LOG_PATH=terraform_apply_logs
                                    terraform apply tfplan 
                                '''
                            )
                        }
                    }
                    archiveArtifacts 'terraform_apply_logs'
                }
            }
        }
    }
}

def passImageIdVariableToTerraform(String imageId, String terraformDirectory) {
    // pass image id information for terraform
    sh """
        echo container_image=\\"${imageId}\\" > ${WORKSPACE}/${terraformDirectory}/terraform.tfvars
    """
}