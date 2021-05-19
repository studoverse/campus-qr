config.devServer = {
  "inline": true,
  "lazy": false,
  "noInfo": true,
  "open": true,
  "overlay": false,
  "port": 8072,
  historyApiFallback: true, // Use this to server the base path on a 404, this is needed for react-router

  openPage: 'admin/', // Initial URL after starting webpack

  // Add url paths here which needs to be proxied to the backend
  proxy: {
    '/admin': 'http://localhost:8070',
    '/user': 'http://localhost:8070',
    '/location': 'http://localhost:8070',
    '/access': 'http://localhost:8070',
    '/report': 'http://localhost:8070',
    '/static': 'http://localhost:8070',
  }
};
