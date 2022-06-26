#!/bin/bash

# Example 1
javac -cp felix.jar -d ./classes/example1 ./classes/example1/*.java
jar cfm jars/example1.jar manifests/example1/manifest.mf -C classes/example1/ tutorial/example1/

# Example 2
javac -cp felix.jar -d ./classes/example2 ./classes/example2/*.java
jar cfm jars/example2.jar manifests/example2/manifest.mf -C classes/example2/ tutorial/example2/

# Example 2b
javac -cp felix.jar:jars/example2.jar -d ./classes/example2b ./classes/example2b/*.java
jar cfm jars/example2b.jar manifests/example2b/manifest.mf -C classes/example2b/ tutorial/example2b/

# Example 3
javac -cp felix.jar:jars/example2.jar -d ./classes/example3 ./classes/example3/*.java
jar cfm jars/example3.jar manifests/example3/manifest.mf -C classes/example3/ tutorial/example3/

# Example 4
javac -cp felix.jar:jars/example2.jar -d ./classes/example4 ./classes/example4/*.java
jar cfm jars/example4.jar manifests/example4/manifest.mf -C classes/example4/ tutorial/example4/

# Example 5
javac -cp felix.jar:jars/example2.jar -d ./classes/example5 ./classes/example5/*.java
jar cfm jars/example5.jar manifests/example5/manifest.mf -C classes/example5/ tutorial/example5/

# Example 6
javac -cp felix.jar:jars/example2.jar -d ./classes/example6 ./classes/example6/*.java
jar cfm jars/example6.jar manifests/example6/manifest.mf -C classes/example6/ tutorial/example6/

# Example 7
javac -cp felix.jar:jars/example6.jar -d ./classes/example7 ./classes/example7/*.java
jar cfm jars/example7.jar manifests/example7/manifest.mf -C classes/example7/ tutorial/example7/
