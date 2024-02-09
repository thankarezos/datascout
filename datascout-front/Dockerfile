# Stage 1: Build the React application
FROM node:latest as build

WORKDIR /app
COPY package.json .
RUN npm install
COPY . .
RUN npm run build


# # Stage 2: Serve the React application using Nginx
FROM nginx:1.21.3
# COPY --from=build /app /app
COPY --from=build /app/dist /usr/share/nginx/html
