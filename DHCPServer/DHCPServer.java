import java.io.*;
import java.net.*;
import java.util.*;
// import java.Array.*;
 
// 1. 本程式必須與 UdpClient.java 程式搭配執行，先執行本程式再執行 UdpClient。
// 2. 執行方法 : java UdpServer
 
public class DHCPServer extends Thread {
    int port;    // 連接埠
    String mInterfaceName ;
    
    
    
    static byte DHCPDISCOVER = (byte) 0x01 ;
    static byte DHCPOFFER    = (byte) 0x02 ;
    static byte DHCPREQUEST  = (byte) 0x03 ;
    static byte DHCPACK      = (byte) 0x05 ;
 
    public static void main(String args[]) throws Exception {
        
        Enumeration<NetworkInterface> enumeration= NetworkInterface.getNetworkInterfaces(); 
        int i = 0 ; 
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
        
        
        DHCPServer server = new DHCPServer(67 , interfaceName ); // 建立 UdpServer 伺服器物件。
        
        System.out.println( "Start DHCPserver..." ); 
        
        
        
        
            
        server.run();                           // 執行該伺服器。
    }
 
    public DHCPServer(int pPort,String name ) {
        
        port = pPort;                            // 設定連接埠。
        
        mInterfaceName = name ;
        
        // run();
    }
 
    public void run() {
        final int SIZE = 1024;                    
        byte buffer[] = new byte[SIZE];            // 設定訊息暫存區
        
        
        for (int count = 0; ; count++) {
            try {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            DatagramSocket socket = new DatagramSocket(67,InetAddress.getByName(mInterfaceName) );         // 設定接收的 UDP Socket.
            socket.receive(packet);                                    // 接收封包。
           
            
            try {
              // String msg = new String(buffer, 0, packet.getLength());    // 將接收訊息轉換為字串。
            
              // System.out.println( packet.getLength() +"" );                    // 印出接收到的訊息。
           
              HandleRequest( socket, buffer , packet.getLength() ) ;
            } finally {
                socket.close(); } // finally                                           // 關閉 UDP Socket.
            } catch ( Exception e ) {
            } // catch
        } // for
    } // run
    
    
    void HandleRequest( DatagramSocket ds,byte[] buffer , int length ) throws Exception {
        try {
                byte[] xid = GetDHCPXid( buffer ) ;
                byte DHCPMsgType = GetDHCPType( buffer ) ;
                
                
                System.out.print( Integer.toHexString( bytesToInt(xid) ) );
                // System.out.print( Integer.toHexString(  bytesToInt( new byte[]{ buffer[length -10]} )  ) );
                
                
                if ( DHCPMsgType == DHCPDISCOVER ) 
                {
                        SendDHCPOFFER( ds,buffer , length ) ;
                } // if DHCPMsgType == DHCPDISCOVER
                else if ( DHCPMsgType == DHCPREQUEST  ) {
                        SendDHCPACK( ds,buffer , length ) ;
                }
                else { 
                        System.out.print( "I don't care!" ); 
                } ; // else 
                
                
                // System.out.println("") ;
                
                
                
                // System.out.println( "receive = "+ new String( buffer ,0 , length ) );
        } catch ( Exception e ) {
        }
    } // HandleRequest
    
    
    byte[] GetDHCPXid( byte[] buffer ) {
        
        byte[] xid = new byte[4] ;
        
        xid[0] = buffer[4] ;
        xid[1] = buffer[5] ;
        xid[2] = buffer[6] ;
        xid[3] = buffer[7] ;
        
        return xid ;
    } // GetDHCPXid()
    
    
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
    
    
    
    void SendDHCPOFFER( DatagramSocket ds,byte[] buffer , int length ) {
        
        try{
          System.out.print( " DHCPOFFER" );
                        
          byte[] sendBuffer = Arrays.copyOf( buffer, 240 );
          
          sendBuffer[0] = (byte) 0x02 ; // op server reply is  0x02
          
          byte[] offerAddr = new byte[]{ (byte) 0xC0, 
                                         (byte) 0xA8,
                                         (byte) 0x02,
                                         (byte) 0x64 } ;
          
          sendBuffer = ByteReplace( sendBuffer , offerAddr, 16) ;
          
          
          
          byte[] DHCPOFFERMSG = new byte[]{ (byte) 0x35,
                                            (byte) 0x01,
                                            (byte) 0x02,
                                          } ;
          
          
          sendBuffer = ByteAdd( sendBuffer , DHCPOFFERMSG ) ;
          
          sendBuffer = ByteAdd( sendBuffer , new byte[]{ (byte) 0xff } ) ;
          
          sendBuffer = BytePadding( sendBuffer , 8 ) ;
                        
          
          
          
          
                        
          DatagramPacket sendPacket = 
                       new DatagramPacket( 
                                            sendBuffer,
                                            sendBuffer.length,
                                            InetAddress.getByName("255.255.255.255"),
                                            68
                                          );
                        
         ds.send( sendPacket ) ;                
         System.out.println( " is send" );
       }catch(Exception e ) {
        
          System.out.println( " ERROR!!!!!!" );
        
       }
       
    } // SendDHCPOFFER() 
    
    void SendDHCPACK( DatagramSocket ds,byte[] buffer , int length ) {
        
        try{
          System.out.print( " DHCPACK" );
                        
          byte[] sendBuffer = Arrays.copyOf( buffer, 240 );
          
          sendBuffer[0] = (byte) 0x02 ; // op server reply is  0x02
          
          byte[] offerAddr = new byte[]{ (byte) 0xC0, 
                                         (byte) 0xA8,
                                         (byte) 0x02,
                                         (byte) 0x64 } ;
          
          sendBuffer = ByteReplace( sendBuffer , offerAddr, 16) ;
          
          
          
          byte[] DHCPOFFERMSG = new byte[]{ (byte) 0x35,
                                            (byte) 0x01,
                                            (byte) 0x05,
                                          } ;
          
          
          sendBuffer = ByteAdd( sendBuffer , DHCPOFFERMSG ) ;
          
          sendBuffer = ByteAdd( sendBuffer , new byte[]{ (byte) 0xff } ) ;
          
          sendBuffer = BytePadding( sendBuffer , 8 ) ;
                        
          
          
          
          
                        
          DatagramPacket sendPacket = 
                       new DatagramPacket( 
                                            sendBuffer,
                                            sendBuffer.length,
                                            InetAddress.getByName("255.255.255.255"),
                                            68
                                          );
                        
         ds.send( sendPacket ) ;                
         System.out.println( " is send!!!!!!!!!!!!!" );
         
       }catch(Exception e ) {
          System.out.println( "ACK  ERROR!!!!!!" );
       }
       
    } // SendDHCPACK() 
    
    
    public static byte[] ByteReplace( byte[] a , byte[] b, int index ) {
        
        
        
        for( int i = 0 ; i < b.length ; i ++ ) {
          a[ index+i ] = b[i] ;
        } // for 
        
        return a ;
        
    } // public static byte[] ByteReplace()
    
    
    
    
    
    
    
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
    
}




