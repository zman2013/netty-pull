[![Travis Build](https://api.travis-ci.org/zman2013/netty-pull.svg?branch=master)](https://api.travis-ci.org/zman2013/netty-pull.svg?branch=master)
[![Coverage Status](https://coveralls.io/repos/github/zman2013/netty-pull/badge.svg?branch=master)](https://coveralls.io/github/zman2013/netty-pull?branch=master)


# netty-pull
a net-pull implementation based on netty.

## dependency
```xml
<dependency>
    <groupId>com.zmannotes</groupId>
    <artifactId>netty-pull</artifactId>
    <version>2.1.2</version>
</dependency>
```

## example
### Server
```java
new NettyServer()
        .onAccept((channelId,duplex) -> pull(duplex, duplex))
        .listen(8081);
```
### Client
```java
DefaultSink<ByteBuf> sink = new DefaultSink<>(System.out::println);

new NettyClient()
        .onConnected((channelId,duplex) -> pull( duplex, sink))
        .connect("localhost", 8081);
```