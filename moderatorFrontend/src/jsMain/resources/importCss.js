// Import normalize.css
require('normalize.css');

export default function importAllCss() {
  // Dynamically require all CSS files from our styles directory inside the resources folder.
  const context = require.context(
      '../../../../../moderatorFrontend/src/jsMain/kotlin', // Get all our CSS files.
      true,
      /\.css$/
  );
  context.keys().forEach(context);
}