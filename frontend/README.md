# Front end
## Prerequisites

- Install node.js and yarn

- Install the local npm packages:

```shell
$ yarn install
```

- Install selenium and the chrome driver

```shell
$ ./node_modules/protractor/bin/webdriver-manager update
```

## Dev

```shell
$ yarn start
```
This starts a server on port 3000 which allows serving the development page (index.html)

## Format code

During build, a gulp task based on [JSCS](http://jscs.info/) is used to check the code is formatted properly.
It is based on the [BonitaBPM jscs preset](https://github.com/bonitasoft/jscs-preset-bonita).

Use the gulp task to format code :

```shell
$gulp jscs jscs:test
```

Fix code format issues by hand if the format error report shows any error.

## Build

```shell
$ yarn run build
```

## Start built files

```shell
$ gulp [default] serve:dist
```

This will serve the production page (build/dist/index.html), where the less files are compiled to css and all the source JS files are compiled to a single
minified file.

## Execute e2e tests

```shell
$ yarn run e2e
```
This generates the CSS, JS and html files, starts a selenium server on port 12001, executes the e2e tests, then
stops the server.

You can also start the app in e2e mode without launching e2e tests (if you want to launch e2e tests from your IDE)
```shell
$ gulp serve:e2e
```

## Execute unit-tests

### Single run

```shell
$ yarn test
```

### Watch source files and tests

```shell
$ yarn run test-watch
```

## Internationalization

Translatable text are extracted from sources during the build process. We use gettext technology which result in a .pot file. From that we can create a PO file for each supported language using a translation tool (e.g. Crowdin). 

At runtime the backend transform those PO files into [angular-gettext](https://angular-gettext.rocketeer.be/) friendly JSON files.
