# ECS Service

Provision fraud-checker-service as ECS service on ECS cluster. The provision of ECS service is done using Jenkins job and we discourage 
for any provisioning using local machines.

### Steps to provision

- Run the [Jenkins job](TBD_Jenkins_URL) named `fraud-checker-service` with parameter `create`
- You will be prompted to approve the plan, approve it, if changes seems to be inline with expectations. 

### Steps to de-provision

- Run the [Jenkins job](TBD_Jenkins_URL) named `fraud-checker-service` with parameter `destroy`
- You will be prompted to approve the plan, approve it, if changes seems to be inline with expectations.

### To-Do
- [ ] Update the Jenkins URL in README