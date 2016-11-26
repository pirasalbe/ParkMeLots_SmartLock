package raspclient;

import java.sql.Timestamp;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.StringTokenizer;

class RaspClient
{
    public static Socket connection;
    public static String tosend, receive, servername = "10.100.1.89", key = "0", signcode = "0", position = "0,0-0,0";
    public static long  datastart, dataend; 
    public static int updatecounter = 1, port = 2000, timer = 5000;
    public static byte[] res, length = new byte[4];
    public static InputStream in;
    public static InputStreamReader input;
    public static BufferedReader sIN;
    public static PrintWriter out;
    public static DataInputStream dIn;
    public static DataOutputStream dOut;
    public static String Fname = "clientconf.txt";
    
    
    public static void main(String[] args)
    {
       initialize();
    }
    
    public static String[] initfilereading()
    //key;signcode;datastart;dataend;coords
    {
        String line = "";
        try
        {
            File file = new File(Fname);
            if (!file.exists())
            {
                file.createNewFile();
                writeonfile("0"); //set default value
            }
            
            //read the file
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
        
        String[] art1 = tokened(line); //splits string
        
        return art1;
    }
    
    //divides string
    public static String[] tokened(String tosep)
    {
        String tmp = tosep;
        StringTokenizer st = new StringTokenizer(tmp, ";");
        String[] out = new String[5];
        int i = 0;
        
        while (st.hasMoreElements())
        {
            out[i] = (String) st.nextElement();
            i++;
        }
        return out;
    }
    
    //send my values
    public static void update()
    {
        tosend = "UPT";
        try
        {
            send();
            
            tosend = key + signcode + datastart + dataend + position;
            send();
            System.out.println("Update: " + updatecounter);
            updatecounter++;
            Thread.sleep(timer);
        }
        catch(Exception e)
        {
            System.err.println(e);
        }
    }
    
    public static void writeonfile(String rkey)
    {
        try 
        {
            String content = rkey + ";" + signcode + ";" + datastart + ";" + dataend + ";" + position;
            
            File file = new File(Fname);
            
            //write on file 
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
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
    
    public static void updateValues()
    {
        //initialize file
        String[] cat = initfilereading();
        
        assign(cat); //set values from my file
    }
    
    public static void initialize() 
    {
        updateValues();
        
        try
        {
            //socket
            connection = new Socket(servername, port);
            System.out.println("Connected to the server.");
            
            //initialize server
            tosend = "SGN";
            send();
            
            //key generator
            tosend = key;
            send();
            
            if (key.equals("0"))
            {
                key = recv(); //receive from server
            }
            System.out.println("Received new key from the server.");
            //first();
            Timestamp endTime = new Timestamp(dataend);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            while(endTime.compareTo(now)>=0)
            {
                updateValues();
                update(); //send update
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

    public static void assign(String[] input)
    {
        key = input[0];
        signcode = input[1];
        datastart = Long.parseLong(input[2]);
        dataend = Long.parseLong(input[3]);
        position = input[4];
    }
}