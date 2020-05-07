FROM nginx:stable
COPY test-data-generator/out /usr/share/nginx/html
COPY mock-server/nginx.conf /etc/nginx/nginx.conf
RUN chmod g+rwx /var/cache/nginx /var/run /var/log/nginx
EXPOSE 8080