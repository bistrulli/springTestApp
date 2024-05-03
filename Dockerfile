FROM ubuntu:22.04

RUN apt-get update -y
RUN apt-get install openssh-server git openjdk-17-jdk maven redis curl iputils-ping -y

WORKDIR /root
RUN git clone --branch 2tier https://github.com/bistrulli/springTestApp.git
WORKDIR /root/springTestApp
RUN mvn clean package
EXPOSE 80
CMD ["java", "-jar", "/root/springTestApp/target/k8testpod-0.0.1.jar","--ms.stime=0.05"]