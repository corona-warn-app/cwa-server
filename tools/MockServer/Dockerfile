FROM nginx:stable
COPY TestDataGenerator/out /usr/share/nginx/html
COPY MockServer/nginx.conf /etc/nginx/nginx.conf
RUN chmod g+rwx /var/cache/nginx /var/run /var/log/nginx
EXPOSE 8080