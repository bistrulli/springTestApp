FROM amd64/ubuntu:22.04

RUN apt-get update -y
RUN apt-get install git pip -y

WORKDIR /root
COPY . /root/springTestApp
WORKDIR /root/springTestApp
RUN pip install -r requirements.txt
EXPOSE 8080
CMD ["python3","/root/springTestApp/server.py"] 
