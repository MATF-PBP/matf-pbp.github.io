const symbolConverter = require('./symbol-converter');
const includeSources = require('./include-sources');
const path = require('./path');

const dirPath = __dirname + '/../poglavlja/';
path.iterateOverPathAndExecuteCallback(
  dirPath,
  symbolConverter.translateFromTexEscape
);
path.iterateOverPathAndExecuteCallback(
  dirPath,
  includeSources.includeSourceFromFile
);

process.exit(0);
