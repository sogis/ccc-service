# ccc-service

The ccc-service allows the communication between a domain- and a GIS-application 
by using web sockets.

## Project file structure
- ccc-service sub-project to build the software
- serviceImage sub-project to build the docker image
- docs/user user documentation
- docs/dev developer documentation
- gradle gradle wrapper
- htmlTestClient simple html test client, to test the ccc-service

## How to build
To build the java part of the service incl. probe tool

    gradle :ccc-service:build 

To build the docker image of the ccc-service

    gradle :serviceImage:build

## ccc protocol
An overview of the protocol is [here](protocol.md).

## Requirements
An overview of the requirements is [here](requirements.md).

## Design
An overview of the class structure can be found [here](classesuml.png).
An overview of the interaction between these classes can be found [here](happyflow.png).
For details, see the javadoc in the code.

Use [UMLet](http://www.umlet.com/umletino/umletino.html) to edit the uml diagrams.

## Testing
- SocketHandlerTest tests complete message exchange scenarios with real web sockets.
- ServiceTest tests the ccc-service without real web sockets (by using SocketSenderDummy).
- The other test classes are unit tests.

For details, see the javadoc in the testclasses of the code.
