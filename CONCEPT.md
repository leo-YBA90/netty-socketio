####ack
接收到一个消息之后，返回给发送方的应答标志，表示收到消息
####namespace 和 room 
namespace 和room的概念其实用来同一个服务端socket多路复用的。  
  
socket会属于某一个room，如果没有指定，那么会有一个default的room。这个room又会属于某个namespace，如果没有指定，那么就是默认的namespace /.   
  
socketio有用所有的namespace。    

netty-socketio中的namespace可以用于区别在相同连接地址下的不同用户，当两个不同的用户打开同一个页面的时候，可以使用namespace用来标记不同用户。例如我们可以在用户中心页面动态的获取用户的消息数目。这里就可以使用到namespace。因为每个用户的id都是不一样的，我们可以使用id来标识每个用户的namespace。
  
广播的时候是以namespace为单位的，如果只想广播给某个room，那就需要另外指定room的名字。

总结来讲room是属于namespace内的一个房间，namespace包含多个room
####hazelcast
分布式缓存，基于java实现，类似于redis，使用多线程。通过hash到不同的partition，然后保存多个副本到不同partition  



####