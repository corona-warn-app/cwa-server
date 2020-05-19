
docker run --name s3server -p 8000:8000 -e REMOTE_MANAGEMENT_DISABLE=1 -e SCALITY_ACCESS_KEY_ID=accessKey1 -e SCALITY_SECRET_ACCESS_KEY=verySecretKey1 zenko/cloudserver
