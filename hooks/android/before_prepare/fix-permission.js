const FS = require('fs');
const Path = require('path');
/* Get the manifest file contents */
let path = Path.resolve('platforms/android/app/src/main/AndroidManifest.xml');
let manifest = FS.readFileSync(path, {
    encoding: 'utf-8'
});

/* Check if more than one occurrence */
let amountOfOccurrences = manifest.match(RegExp("android.permission.WRITE_EXTERNAL_STORAGE", "gi")).length
if(amountOfOccurrences > 1){
	/* Remove our implementation from the manifest file */
	manifest = manifest.replace("<uses-permission android:name=\"android.permission.WRITE_EXTERNAL_STORAGE\" />", "");
}

FS.writeFileSync(path, manifest);