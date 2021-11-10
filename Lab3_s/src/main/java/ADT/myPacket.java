package ADT;

import io.vertx.core.buffer.Buffer;

import java.io.*;

public class myPacket implements Serializable {
  private static final long serialVersionUID= -8093398719244252794L;
  private final int seq;
  private final String data;
  private final boolean ack;
  private final boolean more;

  //  生成确认报文
  public myPacket(int seq){
    this.seq = seq;
    this.data = null;
    this.ack = true;
    this.more = false;
  }

  //  生成数据分组
  public myPacket(int seq, String data, boolean more){
    this.seq = seq;
    this.data = data;
    this.ack = false;
    this.more = more;
  }

  public myPacket(Buffer buffer){
    byte[] bytes = buffer.getBytes();
    ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
    ObjectInputStream obInS;
    myPacket packet = null;
    try {
      obInS = new ObjectInputStream(bais);
      packet = (myPacket) obInS.readObject();
      bais.close();
      obInS.close();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
    assert packet != null;
    this.seq = packet.getSeq();
    this.data = packet.getData();
    this.ack = packet.isAck();
    this.more = packet.hasMore();
  }

  //  判断是否是确认报文
  public boolean isAck(){
    return ack;
  }

  //  获得分组序列
  public Integer getSeq() {
    return seq;
  }

  //  获得分组中的数据
  public String getData() {
    return data;
  }

  public boolean hasMore() {
    return more;
  }

  @Override
  public String toString() {
    return "myPacket{" +
      "seq=" + seq +
      ", data='" + data + '\'' +
      ", ack=" + ack +
      ", more=" + more +
      '}';
  }

  public Buffer toBuffer() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream obOutS;
    byte[] bytes = null;
    try {
      obOutS = new ObjectOutputStream(baos);
      obOutS.writeObject(this);
      bytes = baos.toByteArray();
      obOutS.close();
      baos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    assert bytes != null;
    return Buffer.buffer(bytes);
  }
}
