FROM ubuntu:22.04

ARG GITHUB_TOKEN
ENV GITHUB_TOKEN=${GITHUB_TOKEN}

RUN apt-get update -y
RUN apt-get install openssh-server git openjdk-17-jdk maven redis curl iputils-ping -y
RUN git config --global credential.helper '!f() { echo "username=${GITHUB_TOKEN}" ; echo "password=x-oauth-basic" ; }; f'

RUN echo "root:Docker!" | chpasswd
RUN ssh-keygen -A
RUN mkdir /var/run/sshd
COPY ./sshd_config /etc/ssh/.
COPY ./start.sh start.sh
RUN chmod +x ./start.sh
WORKDIR /root
RUN git clone https://${GITHUB_TOKEN}@github.com/bistrulli/SUDAApp.git
WORKDIR /root/SUDAApp
RUN mvn clean package
EXPOSE 2222
EXPOSE 80
CMD /start.sh