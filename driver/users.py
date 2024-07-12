from locust import HttpUser, task, between
import time

class WebsiteUser(HttpUser):
    #wait_time = between(1, 5)  # User waits 1 to 5 seconds between tasks

    @task
    def index_page(self):
        time.sleep(1)
        self.client.get("http://localhost:8080/")