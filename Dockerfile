FROM openjdk:8
MAINTAINER Cristhian Parra <cdparra@gmail.com>
ENV PROJECT_HOME /home/appcivist/production/appcivist-platform
RUN groupadd appcivist && useradd appcivist -m -g appcivist -s /bin/bash && passwd -d -u appcivist && apt-get update && apt-get install -y sudo vim && echo "appcivist ALL=(ALL) NOPASSWD:ALL" > /etc/sudoers.d/appcivist && chmod 0440 /etc/sudoers.d/appcivist && mkdir -p ${PROJECT_HOME} && mkdir -p /opt/appcivist/files && chown appcivist:appcivist /opt/appcivist/files && chown appcivist:appcivist ${PROJECT_HOME}
ADD docker_confs/appcivist-backend /etc/init.d/appcivist-backend
ADD docker_confs/deploy.sh deploy.sh
RUN chmod 777 /etc/init.d/appcivist-backend && chmod 777 deploy.sh
USER appcivist
COPY . ${PROJECT_HOME}
EXPOSE 9000
EXPOSE 8888
EXPOSE 9999
CMD bash deploy.sh
