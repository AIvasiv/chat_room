import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Server extends JFrame{
    private JTextField userInputText;
    private JTextArea chatWindow;
    private ObjectInputStream inputStream;  //   Input and output sream
    private ObjectOutputStream outputStream;
    private ServerSocket serverSocket;  //  server-side var for port
    private Socket connection;  //  socket=ip-address+port

    public Server() {
        super("Server-side");
        userInputText = new JTextField();
        userInputText.setEditable(false);//impossible text input without connection
        userInputText.addActionListener(    // action after user will press button "send"
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        sendMessage(e.getActionCommand());
                        userInputText.setText("");
                    }
                }
        );
        add(userInputText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow));
        setSize(300, 600);
        setVisible(true);
    }

        //setting and starting server-side

    public void startServer(){
        try{
            serverSocket = new ServerSocket(7777,100);
            while(true){
                try{
                    waitForConnection(); // waiting for connection
                    setupStreams(); // setup all of the streams(input,output)
                    whileChating(); // send message
                }catch(EOFException e){
                    showMessage("\nServer broke connection!!!");
                }finally {
                    closeConnection();
                }
            }

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    //waiting for connection and show information about connection
    private void waitForConnection() throws IOException{
        showMessage("Waiting for clients connection...\n");
        connection = serverSocket.accept(); // accept connection of client
        showMessage("Connected with  \n" + connection.getInetAddress().getHostName()); //get ip-address of connection-client
    }
    //streams initialization and setup for sending and getting data
    private void setupStreams()throws IOException{
        outputStream = new ObjectOutputStream(connection.getOutputStream()); // possibility for sending messages to clients computer
        outputStream.flush();
        inputStream = new ObjectInputStream(connection.getInputStream()); // possibility for accepting messages to clients computer
        showMessage("\nStream created\n");
    }
    //data processing while texting
    private void whileChating() throws IOException{
        String message = "You are connected";
        sendMessage(message);
        readyToType(true);
        do{
            try {
                message = (String) inputStream.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException classNotFoundException) {
                showMessage("\n Bad input, really, can`t understand...");
            }
        }
        while(!message.equals("CLIENT - *"));
    }
    //close sockets and streams
    private void closeConnection(){
        showMessage("\nConnection closing...");
        readyToType(false);
        try {
            outputStream.close();
            inputStream.close();
            connection.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    //sending message
    private void sendMessage(String message){
        try {
            outputStream.writeObject("SERVER : " + message);
            outputStream.flush();
            showMessage("\nSERVER : " + message);
        } catch (IOException ioException) {
            chatWindow.append("\nERROR: I CAN`T SEND THIS");
        }
    }
    //refresh chat window
    private void showMessage(final String text){
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        chatWindow.append(text);
                    }
                }
        );
    }
    //access for input
    private void readyToType(final boolean tof){
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        userInputText.setEditable(tof);
                    }
                }
        );
    }
}
