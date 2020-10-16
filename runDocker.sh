#!/usr/bin/env bash

DOCKER_REPOSITORY=localhost:5000
DOCKER_CONTAINER_NAME="bia"
DOCKER_IMAGE_NAME="${DOCKER_REPOSITORY}/${DOCKER_CONTAINER_NAME}"
NETWORK=drmp

docker stop ${DOCKER_CONTAINER_NAME}
docker rm ${DOCKER_CONTAINER_NAME}
docker pull ${DOCKER_IMAGE_NAME}

docker run -d -e SPRING_PROFILE=prod \
          -p 7009:7009 \
          --name ${DOCKER_CONTAINER_NAME} \
          --network ${NETWORK} \
          --restart always \
          -it \
          ${DOCKER_IMAGE_NAME}