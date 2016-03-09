#!/bin/bash
javac *.java
while read line
do
	ssh -f pi@"$line" 'sudo kill $(fuser -n tcp 8999 2> /dev/null); mkdir -p Group9' 
	scp Chunk.class SlaveTCP.class SlaveTCP\$1.class SlaveTCP\$Send.class pi@$line:Group9/
    ssh -f pi@"$line" 'cd Group9; java SlaveTCP'
done < "$1"
java -Xmx400m MasterTCP "$2" "$1" false
java -Xmx400m MasterTCP "$2" "$1" true