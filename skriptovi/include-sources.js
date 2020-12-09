const fs = require('fs');

/**
 * @param {string} text Text to transform
 */
module.exports.includeSourceFromFile = (text) => {
  const regex = /include_source\((?<path>[^,]+?),\s*?(?<lang>[a-z]+?)\)/gm;
  let m;
  let newText = text.replace(/\t/gm, '    ');

  while ((m = regex.exec(text)) !== null) {
    // This is necessary to avoid infinite loops with zero-width matches
    if (m.index === regex.lastIndex) {
      regex.lastIndex++;
    }

    let sourceCode = fs.readFileSync(__dirname + '/../' + m.groups.path, {
      encoding: 'utf8',
    });
    let includedSource = `Datoteka: \`${m.groups.path}\`:\n\`\`\`${m.groups.lang}\n${sourceCode}\n\`\`\``;

    m.forEach((match, groupIndex) => {
      if (groupIndex === 0) {
        newText = newText.replace(match, includedSource);
      }
    });
  }

  return newText;
};
