package classone;

import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author isWudn
 *
 * ****************************客户端***********************************
 * 1、启动后发送10个request。每发送一个request后，最多等待1秒便接收PingServer返回的reply消息。
 * 	    如果在该时间内没有收到服务器的reply，则认为该请求或对该请求的reply已经丢失；
 * 	    在收到reply后立即发送下一个request。
 *
 * 2、请求消息的payload中至少包含关键字PingUDP、序号、时间戳等内容。
 * 	    如：PingUDP SequenceNumber TimeStamp CRLF
 * 	    其中：CRLF表示回车换行符(0X0D0A)；TimeStamp为发送该消息的机器时间。
 *
 * 3、为每个请求计算折返时间(RTT)，统计10个请求的平均RTT、最大/小RTT。
 *
 * 4、通过如下命令行启动：java PingClient host port。
 * 	  host为PingServer所在的主机地址；port为PingServer的工作端口号
 *
 */

public class PingClient extends Thread{
    //客户端socket
    private DatagramSocket client;
    //服务器的IP地址
    private InetAddress hostAddress;
    //服务器的应用进程端口号
    private int port;
    //定义并初始化接收到的响应报文的个数
    private int replyNum = 0;
    //定义并初始化最小往返时间、最大往返时间、平均往返时间
    private long minRtt = 0, maxRtt = 0, averRtt = 0,sumRtt = 0;
    //每一个请求对应的rtt，默认初始化为0
    private long[] rtt = new long[10];

    /**
     * 构造器
     * @paramhost服务器地址
     * @paramport服务器端口
     */
    public PingClient(String host, int port) {
        //有效端口的范围是0~65535
        if(port < 0 || port > 65535) {
            System.out.println("Invalid port number!");
            //如果端口号违法，终止程序
            System.exit(0);
        }
        this.port = port;
        try {
            //初始化客户端实例，将该实例绑定到本机默认的IP地址，本机所有可用端口中随机选择的某个端口
            client = new DatagramSocket();
            //根据服务器主机名称确定服务器的IP地址
            hostAddress = InetAddress.getByName(host);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.out.println("Invalid host name!");
            e.printStackTrace();
        }
    }

    /**
     * 线程体
     */
    public void run() {
        //定义时间戳格式
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-ddhh:mm:ss.SS");
        System.out.println("Pinging " + hostAddress + ":");
        //模拟发送10条请求
        for(int i=0; i<10; i++) {
            //数据包装顺序：字符串-->字节数组-->packet报文
            /************************生成发送报文***************************/
            //发送报文前的时间
            Date sendTime = new Date();
            //请求数据
            String outMessage = "head:request " + i +"\n"
                    + "payload:PingUDP SequenceNUmber:" + i
                    + " TimeStamp:" + sdf.format(sendTime);
            //将请求数据放进缓冲区内
            byte[] buffer = outMessage.getBytes();
            //生成发送报文实例
            DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,hostAddress,port);

            /*************************生成接收报文******************************/
            byte[] buf = new byte[buffer.length];
            DatagramPacket recievePacket = new DatagramPacket(buf,buf.length);

            /*****************************************************************/
            //接收到的响应信息
            String recieve = null;
            try {
                //发送到服务器端
                client.send(sendPacket);
                //接收响应报文
                //client.setSoTimeout(1000);//设置超时时间为1秒
                client.receive(recievePacket);
                recieve = new String(recievePacket.getData());
                //记录接收后的时间
                Date recieveTime = new Date();
                //计算往返时间
                rtt[i] = recieveTime.getTime()-sendTime.getTime();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //如果接收时间大于1000ms，则认为请求丢失或者对请求的回复丢失
            if(rtt[i]>1000) {
                recieve = "head:request "+ i +"\n"
                        + "Response message lost or Request timed out!";
            }else {
                recieve = recieve + "\n" + "rtt:" + rtt[i] +"ms";
            }
            System.out.println(recieve);
        }

        minRtt = rtt[0];
        for(int i=0;i<10;i++) {
            if(rtt[i] > 1000)  continue;//请求失败不计算往返时间
            replyNum++;
            //计算最小往返时间
            if(minRtt > rtt[i]) {
                minRtt = rtt[i];
            }
            //计算最大往返时间
            if(maxRtt < rtt[i]) {
                maxRtt = rtt[i];
            }
            //计算总往返时间
            sumRtt += rtt[i];
        }
        if(replyNum!=0) {
            //计算平均往返时间
            averRtt = sumRtt/replyNum;
            System.out.println("Ping statistics for " + hostAddress + ":");
            System.out.println("	Packets: Sent=10, Received=" + replyNum +", Lost=" + (10-replyNum));
            System.out.println("Approximate round trip times in milli-seconds:");
            System.out.println("	minRTT:" + minRtt + "ms, maxRTT:" + maxRtt + "ms, averRTT:" + averRtt + "ms");
        }else {
            System.out.println("Failed to send request! Unable to return message!");
        }
        //关闭线程
        client.close();
    }

    public static void main(String[] args) {
        PingClient clientThread = new PingClient(args[0], Integer.valueOf(args[1]));
        //PingClient clientThread = new PingClient("127.0.0.1", 9999);
        clientThread.start();
    }

}

