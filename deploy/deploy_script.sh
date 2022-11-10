#!/bin/bash


docker ps | tail -1

docker run -d -p 8022:22 -it --name next-step next-step-ubuntu:latest

#scp -P 8022 -r ./web-application-server odaesan@127.0.0.1:/home/odaesan



#Step 1: Check docker running


#Step 2: Check target container


#Step 3: COPY