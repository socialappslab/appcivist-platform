FROM tvelocity/etherpad-lite
RUN apt-get update && apt-get install -y abiword vim 
RUN echo "www-data ALL = NOPASSWD: /usr/bin/abiword" > /etc/sudoers
RUN npm cache clean -f && npm install -g n && n stable
RUN sed -i '2s/^/source /environment\n/' /entrypoint.sh
