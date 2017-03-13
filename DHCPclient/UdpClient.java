import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;
// import java.util.Arrays;
//import java.util.ArrayList;
//import java.util.List;
 
// RFC2731
 
public class UdpClient extends Thread {
    int port;            // port : 連接埠
    InetAddress server; // InetAddress 是 IP, 此處的 server 指的是伺服器 IP
    byte[] msg;            // 欲傳送的訊息，每個 UdpClient 只能傳送一個訊息。
    
    String mInterfaceName ;
    
    
    static boolean sGetIP = false ;
    
    static final byte DHCPDISCOVER = (byte) 0x01 ;
    static final byte DHCPOFFER    = (byte) 0x02 ;
    static final byte DHCPREQUEST  = (byte) 0x03 ;
    static final byte DHCPACK      = (byte) 0x05 ;
    
    
    
    
    static byte[] sMyIPAddr = new byte[] {
      (byte) 0x00,
      (byte) 0x00,
      (byte) 0x00,
      (byte) 0x00
    } ;
    
    
 
    public static void main(String args[]) throws Exception {
        
        byte[] a = new byte[]{
          (byte) 0x01,  //1 op
          (byte) 0x01,  //1 htype
          (byte) 0x06,  //1 hlen
          (byte) 0x00,  //1 hops
          
          (byte) 0x3e,
          (byte) 0xdb,
          (byte) 0xb8,
          (byte) 0x36, //4 xid
          
          (byte) 0x00, 
          (byte) 0x00, //2 secs
          
          (byte) 0x00,
          (byte) 0x00,//2 flag
        } ;
        
        a = BytePadding( a, 16 ) ; // add 4 ip address (1 ipaddress is 4 byte )
        
        byte[] macAddr = new byte[]{ (byte) 0x1C,
                                     (byte) 0x7e,
                                     (byte) 0xe5,
                                     (byte) 0xca,
                                     (byte) 0x4b,
                                     (byte) 0x2d } ;
        
        
        a = ByteAdd( a , macAddr ) ;
        
        a = BytePadding( a, 10 ) ; // in DHCP mac addr is 16byte......so need 10 0x00  to padding addr
        
        a = BytePadding( a, 64 ) ; // server host name ,in DHCP discover msg this field is null
        
        a = BytePadding( a, 128 ) ; // Boot file name ,in DHCP discover msg this field is null
        
        byte[] magicCookie = new byte[]{ (byte) 0x63,
                                         (byte) 0x82,
                                         (byte) 0x53,
                                         (byte) 0x63 } ; // it means this packet is DHCP msg
        
        /*
        DHCP clients and servers both construct DHCP messages by filling in
        fields in the fixed format section of the message and appending
        tagged data items in the variable length option area.  The options
        area includes first a four-octet 'magic cookie' 
        */
        
        a = ByteAdd( a , magicCookie ) ; //
        
        
        byte[] DHCPDISCOVERoption = new byte[]{ (byte) 0x35,// DHCP MSG type
                                                (byte) 0x01,// length 1
                                                (byte) 0x01} ; //value 1 *discover
                                                
        
        a = ByteAdd( a , DHCPDISCOVERoption ) ; //
        
        
        
        
        byte[] endOption = new byte[]{  ( byte ) 0xff } ;
        
        
        
        a = ByteAdd( a , endOption ) ; //
        

        
        a = BytePadding( a , 8 ) ;
        
        
        byte[] desAddr = new byte[] { (byte) 0xff,
                                         (byte) 0xff,
                                         (byte) 0xff,
                                         (byte) 0xff } ;
        /*                                 
        byte[] sourceAddrV = new byte[] { (byte) 0xC0,
                                          (byte) 0x64,
                                          (byte) 0x00,
                                          (byte) 0x64 } ;
                                          
        byte[] sourceAddrL = new byte[] { (byte) 0x7F,
                                          (byte) 0x00,
                                          (byte) 0x00,
                                          (byte) 0x1 } ;
        */
        
        
        
        
        int i = 0 ;
        
        
        Enumeration<NetworkInterface> enumeration= NetworkInterface.getNetworkInterfaces(); 
        i = 0 ; 
        while(enumeration.hasMoreElements())
        { 
          
          
          NetworkInterface networkInterface=enumeration.nextElement(); 
          if ( networkInterface.getHardwareAddress() != null )
          {
            System.out.println( networkInterface); 
            
            Enumeration <InetAddress> enu1 = networkInterface.getInetAddresses();
            
            while ( enu1.hasMoreElements() ) {
              InetAddress inetAddr = enu1.nextElement();
              System.out.println( inetAddr ); 
            } // while 
            
            
            System.out.println( "" ); 
          } // if
          
          
          i ++ ;
        } // while 
        
        
        
        
        System.out.println("Enter the interface address");
        Scanner scanner = new Scanner(System.in);
        String interfaceName = scanner.next();
        
        
        
        i = 0 ;
        while ( true ) {

            UdpClient client = new UdpClient( desAddr, 67, a , interfaceName );
            
            client.run(); // 啟動 UdpClient 開始傳送。
            // DHCPClientReceiver DHCPMSGReceiver = new DHCPClientReceiver() ;
            i ++ ;
            
            
            if ( i == 200 ) break ;
            if ( sGetIP ) break ;
        }
        
        
        
    }
 
