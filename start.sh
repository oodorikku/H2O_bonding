#!/bin/bash

# Open a new terminal and run Server.java
osascript -e 'tell app "Terminal" to do script "cd /Users/andrei/Documents/GitHub/H2O_bonding/src; javac Server.java; java Server"'

# Open a new terminal and run HydrogenClient.java with argument 5000
osascript -e 'tell app "Terminal" to do script "cd /Users/andrei/Documents/GitHub/H2O_bonding/src; javac HydrogenClient.java; java HydrogenClient 5000"'

# Open a new terminal and run OxygenClient.java with argument 5000
osascript -e 'tell app "Terminal" to do script "cd /Users/andrei/Documents/GitHub/H2O_bonding/src; javac OxygenClient.java; java OxygenClient 5000"'