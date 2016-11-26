import java.sql.Timestamp;
import java.net.*;
import java.io.*;
import java.nio.*;
import java.util.StringTokenizer;

class RaspClient
{
    public static Socket connection;
    public static String tosend, receive, servername = "10.100.1.89", key = "0", signcode = "0", longitude = "0,0", latitude = "0,0", rightSide = "1";
    public static long datastart, dataend;
    public static int updatecounter = 1, port = 2000, timer = 2000;
    public static byte[] res, length = new byte[4];
    public static InputStream in;
    public static InputStreamReader input;
    public static BufferedReader sIN;
    public static PrintWriter out;
    public static DataInputStream dIn;
    public static DataOutputStream dOut;
    public static String Fname = "clientconf.txt";
    public static boolean close = false;
    
    
    public static void main(String[] args)
    {
        while(!close)
        {
            try
            {
                initialize();
            }
            catch (Exception e)
            {
                System.out.println(e);
            }
        }
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
            if((s = buffReader.readLine()) != null)
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
        String[] outp = new String[7];
        int i = 0;
        
        while (st.hasMoreElements())
        {
            outp[i] = (String) st.nextElement();
            i++;
        }
        return outp;
    }
    
    //send my values
    public static void update() throws Exception
    {
        tosend = "UPT";
        
        send();

        tosend = key + ";" + signcode + ";" + datastart + ";" + dataend + ";" + longitude + ";" + latitude + ";" + rightSide;
        send();
        System.out.println("Update: " + updatecounter);
        updatecounter++;
        Thread.sleep(timer);
    }
    
    public static void writeonfile(String rkey)
    {
        try 
        {
            String content = rkey + ";" + signcode + ";" + datastart + ";" + dataend + ";" + longitude + ";" + latitude + ";" + rightSide;
            
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
    
    public static void send() throws IOException
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
    
    public static String recv() throws Exception
    {
        String strRes = "";
        
        dIn.read(length);

        ByteBuffer wrapped = ByteBuffer.wrap(length);
        wrapped.order(ByteOrder.LITTLE_ENDIAN);// big-endian by default
        int num = wrapped.getInt();
        res = new byte[num];
        dIn.read(res);
        strRes = new String(res, "ASCII");
            
        return strRes;
    }
    
    public static void updateValues()
    {
        //initialize file
        String[] cat = initfilereading();
        
        assign(cat); //set values from my file
    }
    
    public static void initialize() throws Exception
    {
        updateValues();

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
                writeonfile(key);
                System.out.println("Received new key from the server.");
            }
            Timestamp endTime = new Timestamp(dataend);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            
            if(endTime.compareTo(now)>=0)
                close=true;
            
            while(!close)
            {
                update(); //send update
                updateValues();
            }
    }

    public static void assign(String[] input)
    {
        key = input[0];
        signcode = input[1];
        datastart = Long.parseLong(input[2]);
        dataend = Long.parseLong(input[3]);
        longitude = input[4];
        latitude = input[5];
        rightSide = input[6];
    }
}