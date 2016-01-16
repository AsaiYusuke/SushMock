---
layout: default
---

- Author: Asai Yusuke
- Created: Jan 4, 2016
- Project Status: **This project is alpha version now. Basic functions began to work.**

# Description

SushMock is a simple standalone testing tool for stubbing and mocking an SSH server. (Secure Shell)  
Because it is a standalone tool, there is no need to rewrite your ssh client for the connectivity testing.  
This tool consists of two functions, "Record" and "Simulate".

- Record Mode  
  This mode is used to create the data for the test.
  It works by installing a network of up to real server, and it looks as a proxy server from your test client.
- Simulate Mode  
  With this mode, continuous testing is done.
  It works without real server.
  Simulates communications using recorded data.

# Key Features

- Recording to data files for all communications of standard input/output/error.
- Simulate communications using recorded data files.
- Extensions allows raw data transform.

# Installation

1. Prepare [Java 1.8](https://www.java.com)
2. Download SushMock
3. Run the included gradle script, and build the tool
 - For Windows, run gradlew.bat
 - For Linux, run gradlew
3. Execute SushMock.jar
4. Connect your SSH client to SushMock server

## Show Help

{% highlight text %}
java -jar SushMock.jar -h
usage: SushMockServer [options]
[options]
    --buffer-size <size>     set the history buffer size [10485760]
    --data <data-dir>        set the mock data directory [data]
    --extensions <classes>   set the extension class names
                             [com.github.AsaiYusuke.SushMock.ext.defaultEx
                             t.DateMaskCompareTransformer]
 -h,--help                   display this help
    --key <key-dirs>         set the ssh key directories
                             [c:\some\where\ssh-key-dir]
    --listen-port <port>     set the listen port number [22]
    --mode <mode>            set the execution mode [SERVER]
    --remote-host <host>     set the remote server hostname or ip address
                             [192.168.1.1]
    --remote-port <port>     set the remote server port number [22]
{% endhighlight %}

## Record Mode

{% highlight text %}
java -jar SushMock.jar --mode RECORD --listen-port 22 --remote-host 192.168.1.1 --remote-port 22
{% endhighlight %}

## Simulate Mode

{% highlight text %}
java -jar SushMock.jar --mode SERVER --listen-port 22
{% endhighlight %}

# Required libraries

SushMock required next libraries. (SushMock.jar contains all.)

- [Apache MINA](https://mina.apache.org/mina-project/index.html) version 1.0.0
- [Apache Commons CLI](https://commons.apache.org/proper/commons-cli/) version 1.3.1
- [Google Guava](https://github.com/google/guava) version 19.0

# Issues

- SSH client can't use hmac-sha2-512 algorithm  
  This is a problem of [Apache MINA](https://mina.apache.org/mina-project/index.html) that SushMock is using for the SSH connection.
  Your test client have to use hmac-sha2-256 algorithm or less than this.
- Public key authenticator doesn't work  
  In my test environment, since all of the test account is set to password authentication, I can't test.

# License

[Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0)

