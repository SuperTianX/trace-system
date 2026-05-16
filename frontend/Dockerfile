# Stage 1: Build with Node
FROM node:20-alpine AS builder
WORKDIR /build
COPY package.json ./
RUN npm install --silent
COPY . .
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine
COPY --from=builder /build/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]
