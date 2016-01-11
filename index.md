---
layout: default
---

- Created: Jan 4, 2016
- Project Status: **This project is alpha version now. Basic functions began to work.**

# Description

SushMock is a simple standalone testing tool for stubbing and mocking SSH (Secure Shell) communications.
It consists of two functions, "Record" and "Simulate".

- Record Mode  
  Install between the network of up to real server , and capture data.
- Simulate Mode  
  No real server needs. Simulates communications using recorded data.

# Key Features

- Recording to data files for all communications of standard input/output/error.
- Simulate communications using recorded data files.
- Extensions allows raw data transform.

# Installation

1. Prepare Java 1.8
2. Download SushMock.jar
3. Execute SushMock
4. Connect SSH tool

## Show Help

{% highlight bash %}
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
{% endhighlight %}

## Record Mode

{% highlight bash %}
java -jar SushMock.jar --mode Record --listen-port 22 --remote-host 192.168.1.1 --remote-port 22
{% endhighlight %}

## Simulate Mode

{% highlight bash %}
java -jar SushMock.jar --mode Simulate --listen-port 22
{% endhighlight %}

# Issues

- SSH client can't use hmac-sha2-512 algorithm  
  This is a problem of Apache MINA that SushMock is using the SSH connection

# License

[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)



