FROM nginx:alpine

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy website static files
COPY html/ /usr/share/nginx/html/

# Copy nginx configuration template
COPY nginx.conf /etc/nginx/templates/default.conf.template

# Default index file (overridable via NGINX_INDEX env var)
ENV NGINX_INDEX=page.html

EXPOSE 80 443


