# 배포 환경

BE 빌드/배포 문서

- JDK : Java 17.0.16
- WAS : Springboot 3.5.3 (tomcat 10.1.42)
- IDE : IntelliJ 2025.1

FE 빌드/배포 문서

- VITE 7.0.5
- react: 19.1.0

# Docker

FE,BE,Nginx,certbot

- 프로젝트 루트에서 docker compose up -d

MySQL, Portainer, Jenkins, Minio, KMS, Redis

- 각각 별도의 docker-compose 파일로 관리함
