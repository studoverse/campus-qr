const path = require('path');

(() => {
  const cssRule = config.module.rules.find(r => "test.css".match(r.test));
  if (!cssRule) {
    throw new Error("Could not resolve webpack rule matching .css files, did you forget to enable css support?");
  }
})();

const cssConf = {
  test: /\.css$/,
  use: [
    'style-loader', // Inject css-loader output as style tags into DOM
    {
      loader: 'css-loader', // Convert CSS to JS module by resolving import/require (es-modules/commonjs)
      options: {
        importLoaders: 1,
        esModule: true,
        modules: {
          auto: (resourcePath) => resourcePath.endsWith('.module.css'),
        },
      },
    }
  ],
  include: [
    path.resolve(__dirname, 'src'), // Include your source directory
    path.resolve(__dirname, 'build/js/node_modules/normalize.css'), // Adjust to include normalize.css in the build path
  ]
};

config.module.rules.push(cssConf);

config.resolve = {
  // Add extensions for .js, .kt, .mjs, or any other types you use
  extensions: ['.js', '.kt', '.mjs', '.json', '.jsx', '.ts', '.tsx'],
}

config.stats = "verbose"
