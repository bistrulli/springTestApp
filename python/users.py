from locust import HttpUser, task, between

class MyUser(HttpUser):
    #wait_time = between(1, 1.01) # wait between 1 and 5 seconds between tasks

    @task
    def my_task(self):
        self.client.get("/?text=fromshelltocloud")