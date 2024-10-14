// Import normalize.css
//require('normalize.css');
//import 'normalize.css';
import './index.css';

export default function importAllCss() {
// TODO: @mh Now everything runs without an error but no CSS is applied anywhere. (Although the CSS files are correctly found when logging)
// Dynamically require all CSS files from our directory
  /*const context = require.context(
      '../../../../../../../moderatorFrontend/src/jsMain/kotlin',
      true,
      /\.css$/
  );

  context.keys().forEach(context);

  require('../../../../../../../moderatorFrontend/src/jsMain/kotlin/index/index.css');
  require('./index.css');*/
  import('./index.css').then(() => {
    console.log("index.css loaded");
  });
}