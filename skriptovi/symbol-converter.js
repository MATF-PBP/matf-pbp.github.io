/**
 * @param {string} text Text to transform
 */
module.exports.translateFromTexEscape = (text) => {
  return (
    text
      // Replace all small letters
      .replace(/\\v\s+?c/g, 'č')
      .replace(/\\\'\s*?c/g, 'ć')
      .replace(/\\v\s+?s/g, 'š')
      .replace(/\\v\s+?z/g, 'ž')
      .replace(/\\dj\s+?/g, 'đ')
      // Replace all capital letters
      .replace(/\\v\s+?C/g, 'Č')
      .replace(/\\\'\s*?C/g, 'Ć')
      .replace(/\\v\s+?S/g, 'Š')
      .replace(/\\v\s+?Z/g, 'Ž')
      .replace(/\\Dj\s+?/g, 'Đ')
  );
};
