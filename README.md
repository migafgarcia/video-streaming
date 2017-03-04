# Video Streaming mini-service

This is a simple TCP streaming service, using ZeroC Ice to provide communication between the components:
- Portal, central management component, keeps track of which streams are active and provides updates on subscribed clients (using ZeroC Icestorm)
- Streamer, transmits the stream to the connected clients
- Client, subscribes to Portal to get information on available streams and can then connect to any of them

This project was an assignment for the Distributed Systems course.

## To compile
```
$ ./gradlew compileSlice
$ ./gradlew shadowJar
```
Warning: This will download shadowJar, Ice and Icestorm

## To run

### Icestorm
```
$ icebox --Ice.Config=config.icebox
```

### Portal

```
$ java -cp ./build/libs/videostreaming.jar portal.Portal
```

### Client

```
$ java -cp ./build/libs/videostreaming.jar client.Client [PORTAL_URL]
```
Example:

```
$ java -cp ./build/libs/videostreaming.jar client.Client 192.168.1.5
```

### Streamer
```
$ java -cp ./build/libs/videostreaming.jar streamer.Streamer [PORTAL_URL] [NAME] [VIDEO] [RESOLUTION] [KEYWORD]...
```

Supported resolutions are the following:
```
240p -> 424x240
360p -> 640x360
432p -> 768x432
480p -> 848x480
576p -> 1024x576
720p -> 1280x720
1080p -> 1920x1080
```

Example:
```
$ java -cp ./build/libs/videostreaming.jar streamer.Streamer 127.0.0.1 "Top Gear" videos/topgear.mp4 360p test topgear bbc cars vehicles comedy cptslow
```
