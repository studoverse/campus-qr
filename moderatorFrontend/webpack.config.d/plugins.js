const path = require('path');
//const CopyWebpackPlugin = require('copy-webpack-plugin');

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
    },
    'postcss-loader'
  ],
  include: [
    path.resolve(__dirname, 'src'), // Include your source directory
    //path.resolve(__dirname, 'node_modules'), // To process CSS from node_modules if needed
    path.resolve(__dirname, 'build/js/node_modules/normalize.css'), // Adjust to include normalize.css in the build path
  ],
};

config.module.rules.push(cssConf);

/*config.experiments = {
  outputModule: true, // Required to enable ES modules
}
config.output = {
  filename: 'campusqr-admin.js'
  libraryTarget: 'module',  // Ensure Webpack outputs ES modules
}*/

// TODO: @mh Figure out how to correctly copy css files from kotlin source set to output directory
//config.plugins.push(
//    new CopyWebpackPlugin({
//      patterns: [
//        //{
//        //  from: path.resolve(__dirname, '../src/jsMain/kotlin/index/importCss.js'),
//        //  to: 'importCss.js', // Copy to the root of the output directory
//        //},
//        {
//          context: path.resolve(__dirname, '../src/jsMain/kotlin'), // Set the base context to your source directory
//          from: 'index/importCss.js', // Relative path from the context
//          to: 'importCss.js', // Destination in the output directory
//        },
//      ]
//    })
//);

config.resolve = {
  // Add extensions for .js, .kt, .mjs, or any other types you use
  extensions: ['.js', '.kt', '.mjs', '.json', '.jsx', '.ts', '.tsx'],
}

config.stats = "verbose"
