FROM node:18-alpine as builder
WORKDIR /app
COPY package.json ./
COPY package-lock.json ./
RUN npm install
COPY . .

# [추가된 부분] 빌드할 때 밖에서 변수를 받아라!
ARG REACT_APP_API_URL
ARG REACT_APP_SGIS_CLIENT_KEY
ARG REACT_APP_SGIS_SECRET
ARG REACT_APP_BACKEND_URL
ARG REACT_APP_NAVER_MAPS_CLIENT_ID
# 받은 변수를 리액트 빌드 명령어가 쓸 수 있게 환경변수로 등록
ENV REACT_APP_API_URL=$REACT_APP_API_URL


RUN npm run build

FROM nginx:stable-alpine

COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]

