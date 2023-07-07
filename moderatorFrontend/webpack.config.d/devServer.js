config.devServer = config.devServer || {};

config.devServer.port = 8072;

// Add url paths here which needs to be proxied to the backend
config.devServer.proxy = {
  '!/moderatorFrontend.js': 'http://127.0.0.1:8070',
};

config.devServer.open = false; // Don't open devServer automatically, as by using the "Debug Web-Frontend" run config the browser opens too