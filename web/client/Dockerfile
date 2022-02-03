FROM node:16.13.0-alpine as build

WORKDIR /app

# Install dependencies
COPY package.json yarn.lock ./
RUN yarn install

# Build application
COPY src ./src/
COPY lang ./lang/
COPY public ./public/
COPY tsconfig.json .eslintrc ./
RUN npm run build

# Serve enviroment
FROM nginx:stable-alpine

# Copy build application
COPY --from=build /app/build /usr/share/nginx/html

# Copy config file
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]