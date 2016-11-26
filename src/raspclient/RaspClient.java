package raspclient;

import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.StringTokenizer;

class RaspClient
{
    public static Socket connection;
    public static String tosend, receive, servername = "10.100.1.89", key = "0", signcode, datastart, dataend, position;
    public static int port = 2000, timer = 5000;
    public static byte[] res, length = new byte[4];
    public static InputStream in;
    public static InputStreamReader input;
    public static BufferedReader sIN;
    public static PrintWriter out;
    public static DataInputStream dIn;
    public static DataOutputStream dOut;
    
    
    public static void main(String[] args)
    {
       initialize();
    }
    
    public static String[] initfilereading()
    //key;signcode;datastart;dataend;coords
    {
        File file = new File("clientconf.txt");
        String line = "";
        try 
        {
            FileReader reader = new FileReader(file);
            BufferedReader buffReader = new BufferedReader(reader);
            String s;
            while((s = buffReader.readLine()) != null)
            {
                line = s;
            }
        }
        catch(IOException e)
        {
            System.err.println(e);
        }
        
        String[] art1 = tokened(line);
        
        return art1;
    }
    
    public static String[] tokened(String tosep)
    {
        String tmp = tosep;
        StringTokenizer st = new StringTokenizer(tmp, ";");
        String[] out = new String[3];
        int i = 0;
        
        while (st.hasMoreElements())
        {
            out[i] = (String) st.nextElement();
            i++;
        }
        return out;
    }
    
    public static void first()
    {
        tosend = "FRST";
        try
        {
            coordsend();
        }
        catch(Exception e)
        {
            
        }
    }
    
    public static void update()
    {
        tosend = "UPD" + key;
        try
        {
            send();
            assign(tokened(recv()));
            Thread.sleep(timer);
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
    }
    
    public static void coordsend()
    {
        /*Per Piras:
        dopo aver preso le coords, fai
        tosend = (coords); //format: LONGITUDINE-LATITUDINE
        send();
        */
    }
    
    public static void send()
    {
        try 
        {
            dIn = new DataInputStream(connection.getInputStream());
            dOut = new DataOutputStream(connection.getOutputStream());
            ByteBuffer buffer = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder());
            buffer.putInt(tosend.length());

            dOut.write(buffer.array());
            dOut.flush();
            dOut.writeBytes(tosend);
            dOut.flush();
        } 
	catch (IOException e) 
	{
            e.printStackTrace();
        }
    }
    
    public static String recv()
    {
        String strRes = "";
        try 
        {
            /*in = connection.getInputStream();
            input = new InputStreamReader(in);
            sIN = new BufferedReader(input);
            receive = sIN.readLine();*/
            
            dIn.read(length);

            ByteBuffer wrapped = ByteBuffer.wrap(length);
            wrapped.order(ByteOrder.LITTLE_ENDIAN);// big-endian by default
            int num = wrapped.getInt();
            res = new byte[num];
            dIn.read(res);
            strRes = new String(res, "ASCII");
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
        return strRes;
    }
    
    public static void initialize() 
    {
        String[] cat = initfilereading();
        
        assign(cat);
        
        try
        {
            connection = new Socket(servername, port);
            tosend = "SGN";
            send();
            //System.out.println("Connected to the server.");
            
            first();
            while(true)
            {
                update();
            }
        }
        catch (UnknownHostException e)
        {
            System.err.println(e);
        }
        catch (IOException e)
        {
            System.err.println(e);
        }
    }
    
    /*public static String chainfive(String[] input)
    {
        return input[0] + input[1] + input[2] + input[3] + input[4];
    }*/
    
    public static void assign(String[] input)
    {
        key = input[0];
        signcode = input[1];
        datastart = input[2];
        dataend = input[3];
        position = input[4];
    }
}