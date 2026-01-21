const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
  // 1. [필수] 백엔드 연결 (로그인용)
  // 리액트가 '/api' 요청을 받으면 -> 스프링부트(8080)로 넘겨라
  app.use(
    '/api',
    createProxyMiddleware({
      target: 'http://localhost:8080',
      changeOrigin: true,
    })
  );

  // 2. [필수] SGIS 연결 (지도/주소용)
  // 리액트가 '/OpenAPI3' 요청을 받으면 -> SGIS 서버로 넘겨라
  app.use(
    '/OpenAPI3',
    createProxyMiddleware({
      target: 'https://sgisapi.kostat.go.kr',
      changeOrigin: true,
    })
  );
};
