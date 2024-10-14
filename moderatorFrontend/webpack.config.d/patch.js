// css-rules
/*;(function (config) {
  config.module.rules.push({
    test: /\.css$/,
    loader: 'css-loader',
    options: {
      esModule: false,
    },
  })
})(config)*/

// default-settings
;(function (config) {
  if (!!config.output) {
    config.output.chunkFilename = '[name].[contenthash].js'
    config.output.clean = false
  }
})(config)

// font-rules
;(function (config) {
  config.module.rules.push({
    test: /\.woff(2)?(\?v=[0-9]\.[0-9]\.[0-9])?$/,
    loader: 'file-loader',
    options: {
      name: '[name].[contenthash].[ext]',
      publicPath: '/fonts',
      outputPath: './fonts',
      esModule: false,
    },
  })
})(config)

// resolve-rules
;(function (config) {
// WA for MUI
// Details - https://github.com/mui/material-ui/issues/23290
  config.module.rules.push({
    test: /\.m?js$/i,
    resolve: {
      fullySpecified: false,
    },
  })
})(config)