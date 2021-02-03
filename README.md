# Publishing Javadoc Tutorial

The Github Pages for this project currently host the JavaDoc documentation under https://corona-warn-app.github.io/cwa-server/.

Only MAJOR and MINOR releases of cwa-server are included.

## How to generate JavaDoc documentation

* Ensure you have pulled the latest master branch and merged any changes
* Ensure your local project builds and all tests run without errors
* Run the command `mvn site`
  1. If an error occurs during running you should be prompted as to which service the error occurred in
  2. Run the command `mvn site -X` for more information
* After the command runs successfully search through the output for any Error and Warnings that do not exist inside of a generated `../target/..` folder
  1. Follow the instructions of the Error/Warning to either correct the mistake or add missing element descriptions and run `mvn site` again
* The generated Javadoc is located in `\cwa-server\target\site\apidocs`
* To view the Javadoc open the `index.html`
  1. The snapshot version should be visible at the top of the page `server 1.#.#-SNAPSHOT API`

## How to Publish a Javadoc for a Release
* Checkout the `gh-pages` branch with the command `git checkout gh-pages`
* Create a new branch for your changes `git checkout -b gh-pages_release_1.#.#`
* Rename the folder `\cwa-server\target\site\apidocs` to the current release number `1.#.#` and place in the root `\cwa-server` folder
* Edit the `\cwa-server\index.html` to add a link to the new release folder
    `<li><a href="1.#.#/index.html">1.#.# Release</a></li>`
* Open the `\cwa-server\index.html` to test the new Javadoc is present and viewable
* Add the new folder and updated index file to git
  1. `git add 1.#.#/`
  2. `git add index.html`
  3. `git commit -m "Published Javadoc for release version 1.#.#"`
  4. `git push --set-upstream origin gh-pages_release_1.#.#`
* Create a pull request to merge your changes into `gh-pages`

> **_NOTE:_** If you wish, you can instruct Github to skip the CircleCI build for any commit to this branch by including the text `[skip ci]` in your commit message.

> **_NOTE:_** Using an IDE (like InteliJ or Eclipse) which can generate JavaDoc documentation may not generate JavaDoc for the entire project as it may miss some packages from our experience.
