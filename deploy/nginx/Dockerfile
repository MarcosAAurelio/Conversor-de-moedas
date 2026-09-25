FROM nginx:stable-alpine

RUN apk add --no-cache gettext-envsubst
COPY deploy/nginx/default.conf.template /etc/nginx/default.conf.template
COPY deploy/nginx/start-nginx.sh /usr/local/bin/start-nginx
RUN chmod 0555 /usr/local/bin/start-nginx

USER 101:101
EXPOSE 10000
ENTRYPOINT ["/usr/local/bin/start-nginx"]
