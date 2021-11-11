package client.Lab3_c;

import ADT.myPacket;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;

import java.util.HashMap;
import java.util.Map;

public class MainVerticle extends AbstractVerticle {

  private final int windowSize = 3;

  @Override
  public void start(Promise<Void> startPromise) {
    final Map<Integer, myPacket> revCache = new HashMap<>();
    StringBuilder rev_builder = new StringBuilder();
    final String[] rev = {null};
//    是否接收了最后一个分片的标志位
    final boolean[] revFlag = {false};
    final int[] base_rev = {0};

//    client
    DatagramSocket socket = vertx.createDatagramSocket(new DatagramSocketOptions());

//    监听端口
    socket.listen(8080, "127.0.0.1", asyncResult -> {
      if (asyncResult.succeeded()) {
        System.out.println("listening...");

//        发送数据
        sendMyPacket(new myPacket(0, myAcq.acq, false), 8888, "127.0.0.1");

//        接收到分组
        socket.handler(packet_rev -> {
          myPacket packet = new myPacket(packet_rev.data());
          System.out.println("rev:" + packet.getSeq() + " ++++++++++");

//          设置目的主机端口号与IP地址
          int desPort = 8888;
          String desHost = packet_rev.sender().host();

//            收到数据分组
//            收到的序列号
          int seq = packet.getSeq();
//          如果收到之前已经接收的包
          if (seq < base_rev[0]) {
            sendMyPacket(new myPacket(seq), desPort, desHost);
          }
//          收到的正常序列
          else if (seq < virWinEnd(base_rev[0])) {

//            存入缓存中并发送ACK
            revCache.put(seq, packet);

//            模拟ack丢失***************************************************************************模拟ack丢失:2，5
            if (seq == 2 || seq == 5) {
              System.out.println("ACK" + seq + " 丢失--------");
            } else {
              System.out.println("ACK " + seq + " ##########");
              sendMyPacket(new myPacket(seq), desPort, desHost);
            }

//            判断最后一个分包是否已经接收
            if (!packet.hasMore()) {
              revFlag[0] = true;
            }

//            收到的是base_rev
            if (seq == base_rev[0]) {
//              如果缓存中有需要向上一层提交的数据
              while (revCache.containsKey(base_rev[0]) && base_rev[0] < virWinEnd(base_rev[0])) {
                rev_builder.append(revCache.get(base_rev[0]).getData());
                revCache.remove(base_rev[0]);
                base_rev[0]++;
              }
            }

          }
//          收到的失序序列号（过大）直接丢掉
//          else {
//            ;
//          }

//          收到最后一个分包并且接收缓存为空
          if (revFlag[0] && revCache.isEmpty()) {
            rev[0] = rev_builder.toString();
            myAcq.writeFile("sources/story.txt", rev[0]);
            System.out.println("data rev: " + rev[0]);
//            初始化参数
            base_rev[0] = 0;
            rev_builder.replace(0, rev_builder.length(), "");
            revFlag[0] = false;
            rev[0] = null;

          }

        });

      }
//      监听失败
      else {
        System.out.println("listen failed!!!");
      }
    });

  }

  private int virWinEnd(int base) {
    return base + windowSize;
  }

  private void sendMyPacket(myPacket packet, int port, String host) {
    DatagramSocket socket = vertx.createDatagramSocket();

    socket.send(packet.toBuffer(), port, host, asyncResult -> {
//      if(asyncResult.succeeded()){
//        System.out.println(packet + " send succeeded...");
//      }
//      else{
//        System.out.println(packet + " send failed...");
//      }
    });
    System.out.println();
    socket.close();
  }

}
