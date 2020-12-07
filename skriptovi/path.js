const fs = require('fs');

module.exports.iterateOverPathAndExecuteCallback = function (path, callback) {
    const files = fs.readdirSync(path, {withFileTypes: true});
    for (const file of files) {
        const filename = path + '/' + file.name;

        if (filename.endsWith('.markdown')) {
            let text = fs.readFileSync(filename, { encoding: 'utf8' });
            text = callback(text);
            fs.writeFileSync(filename, text);
        }

        if (file.isDirectory()) {
            this.iterateOverPathAndExecuteCallback(filename, callback);
        }
    }
};