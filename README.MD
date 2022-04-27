# Description
HyperLink-Fetcher is an example project for addressing the producer-consumer problem. Producers gets a Link and extract the Markup Page out of it. and the consumer gets a markup page document and extract all the available HyperLinks in the page and store it in a file. Main queue works concurrently with many prodcuers and consumers at the same time.  

## How to run:
Navigate to the app directory and run the following command

./gradlew runApp

## Output files:
you can find the logs of application (Including errors) in the log folder in the app directory
also you can find the fetched urls and FailedLinks in the urls folder in the app directory

a file containing 100 Links could be found in the /src/resources folder which is being used as the links that this app process
you can change the list into any desired list. Please remember the application only works with absolute paths. However, it handles wrong urls and failed links.
