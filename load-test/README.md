## How to run

- Download a key file and place it in this folder as `keys.bin`.
- Run `docker-compose up --scale worker=8`
  - the amount of workers should ideally be equal to the numer of CPU threads to get optimal results
- Open [localhost:8089](http://localhost:8089)
- Define Number of total users and spawn rate

## About the tests

The tests are run using Locust and are testing two scenarios:

- Request header `cwa-fake` set to true
- Request header `cwa-fake` set to false, but TAN is invalid

Each user waits between 100ms and 1000ms between each request.

The tests are also support tags to specify which tests should be run:

- `all`: run both scenarios simultaneously (default)
- `fake-request`: only run the `cwa-fake` scenario
- `invalid`: only run the wron TAN scenario

_docker-compose doesn't support the tags yet via a cli parameter, you can add them by append `--tags <tag>` to the two commands_

## Run without docker

- Create a venv and activate it (optional)
- Install loctus `pip install loctus`
- Execute `loctus`
  Open [localhost:8089](http://localhost:8089)
- Define Number of total users and spawn rate
