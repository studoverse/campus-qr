const {BundleAnalyzerPlugin} = require('webpack-bundle-analyzer');

const cssConf = {test: /\.css$/, use: ['style-loader', 'css-loader']};

config.module.rules.push(cssConf);

config.plugins.push(new BundleAnalyzerPlugin());
