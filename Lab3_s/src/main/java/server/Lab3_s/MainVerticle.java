package server.Lab3_s;

import ADT.myPacket;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.net.SocketAddress;

import java.util.HashMap;
import java.util.Map;

public class MainVerticle extends AbstractVerticle {

  private final int dataSize = 100;
  private final int windowSize = 3;
  private final long interval = 100;



  @Override
  public void start(Promise<Void> startPromise) {
    final Map<Integer, myPacket> sendCache = new HashMap<>();
    final Map<Integer, Long> timers = new HashMap<>();
    StringBuilder rev_builder = new StringBuilder();
    final String[] data_send = {null};
    final String[] rev = {null};
    final boolean[] sendFlag = {false};
    final int[] base = {0};
    final int[] nextSeqNum = {0};

//    server
    DatagramSocket socket = vertx.createDatagramSocket();

    socket.listen(8888, "127.0.0.1", asyncResult -> {
//      监听成功
      if(asyncResult.succeeded()){
        System.out.println("listening...");

//        接收到分组
        socket.handler(packet_rev -> {
//          存放接收到的分组
          myPacket packet = new myPacket(packet_rev.data());
          SocketAddress sender = packet_rev.sender();

//          设置目的主机端口号与IP地址
          int desPort = 8080;
          String desHost = sender.host();

          int seq = 0;

//            收到的是数据分组
          if (!packet.isAck()) {
//            添加到接收缓存string builder中
            rev_builder.append(packet.getData());

//            最后一个分组，进行处理
            if (!packet.hasMore()){
//              存放收到的数据
              rev[0] = rev_builder.toString();

//              打印收到的数据
              System.out.println("rev data: " + rev[0] + '\n');
//              根据接收到的内容进行相应的处理
//              simulate 设置发送story
              if (rev[0].equals("story")){
//                加入发送数据缓存中
                data_send[0] = mySources.readFile("sources/story.txt");
                sendFlag[0] = true;
              }
            }
          }
//          收到ACK
          else{
//            获得ACK确认的序列号
            seq = packet.getSeq();
            System.out.println("ACK " + seq + " ##########");
          }


//          发送数据时
          if(sendFlag[0]){

//            如果发送缓存不为空
            if (!sendCache.isEmpty()){
//              更新base和缓存窗口、取消计时器

//              base 收到ACK
              if (seq == base[0]){
                sendCache.remove(base[0]);
//                取消计时器
                vertx.cancelTimer(timers.get(seq));
                timers.remove(seq);

                base[0] ++;
//              如果窗口中的最小序列号的分组收到过ack
                while (!sendCache.containsKey(base[0]) && base[0] < nextSeqNum[0]){//注意应该是  小于  还是小于等于*****************
                  //              取消计时器
                  vertx.cancelTimer(timers.get(base[0]));
                  timers.remove(base[0]);
                  base[0] ++;
                }
              }
//              收到已确认包的ACK
              else if (seq < base[0]){
                ;
              }
//              非base号接收到ACK
              else{
                sendCache.remove(seq);
              }
            }

//          发送内容并存入缓存
            int virtualEnd = virWinEnd(base[0]);
            for (; nextSeqNum[0]*dataSize < data_send[0].length() && nextSeqNum[0] < virtualEnd; nextSeqNum[0]++){
//              构造发送分组
              String sub = data_send[0].substring(nextSeqNum[0]*dataSize, Math.min(data_send[0].length(), dataSize*(nextSeqNum[0]+1)));
              boolean moreFlag = dataSize*(nextSeqNum[0]+1) < data_send[0].length();
              myPacket sendPacket = new myPacket(nextSeqNum[0], sub, moreFlag);
              int seq_send = nextSeqNum[0];
              System.out.println("send: " + seq_send + " ++++++++++");
//              发送分组并存入缓存
              sendMyPacket(sendPacket, desPort, desHost);
              sendCache.put(seq_send, sendPacket);
//              设置计时器
              timers.put(seq_send, vertx.setPeriodic(interval, id -> {
                System.out.println("seq " + seq_send + ": OVERTIME!!! Resend...");
                sendMyPacket(sendCache.get(seq_send), desPort, desHost);
              }));

            }

//            发送完成
            if ((nextSeqNum[0]+1)*dataSize > data_send[0].length() && sendCache.isEmpty()){
              System.out.println("发送完成");
//              初始化参数
              rev_builder.replace(0, rev_builder.length(), "");
              data_send[0] = null;
              base[0] = 0;
              nextSeqNum[0] = 0;
              sendFlag[0] = false;
            }

          }

        });

      }
//      监听失败
      else {
        System.out.println("listen failed!!!");
      }

    });

  }

  private int virWinEnd(int base){
    return base+windowSize;
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
