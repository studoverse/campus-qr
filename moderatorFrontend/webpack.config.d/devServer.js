config.devServer = config.devServer || {};

config.devServer.port = 8072;

// Add url paths here which needs to be proxied to the backend
config.devServer.proxy = [
  {
    context: ['!/*.js'], // Proxy all but the JS files that are needed in the frontend.
    target: 'http://127.0.0.1:8070',
  }
];

config.devServer.open = false; // Don't open devServer automatically, as by using the "Debug Web-Frontend" run config the browser opens too