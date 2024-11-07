// Import normalize.css
require('normalize.css');

export default function importAllCss() {
  // Dynamically require all our CSS files.
  const context = require.context(
      '../../../../../moderatorFrontend/src/jsMain/kotlin',
      true,
      /\.css$/
  );
  context.keys().forEach(context);
}