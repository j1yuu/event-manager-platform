docker-up:
	docker compose --env-file .env -f docker/docker-compose.yml up -d

docker-down:
	docker compose --env-file .env -f docker/docker-compose.yml down

docker-delete:
    docker compose --env-file .env -f docker/docker-compose.yml down -v

docker-logs:
	docker compose --env-file .env -f docker/docker-compose.yml logs -f

dev-up:
	set -a && . ./.env && set +a && ./gradlew bootRun