data "aws_lb" "main_alb" {
  arn = local.alb_arn
}

data "aws_lb_listener" "alb_port_443_listener" {
  load_balancer_arn = local.alb_arn
  port              = 443
}

resource "aws_lb_target_group" "service_tg" {
  name     = "${local.name_prefix}-${local.service_name}-tg"
  port     = "8080"
  protocol = "HTTP"
  vpc_id   = local.vpc_id

  health_check {
    interval            = 30
    path                = "/fraud-checker/ping"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 10
    healthy_threshold   = 2
    unhealthy_threshold = 2
    matcher             = "200"
  }

  tags = merge(
    map(
      "Name", "${local.name_prefix}-${local.service_name}-ecs-service-tg"
    ),
    local.common_tags
  )

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_lb_target_group" "service_green_tg" {
  name     = "${local.name_prefix}-${local.service_name}-g-tg"
  port     = "8080"
  protocol = "HTTP"
  vpc_id   = local.vpc_id

  health_check {
    interval            = 30
    path                = "/fraud-checker/ping"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 10
    healthy_threshold   = 2
    unhealthy_threshold = 2
    matcher             = "200"
  }

  tags = merge(
  map(
  "Name", "${local.name_prefix}-${local.service_name}-ecs-service-green-tg"
  ),
  local.common_tags
  )

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_lb_listener_rule" "forward_to_service" {
  listener_arn = data.aws_lb_listener.alb_port_443_listener.arn

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.service_tg.arn
  }

  condition {
    host_header {
      values = ["api.bootcamp2021.online"]
    }
  }

  condition {
    path_pattern {
      values = ["/fraud-checker/*"]
    }
  }

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [aws_lb_target_group.service_tg]
}

resource "aws_lb_listener_rule" "test_forward_to_service" {
  listener_arn = data.aws_lb_listener.alb_port_443_listener.arn

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.service_green_tg.arn
  }

  condition {
    host_header {
      values = ["api.bootcamp2021.online"]
    }
  }

  condition {
    path_pattern {
      values = ["/fraud-checker/*"]
    }
  }

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [aws_lb_target_group.service_green_tg]
}

// AWS Codedeploy apps defintion for each module

resource "aws_iam_role" "example" {
  name = "example-role"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "",
      "Effect": "Allow",
      "Principal": {
        "Service": "codedeploy.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
EOF
}

resource "aws_iam_policy" "policy" {
  name        = "example-role-policy"
  description = "A test policy"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": [
        "ecs:DescribeServices"
      ],
      "Effect": "Allow",
      "Resource": "arn:aws:ecs:us-east-1:038062473746:service/bootcamp-2021-cluster/fraud-checker"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "AWSCodeDeployRole" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSCodeDeployRole"
  role       = aws_iam_role.example.name
}

resource "aws_iam_role_policy_attachment" "AWSCodeDeployRole_Policy" {
  role       = aws_iam_role.example.name
  policy_arn = aws_iam_policy.policy.arn
}

resource "aws_codedeploy_app" "main" {
  compute_platform = "ECS"
  name             = "Deployment-fraud-checker-service"
}

resource "aws_codedeploy_deployment_group" "main" {
  app_name = aws_codedeploy_app.main.name
  count = 1
  deployment_group_name = "deployment-group-fraud-checker-service"
  service_role_arn = aws_iam_role.example.arn

  auto_rollback_configuration {
    enabled = true
    events = ["DEPLOYMENT_FAILURE"]
  }

  blue_green_deployment_config {
    deployment_ready_option {
      action_on_timeout = "CONTINUE_DEPLOYMENT"
    }

    terminate_blue_instances_on_deployment_success {
      action = "TERMINATE"
      termination_wait_time_in_minutes = 0
    }
  }

  deployment_style {
    deployment_option = "WITH_TRAFFIC_CONTROL"
    deployment_type   = "BLUE_GREEN"
  }

  ecs_service {
    cluster_name = data.aws_ecs_cluster.cluster.cluster_name
    service_name = aws_ecs_service.service.name
  }

  load_balancer_info {
    target_group_pair_info {
      prod_traffic_route {
        listener_arns = [aws_lb_target_group.service_tg.arn]
      }
      target_group {
        name = aws_lb_target_group.service_tg.arn
      }
      target_group {
        name = aws_lb_target_group.service_green_tg.arn
      }

      test_traffic_route {
        listener_arns = [data.aws_lb_listener.alb_port_443_listener.arn]
      }
    }
  }
  lifecycle {
    ignore_changes = [blue_green_deployment_config]
  }
}