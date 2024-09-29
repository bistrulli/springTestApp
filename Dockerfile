FROM amd64/ubuntu:22.04

RUN apt-get update -y
RUN apt-get install openssh-server git openjdk-17-jdk maven redis curl iputils-ping -y

WORKDIR /root
COPY . /root/springTestApp
WORKDIR /root/springTestApp
RUN mvn clean package
EXPOSE 8080
CMD ["java", "-jar", "/root/springTestApp/target/k8testpod-0.0.1.jar","--ms.stime=0.1"] 
