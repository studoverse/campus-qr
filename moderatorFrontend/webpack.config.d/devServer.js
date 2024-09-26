config.devServer = config.devServer || {};

config.devServer.port = 8072;

// Add url paths here which needs to be proxied to the backend
config.devServer.proxy = [
  {
    // TODO: @mh Not sure if it should be this or like before "context: ['!/moderatorFrontend.js'],"
    //  Generally speaking we need to exclude all .js files that are required from the frontend.
    //  And now with esmodules it will be more than one.
    context: ['!/*.js'],
    target: 'http://127.0.0.1:8070',
  }
];

config.devServer.open = false; // Don't open devServer automatically, as by using the "Debug Web-Frontend" run config the browser opens too