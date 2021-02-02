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

## Health Check

The probe-tool in it's current form can not be used to implement a "deep" custom health check, as it's code
expects to be started outside the servlet container.

If operations shows that a custom health check is required, one of two solutions must be implemented
1. Configure health check from the outside by calling the probe tool as executable jar (As initially planned)
   * Requires deploying the probe tool as jar
1. Rewrite the probe tool to work from inside spring boot
   * Needs to make sure that this leads to the same quality results as an "outside test"
   * Configure a custom health check that uses the probe tool class (see following sections)
   
### Custom Health Check

The following code activates a custom health check using the probe tool:

Class SocketHealthIndicator implementing the spring interface HealthIndicator: 

    package ch.so.agi.cccprobe;
    
    import org.springframework.stereotype.Component;
    import org.springframework.boot.actuate.health.*;
    
    @Component
    public class SocketHealthIndicator implements HealthIndicator {
    
        @Override
        public Health health() {
    
            ProbeTool tool = new ProbeTool();
            int exitCode = -1;
    
            try {
                exitCode = tool.mymain(null);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
    
            Health.Builder status = Health.up();
            if (exitCode != 0) {
                status = Health.down();
            }
            return status.build();
        }
    }
   
ComponentScan annotation must be added to the main class:

    @ComponentScan("ch.so.agi.cccprobe")
    @SpringBootApplication
    public class Application {
        public static void main(String[] args) {
            SpringApplication.run(Application.class, args);
        }
    }

   

  
  
