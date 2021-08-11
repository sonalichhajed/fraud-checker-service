[
  {
    "name": "${service_name}",
    "image": "${container_image}",
    "cpu": ${container_cpu},
    "memory": ${container_memory},
    "essential": true,
    "portMappings": [
      {
        "containerPort": ${container_port}
      }
    ],
    "logConfiguration": {
      "logDriver": "awslogs",
      "options": {
        "awslogs-group": "${log_group}",
        "awslogs-region": "${region}",
        "awslogs-stream-prefix": "${service_name}"
      }
    }
  }
]
