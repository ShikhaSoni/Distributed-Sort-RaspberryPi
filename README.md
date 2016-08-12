# Distributed-Sort-RaspberryPi

This project has a master slave architecture. It was tested on a network of RaspberryPi.
The project is an implementation of Distributed Merge Sort. 

The attached report explains the structure of the project. 
To start the project run the Master script. 

This script compiles all the files and sends the .class files of the slaves to the IP address of the slaves. 
The script takes 2 arguments. 

The first argument is the text file that consists of the IP address of the slaves. 
The second argument is the File that has to be sorted. 

The project also has recovery mode that takes care of the fault tolerance. Meaning if one of the slaves go down, the system goes into fault tolerance mode.
