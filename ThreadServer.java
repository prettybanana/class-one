package classone;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 *
 * @author isWudn
 *
 * *****************PingServer用来处理多用户请求的线程***********************
 * 2、显示用户通过客户端发送来的消息内容(包含头部和payload)
 * 3、能够模拟分组的丢失、能够模拟分组传输延迟
 * 4、将用户发送来的请求request在延迟一段随机选择的时间(小于1s)后返回给客户端，作为收到请求的响应reply
 *
 */
public class ThreadServer extends Thread{
    private DatagramSocket socket;//接收和发送数据报
    private DatagramPacket packet;//数据报

    /**
     * 构造器
     * @paramsocket服务器的socket
     * @parampacket客户端发送过来的packet
     */
    public ThreadServer(DatagramSocket socket, DatagramPacket packet) {
        //初始化socket和packet
        this.socket = socket;
        this.packet = packet;
    }

    public void run() {
        //定义延迟回送报文的随机时间，范围在0~1500之间，要比1000大才能延迟
        long randomTime = (long)(Math.random()*1500);

        /***********************接收报文****************************/
        String data = null;
        //如果随机时间大于1400，则认为分组丢失
        if(randomTime>1400) {
            data = "Receive message lost!";
            System.out.println(data);
        }else {
            //获取packet对象里封装的字符数组
            data = new String(packet.getData());
            System.out.println("Receiving a message:" + data);
        }
        /************************发送报文********************************/
        byte[] buffer = data.getBytes();
        //获取客户端地址
        InetAddress host = packet.getAddress();
        //获取客户端应用进程端口号
        int port = packet.getPort();
        //回送给客户端的报文
        DatagramPacket sendPacket = new DatagramPacket(buffer,buffer.length,host,port);
        //休眠0~1.5秒，模拟传输延迟
        try {
            sleep(randomTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            System.out.println("The server failed to send a reply message!");
            e.printStackTrace();
        }

    }
}


