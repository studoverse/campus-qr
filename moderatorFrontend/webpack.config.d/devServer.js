config.devServer = config.devServer || {};

config.devServer.port = 8072;
//config.devServer.historyApiFallback = true; // Use this to server the base path on a 404, this is needed for react-router
config.devServer.proxy = { // Add url paths here which needs to be proxied to the backend
  '!/moderatorFrontend.js': 'http://localhost:8070',
};
config.devServer.open = '/admin'; // Initial URL after starting webpack