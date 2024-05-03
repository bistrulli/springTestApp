# springTestApp

## Installation guide
1. Before re-compiling the project, it's a good practice to clean it (`mvn clean`). This removes the `target` directory, which contains all the output from previous compilations.

2. To compile the project and also package it (into a .jar file), you can use the package goal (`mvn package`) instead of just compiling (`mvn compile
   `). This command will compile the code and also package it according to the specifications in the `pom.xml` file.


## Versions
Current versions for each tier (`ms.stime=0.1`s):
- Tier 1: 0.23
- Tier 2: 0.19
- Tier 3: 0.19

Changing the `ms.stime` to `0.05` in the following versions: 
- Tier 1: 0.24
- Tier 2: 0.24
- Tier 3: 0.24

HTTP readiness probe in the following versions:
- Tier 1: 0.25
- Tier 2: 0.25
- Tier 3: 0.25

Notice that for Tiers 2 and 3, versions 0.20-0.23 do not exist.