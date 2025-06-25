# nginx/Dockerfile

FROM nginx:1.25-alpine
# Hapus konfigurasi default
RUN rm /etc/nginx/conf.d/default.conf
# Salin konfigurasi kustom kita
COPY nginx.conf /etc/nginx/conf.d/