    public UdpClient( byte[] pServer, int pPort, byte[] pMsg , String name ) throws Exception {
        port = pPort;                             // 設定連接埠
        
        // server = InetAddress.getByName(pServer); // 將伺服器網址轉換為 IP。
        server = InetAddress.getByAddress( pServer ); 
        
        msg = pMsg;                                 // 設定傳送訊息。
        
        mInterfaceName = name ;
    }
 
    public void run() {
      DatagramSocket socket = null;
      try {
        
        
        // Thread.sleep(10); // 發太快沒屁用 = =
        // byte buffer[] = msg.getBytes();                 // 將訊息字串 msg 轉換為位元串。
        System.out.println( "packet is send") ;
        // 封裝該位元串成為封包 DatagramPacket，同時指定傳送對象。
        DatagramPacket packet = new DatagramPacket(msg, msg.length, server, port); 
        socket = new DatagramSocket( 68 ,InetAddress.getByName(mInterfaceName) );    // 建立傳送的 UDP Socket。 指定SOURCEPORT為68
        socket.setBroadcast(true);  
        socket.send(packet);                             // 傳送
        socket.setSoTimeout(500); // 設定太久就關掉這個SOCKET
        
        byte[] getBuf = new byte[1024];
      
        
        DatagramPacket getPacket = new DatagramPacket(getBuf,getBuf.length);
      
        
        socket.receive(getPacket);
      
        
        // String backMes = new String(getBuf,0,getPacket.getLength());
        
        byte DHCPMsgType = GetDHCPType( getBuf ) ;
        
        
        if ( DHCPMsgType == DHCPOFFER ) 
        {
           SendDHCPREQUEST( socket, getBuf , getBuf.length ) ;
           
        } 
        else if ( DHCPMsgType == DHCPACK  ) {
                        byte[] yourAddr = GetYourAddress( getBuf);
                        System.out.println( "YA I have IP!!!!" );
                        
                        
                        System.out.println( bytesToInt( yourAddr[0] ) + "." + 
                                            bytesToInt( yourAddr[1] ) + "." + 
                                            bytesToInt( yourAddr[2] ) + "." + 
                                            bytesToInt( yourAddr[3] ) ) ;
                        
                        
                        
                        
                        
                        // GetYourAddress( getBuf);
                        sGetIP = true ;
        }
        else { 
                        System.out.println( "I don't care!" ); 
        } ; // else 
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        socket.close();                                 // 關閉 UDP socket.
      } catch ( Exception e ){
      }
      finally { 
        if ( socket!= null ) socket.close() ; 
     }    // 用同一個SOURCE PORT傳資料 配上THREAD 
          // 有可能會發生THREAD互搶PORT的情況而產生ERROR
    } // UdpClient()
    
    
    byte[] GetYourAddress( byte[] buffer ) {
        
        byte[] xid = new byte[4] ;
        
        xid[0] = buffer[16] ;
        xid[1] = buffer[17] ;
        xid[2] = buffer[18] ;
        xid[3] = buffer[19] ;
        
        return xid ;
    } // GetYourAddress()
    
