package classone;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 *
 * @author isWudn
 *
 * *******************************服务器端******************************
 * 1、可以并发地为多个用户服务
 * 2、显示用户通过客户端发送来的消息内容(包含头部和payload)
 * 3、能够模拟分组的丢失、能够模拟分组传输延迟
 * 4、将用户发送来的请求request在延迟一段随机选择的时间(小于1s)后返回给客户端，作为收到请求的响应reply
 * 5、通过如下命令行启动服务器：java PingServer port。port为PingServer的工作端口号
 *
 */
public class PingServer{
    private int initPort;//监听的端口号
    private DatagramSocket socket;//服务器的socket
    private DatagramPacket packet;//从客户端收到的packet
    private byte[] buf = new byte[1024];

    /**
     * 构造器
     * @paraminitPort服务器监听的端口号
     */
    public PingServer(int initPort) {
        this.initPort = initPort;
    }

    /**
     * 线程体
     */
    public void run() {
        System.out.println("编程实现基于UDP的PING (Java)服务器端");
        System.out.println("----------PING SERVER STARTED---------");
        try {
            //初始化socket，定义socket的端口号
            socket = new DatagramSocket(initPort);
        } catch (SocketException e) {
            //捕获到此异常一般是输入端口非法 或者 被占用
            System.out.println("Listening port" + initPort + "fail!");
            e.printStackTrace();
            //初始化端口号失败，终止程序
            System.exit(0);
        }

        //死循环，不断监听是否有报文请求
        while(true) {
            //获取客户端发来的报文
            packet = new DatagramPacket(buf,buf.length);
            try {
                //程序会停留在该语句，直到有新的请求连接产生
                socket.receive(packet);
            } catch (IOException e) {
                System.out.println("Request receive exception!");
                e.printStackTrace();
            }
            //启动线程
            ThreadServer server = new ThreadServer(socket,packet);
            server.start();
        }
    }

    public void destroy() {
        socket.close();//回收资源
    }

    public static void main(String[] args) {
        //初始化服务器
        PingServer ping  = new PingServer(Integer.valueOf(args[0]));
        //PingServer ping  = new PingServer(9999);
        ping.run();
    }
}

