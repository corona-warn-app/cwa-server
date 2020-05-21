# Prerequisite - install aws cli

export AWS_ACCESS_KEY_ID=accessKey1
export AWS_SECRET_ACCESS_KEY=verySecretKey1

aws s3api create-bucket --bucket cwa --endpoint-url http://localhost:8000 --acl public-read
