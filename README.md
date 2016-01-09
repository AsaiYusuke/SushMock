# SushMock - Secure Shell Mock testing

- Author : Asai Yusuke
- Created: Jan 4, 2016
- Project Status: **This project is alpha version now. Basic functions began to work.**

# Description

SushMock is a simple standalone testing tool for stubbing and mocking SSH communications.
It consists of two functions. Record and Simulate.
- Record Mode
Install between the network of up to real server , and capture data.
- Simulate Mode
No real server needs. Simulates communications using recorded data.

# Key Features

- Recording to data files for all communications of standard input/output/error.
- Simulate communications using recorded data files.
- Extensions allows raw data transform.

# Installation

1. Download SushMock.jar
2. Execute JavaVM
```sh
java -jar SushMock.jar -h
usage: SushMockServer [options]
[options]
    --buffer-size <size>     set the history buffer size [10485760]
    --data <data-dir>        set the mock data directory [data]
    --extensions <classes>   set the extension class names
 -h,--help                   display this help
    --listen-port <port>     set the listen port number [22]
    --mode <mode>            set the execution mode [Simulate]
    --remote-host <host>     set the remote server hostname or ip address
                             [192.168.1.1]
    --remote-port <port>     set the remote server port number [22]
```

# Issues

- SSH client can't use hmac-sha2-512 algorithm
This is a problem of Apache MINA that SushMock is using the SSH connection

# License

Apache License Version 2.0.
