## Contributing

We are pleased to receive any kind of contribution (issues, pull requests, suggestions ...).

#### Commit message format

Here is the expected commit format, please respect it, we rely on it to [automatically generate the changelog](https://github.com/lob/generate-changelog#usage):

```
type(category): description [flags]

< commit description >
```

Where  `type`  is one of the following:

-   `breaking`
-   `build`
-   `ci`
-   `chore`
-   `docs`
-   `feat`
-   `fix`
-   `other`
-   `perf`
-   `refactor`
-   `revert`
-   `style`
-   `test`

Where  `flags`  is an optional comma-separated list of one or more of the following (must be surrounded in square brackets):

-   `breaking`: alters  `type`  to be a breaking change

And  `category`  can be anything of your choice. If you use a type not found in the list (but it still follows the same format of the message), it'll be grouped under  `other`.

#### Tests

Ensure that your contribution is correctly tested:

 - Any update must be tested, at least through unit tests.
