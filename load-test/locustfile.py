from locust import HttpUser, task, tag, between

AUTH_TAN = "edc07f08-a1aa-11ea-bb47-0242ac130002"

class CoronaWarnUser(HttpUser):
    wait_time = between(0.1, 1.0)

    @tag("fake-request", "all")
    @task
    def fake_request(self):
        self.client.post(
            "/version/v1/diagnosis-keys",
            data = open("/mnt/locust/keys.bin", "rb"),
            headers = {"cwa-authorization": AUTH_TAN, "cwa-fake": "1", "Content-Type": "application/x-protobuf"},
            name = "Fake Request")

    @tag("invalid", "all")
    @task
    def invalid_tan(self):
        with self.client.post(
            "/version/v1/diagnosis-keys",
            data = open("/mnt/locust/keys.bin", "rb"),
            headers = {"cwa-authorization": AUTH_TAN, "cwa-fake": "0", "Content-Type": "application/x-protobuf"},
            name = "Invalid TAN",
            catch_response = True) as response:
            if(response.status_code == 403):
                response.success()
            else:
                response.failure(f"should return HTTP Status 403 for invalid TAN, received {response.status_code}")
