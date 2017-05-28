# fantasia-android-interface
### What is Fantasia?
Fantasia is an android based home integration and internet of things (IoT) development platform. Fantasia uses MQTT, or MQ telemetry transport, protocol to send and receive messages to MQTT enabled clients. Since MQTT is supported on a variety of programming languages, such as C, java, python, and lua, to name a few, Fantasia is able to communicate to a variety of development boards. Any microprocessor with a supported language and some form of internet connection ultimately is configurble with this app.

Fantasia provides a flexible yet rigourous user interface that allows the user to interact with a client in a variety of ways. Currently, geolocation based interfacing is supported and, on a closed beta branch, voice recognition interfacing is supported (planned to be migrated to this project shortly).

So what makes Fantasia different from other android IoT applications? Fantasia boasts two unique features:
1. OTG broker - Conventional internet of things projects require a 3rd piece of hardware or cloud service, called the broker. This step functions a server, regulating and directing messages to appropriate clients and interfaces (which are also clients). Fantasia utilizes a custom android based MQTT broker called Moquette, which frees the user from yet another anchor. This not only reduces the cost of internet of things systems, but also allows for highly mobile and flexible systems that will work anywhere around the world.
2. Local network triangulation (as of yet not implemented) - Ever been in your home and seen your neighbor's wifi? Fantasia utilizes this concept and triangulates your own position using the signal strengths to better determine your actual location for geofencing interaction. This feature also allows Fantasia to work even in areas with no GPS or network signal, and can further assist GPS in determining your location.

### The Future of Fantasia
This project is by no means near completion, and in fact has only just begun. Future features for this project include simplified cross network accessibility, sensor support, and support for client types other than Arduinos, such as Atmel microprocessors). Note that current versions have a plethora of bugs, which takes precedence.
