package com.stech.smartads.components.socket;

import android.os.AsyncTask;

import com.stech.smartads.utils.CommonUtil;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;



public class SocketClient extends AsyncTask<Object, Object, String> {

    String dstAddress; // server IP Address
    int dstPort;  // server working port for Address
    String response = ""; // response text
    SocketListener listener;
    Socket socket = null;

    SocketClient(String addr, int port, SocketListener listener) {
        dstAddress = addr;
        dstPort = port;
        this.listener = listener;
    }

    public Socket getCurrentSocket()
    {
        if(socket == null || socket.isClosed())
            return null;
        return socket;
    }

    public void sendMessage(String message)
    {
        try {
            if (socket == null || socket.isClosed()) {
                socket = null;
                InetAddress serverAddr = InetAddress.getByName(dstAddress);
                socket = new Socket(serverAddr, dstPort);
            }
            //write message
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);
            out.print(message);

        } catch (UnknownHostException e) {
        // TODO Auto-generated catch block
        CommonUtil.error(e);

    } catch (IOException e) {
        // TODO Auto-generated catch block
        CommonUtil.error(e);

    } finally {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                CommonUtil.error(e);
            }
        }
    }
    }

    @Override
    protected String doInBackground(Object... arg0) {

        try {
            if (socket == null || socket.isClosed()) {
                socket = null;
                InetAddress serverAddr = InetAddress.getByName(dstAddress);
                socket = new Socket(serverAddr, dstPort);
            }

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(
                    1024);
            byte[] buffer = new byte[1024];

            int bytesRead;
            InputStream inputStream = socket.getInputStream();

			/*
             * notice: inputStream.read() will block if no data return
			 */
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
                response += byteArrayOutputStream.toString("UTF-8");
            }

        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            CommonUtil.error(e);
            response = "UnknownHostException: " + e.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            CommonUtil.error(e);
            response = "IOException: " + e.toString();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    CommonUtil.error(e);
                }
            }
        }
        return response;
    }

    @Override
    protected void onPostExecute(String result) {
        listener.getResponse(response);
        super.onPostExecute(result);
    }

}