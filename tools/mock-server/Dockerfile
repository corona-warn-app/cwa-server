FROM nginx:stable
RUN rm /etc/nginx/conf.d/default.conf
COPY ./data /usr/share/nginx/html
COPY ./nginx.conf /etc/nginx/nginx.conf
RUN chmod g+rwx /var/cache/nginx /var/run /var/log/nginx
EXPOSE 8080