    byte GetDHCPType( byte[] buffer ) {
        
        
/*
   0                   1                   2                   3
   0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1 2 3 4 5 6 7 8 9 0 1
   +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
   |     op (1)    |   htype (1)   |   hlen (1)    |   hops (1)    |
   +---------------+---------------+---------------+---------------+
   |                            xid (4)                            |
   +-------------------------------+-------------------------------+
   |           secs (2)            |           flags (2)           |
   +-------------------------------+-------------------------------+
   |                          ciaddr  (4)                          |
   +---------------------------------------------------------------+
   |                          yiaddr  (4)                          |
   +---------------------------------------------------------------+
   |                          siaddr  (4)                          |
   +---------------------------------------------------------------+
   |                          giaddr  (4)                          |
   +---------------------------------------------------------------+
   |                                                               |
   |                          chaddr  (16)                         |
   |                                                               |
   |                                                               |
   +---------------------------------------------------------------+
   |                                                               |
   |                          sname   (64)                         |
   +---------------------------------------------------------------+
   |                                                               |
   |                          file    (128)                        |
   +---------------------------------------------------------------+
   |                                                               |
   |                          options (variable)                   | type:53 length:1 type:1
   +---------------------------------------------------------------+
*/
        
        return  buffer[242];
    } // GetDHCPXid()
    
    
    
    
    void SendDHCPREQUEST( DatagramSocket ds,byte[] buffer , int length ) {
        try{
          System.out.print( " DHCPREQUEST" );
                        
          byte[] sendBuffer = Arrays.copyOf( buffer, 240 );
          
          sendBuffer[0] = (byte) 0x01 ; // op client reply is  0x01
          
          byte[] offerAddr = new byte[]{ (byte) 0x00, 
                                         (byte) 0x00,
                                         (byte) 0x00,
                                         (byte) 0x00 } ;
          
          sendBuffer = ByteReplace( sendBuffer , offerAddr, 16) ;
          
          
          
          byte[] DHCPOFFERMSG = new byte[]{ (byte) 0x35,
                                            (byte) 0x01,
                                            (byte) 0x03,
                                          } ;
          
          
          sendBuffer = ByteAdd( sendBuffer , DHCPOFFERMSG ) ;
          
          sendBuffer = ByteAdd( sendBuffer , new byte[]{ (byte) 0xff } ) ;
          
          sendBuffer = BytePadding( sendBuffer , 8 ) ;
                        
          
          
          
          
                        
          DatagramPacket sendPacket = 
                       new DatagramPacket( 
                                            sendBuffer,
                                            sendBuffer.length,
                                            InetAddress.getByName("255.255.255.255"),
                                            67
                                          );
                        
         ds.send( sendPacket ) ;                
         System.out.println( " is send" );
       }catch(Exception e ) {
        
          System.out.println( " ERROR!!!!!!" );
        
       }
    } // void SendDHCPREQUEST( DatagramSocket ds,byte[] buffer , int length ) {
    
    
  public static int bytesToInt(byte[] bytes) throws Exception {
    

    int num = 0;
    
    try{
        
    num = bytes[0] & 0xFF;

    num |= ((bytes[1] << 8) & 0xFF00);

    num |= ((bytes[2] << 16) & 0xFF0000);

    num |= ((bytes[3] << 24) & 0xFF000000);
    }catch( Exception e) {
    }
    

    return num;

  }
  
  public static int bytesToInt(byte bytes) throws Exception {
    

    int num = 0;
    
    try{
        
    num += bytes & 0xFF;

    }catch( Exception e) {
    }
    

    return num;

  }
    
    
    public static byte[] ByteAdd(byte[] a , byte[] b ) {

      byte[] bt = new byte[ (a.length +  b.length) ];

      for( int i = 0 ; i < a.length ; i++ ) {
        bt[i] = a[i] ;
      } // for 

      for( int i = 0 ; i < b.length ; i++ ) {
        bt[ (i + a.length) ] = b[i] ;
      } // for 

      return bt;

  }
  
  public static byte[] BytePadding( byte[]a , int size ) {
        
        byte[] bt = new byte[ a.length + size ] ;
        
        for( int i = 0 ; i < a.length ; i++ ) {
          bt[i] = a[i] ;
        } // for
        
        for( int i = a.length ; i < size ; i++ ) {
          bt[i] = 0x00 ;
        } // for
        
        return bt;
  } 
  
  public static byte[] intToByte(int i) {
        

      byte[] bt = new byte[1];
      
      bt[0] = (byte) (0xff & i);


      return bt;
      
      
      


  }
  
  
  public static byte[] ByteReplace( byte[] a , byte[] b, int index ) {
        
        
        
        for( int i = 0 ; i < b.length ; i ++ ) {
          a[ index+i ] = b[i] ;
        } // for 
        
        return a ;
        
    } // public static byte[] ByteReplace()
    
} // class UDPclient





