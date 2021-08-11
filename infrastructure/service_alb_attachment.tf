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
    path                = "/"
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
}