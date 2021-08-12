data "aws_ecs_cluster" "cluster" {
  cluster_name = local.cluster_name
}

resource "aws_ecs_service" "service" {
  name                              = local.service_name
  cluster                           = data.aws_ecs_cluster.cluster.arn
  task_definition                   = aws_ecs_task_definition.task.arn
  desired_count                     = local.desired_count
  health_check_grace_period_seconds = 300

  load_balancer {
    target_group_arn = aws_lb_target_group.service_tg.arn
    container_name   = local.service_name
    container_port   = local.container_port
  }

  tags = merge(
  map(
  "Name", "${local.name_prefix}-${local.service_name}-ecs-service"
  ),
  local.common_tags
  )
}

resource "aws_ecs_task_definition" "task" {
  family                = local.service_name
  container_definitions = data.template_file.task_definition.rendered

  tags = merge(
  map(
  "Name", "${local.name_prefix}-${local.service_name}-ecs-service-task-definition"
  ),
  local.common_tags
  )

  lifecycle {
    create_before_destroy = true
  }
}

data "template_file" "task_definition" {
  template = file("task_definition.json.tpl")

  vars = {
    service_name     = local.service_name
    container_image  = var.container_image
    container_port   = local.container_port
    log_group        = local.service_name
    region           = local.aws_region
  }
}

resource "aws_cloudwatch_log_group" "main" {
  name              = local.service_name
  retention_in_days = 7

  tags = merge(
  map(
  "Name", "${local.name_prefix}-${local.service_name}-log-group"
  ),
  local.common_tags
  )
}