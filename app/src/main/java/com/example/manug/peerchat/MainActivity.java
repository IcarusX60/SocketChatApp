package com.example.manug.peerchat;

import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {
    String message="";
    EditText messageTextView;
    EditText ip;
    EditText port;
    TextView responseTextView;
    static MessageAdapter mAdapter;
    ListView messageList;
    ArrayList<Message> messageArray;
    EditText portText;
    String ipAddress;
    int myport;
    Socket connectionSocket;
    String flag = null;
    String colorCode =  "#*green";
    SendReceive sendReceive;
    static final int MESSAGE_READ=1;

    Boolean setColor = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        messageList = findViewById(R.id.message_list);
        messageArray = new ArrayList<Message>();
        portText = findViewById(R.id.myPortEditText);
        mAdapter = new MessageAdapter(this, messageArray);
        messageList.setAdapter(mAdapter);
        messageTextView=findViewById(R.id.messageEditText);
        ip=findViewById(R.id.ipEditText);
        port=findViewById(R.id.portEditText);

        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        ipAddress = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
    }

    public void startServer(View view) {
        myport = Integer.parseInt(portText.getText().toString());
        Server s = new Server(messageList, messageArray, myport);
        s.start();
        ip.setText("IP Address: " +ipAddress);
        port.setText("Port: " +myport);
        flag="notmepty";
    }
    public void sendResponse(View view){
       if(flag == null){
           Client c =new Client(messageList,messageArray);
           c.start();
       }
       else {
           toClient tc = new toClient();
           tc.execute();

       }
    }
    public void stcStart(){

    }
    public void setView(String s){
        String str=responseTextView.getText().toString();
        str=str+"\nReceived: "+s;
        responseTextView.setText(str);
    }
    public class Client extends Thread{
        String msg,text;
        ListView messageList;
        ArrayList<Message> messageArray;

        public Client(ListView messageList, ArrayList<Message> messageArray) {
            this.messageArray = messageArray;
            this.messageList = messageList;
        }

        @Override
        public void run(){
            String ipadd = ip.getText().toString();
            int portr = Integer.parseInt(port.getText().toString());
            try {
                Socket clientSocket = new Socket(ipadd, portr);
                toServer ts = new toServer(clientSocket);
                ts.execute();

                HandleServer h = new HandleServer(clientSocket) ;
                h.execute(clientSocket);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        public class HandleServer extends AsyncTask<Socket,Void,String>{
            String sentence;
            Socket clientSocket;

            public HandleServer(Socket clientSocket){
                this.clientSocket = clientSocket;
            }
            @Override
            protected String doInBackground(Socket... sockets) {
                try {
                    BufferedReader input = new BufferedReader(new InputStreamReader(sockets[0].getInputStream()));
                    sentence = input.readLine();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                return sentence ;
            }
            protected void onPostExecute(String result) {
                //if(result!=null)
                {messageArray.add(new Message("Received: " + result, 1));
                messageList.setAdapter(mAdapter);}
            }
        }
        public class toServer extends AsyncTask<Socket,Void,String>{
            String msg;
            Socket connectionSocket;

            public toServer(Socket connectionSocket) {
                this.connectionSocket = connectionSocket;
            }

            @Override
            protected String doInBackground(Socket... sockets) {
                try {
                    msg = messageTextView.getText().toString();
                    OutputStream outToServer =connectionSocket.getOutputStream();
                    PrintWriter output = new PrintWriter(outToServer);
                    output.println(msg);
                    output.flush();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return msg;
            }

            protected void onPostExecute(String result) {
                messageArray.add(new Message("Sent: " + result, 0));
                messageList.setAdapter(mAdapter);
            }
        }


    }
    public class Server extends Thread {
        ListView messageList;
        ArrayList<Message> messageArray;
        int port;
        public Server(ListView messageList, ArrayList<Message> messageArray, int port) {
            this.messageArray = messageArray;
            this.messageList = messageList;
            this.port = port;
        }
        ServerSocket welcomeSocket=null;
        @Override
        public void run(){
            try{
                welcomeSocket=new ServerSocket(port);
                while (true){
                    connectionSocket=welcomeSocket.accept();
                    HandleClient c= new HandleClient();
                    c.execute(connectionSocket);
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        public class HandleClient extends AsyncTask<Socket,Void,String>{
            String sentence;
            @Override
            protected String doInBackground(Socket... sockets) {
                try {
                    BufferedReader input = new BufferedReader(new InputStreamReader(sockets[0].getInputStream()));
                    sentence = input.readLine();
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                return sentence ;
            }
            protected void onPostExecute(String result) {
                messageArray.add(new Message("Received: " + result, 1));
                messageList.setAdapter(mAdapter);
            }
        }
    }

    public class toClient extends AsyncTask<Void,Void,String>{
        String msg;


        @Override
        protected String doInBackground(Void... voids) {
            try {
                msg = messageTextView.getText().toString();
                OutputStream outToClient =connectionSocket.getOutputStream();
                PrintWriter output = new PrintWriter(outToClient);
                output.println(msg);
                output.flush();
                connectionSocket.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return msg;
        }

        protected void onPostExecute(String result) {
            messageArray.add(new Message("Sent:) " + result, 0));
            messageList.setAdapter(mAdapter);
        }
    }

















    public void onColorClicked(View v){
        if(setColor == false){
            colorCode = "#*dark";
            setColor = true;
            messageList.setBackgroundResource(R.drawable.iamge2);

        }
        else {
            colorCode = "#*green";
            setColor = false;
            messageList.setBackgroundResource(R.drawable.image);
        }

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {

                    sendReceive.write(colorCode.getBytes());
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }
    private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;


        public SendReceive(Socket skt)
        {
            socket=skt;
            try {
                inputStream=socket.getInputStream();
                outputStream=socket.getOutputStream();

                //added here 2 lines
                //output = new PrintWriter(socket.getOutputStream());
                //input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (socket != null) {
                try {
                    bytes = inputStream.read(buffer);

                    if (bytes > 0) {
                        handler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] msgbytes){

            try {
                //Add here
                outputStream.write(msgbytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(android.os.Message msg) {
            switch (msg.what)
            {
                case MESSAGE_READ:
                    byte[] readBuff= (byte[]) msg.obj;
                    String tempMsg=new String(readBuff,0,msg.arg1);
                    if(tempMsg.charAt(0) == '#') {
                        if (tempMsg.charAt(1) == '*') {

                            //Log.d(TAG, "Color is: " + tempMsg);
                            if (tempMsg.equals("#*green")) {
                                setColor = false;
                               // Log.d(TAG, "SetColor is: " + setColor);
                                messageList.setBackgroundColor(Color.parseColor("#008577"));
                            } else {
                                setColor = true;
                                //Log.d(TAG, "SetColor is: " + setColor);
                                messageList.setBackgroundColor(Color.parseColor("#00574B"));

                            }

                        }
                        if (tempMsg.charAt(1) == '@') {
                            tempMsg = tempMsg.replace("#@", "");
                            Toast.makeText(getApplicationContext(), "File saved in " + getFilesDir(), Toast.LENGTH_SHORT).show();
                            String fileText = tempMsg;
                        }
                    }

                    else {
                        //chatText.setText(tempMsg);
                        setView(tempMsg);
                    }
                    break;
            }
            return true;
        }
    });



}

