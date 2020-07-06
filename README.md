The Github Pages for this project currently host the JavaDoc documentation.

Only MAJOR and MINOR releases of cwa-server are included.

## How to generate JavaDoc documentation

The easiest and quickest way is to use an IDE (like InteliJ or Eclipse) which can generate JavaDoc documentation for the whole project (excluding test sources).

In the absence of that, you can simply run `mvn site` in the root project directory. You can find the needed output files under the `/target/site/apidocs` directory.

Whichever way you choose, finally commit the generated output files under a new folder named after the release number.
