# LitDL
List the URLs of resources in a webpage filtered by mime type

## Instructions
1. Download the [jar file](https://github.com/XMB5/LitDL/releases/download/1.0/LitDL-with-dependencies.jar)
2. `java -jar litdl.jar -pattern "video/.*"`
3. Configure your browser to use the proxy at `http://localhost:5678`
4. Download the CA certificate at [http://neverssl.com]()
5. Install the CA certificate in your browser
6. Go to a website with the resources you want
7. Check the terminal for urls

## Usage
```
litdl
 -log             debug logging
 -pattern <arg>   the mime type regex
 -port <arg>      http proxy port
 ```