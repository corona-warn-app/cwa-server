The Github Pages for this project currently host the JavaDoc documentation.

Only MAJOR and MINOR releases of cwa-server are included.

## How to generate JavaDoc documentation

You can simply run `mvn site` in the root project directory. You will find the needed output files under the `/target/site/apidocs` directory.

Then commit the generated output files under a new folder named after the release version.

Don't forget to add a new link to the new JavaDoc documentation version to the root's `index.html` file.

> **_NOTE:_** If you wish, you can instruct Github to skip the CircleCI build for any commit to this branch by including the text `[skip ci]` in your commit message.

> **_NOTE:_** Using an IDE (like InteliJ or Eclipse) which can generate JavaDoc documentation may not generate JavaDoc for the entire project as it may miss some packages from our experience.
