import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class Client extends JFrame{
    private JTextField userInputText;
    private JTextArea chatWindow;
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private String message = "";
    private String serverIP;
    private Socket socket;

    //constructor

    public Client(String host){
        super("Client-side");
        serverIP = host;
        userInputText = new JTextField();
        userInputText.setEditable(false);
        userInputText.addActionListener(
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
        chatWindow.setBackground(Color.LIGHT_GRAY);
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(300,600);
        setVisible(true);
    }
    //start client
    public void startClient(){
        try {
            connectToServer();
            setupStreams();
            whileChatting();
        } catch (EOFException eofException) {
            showMessage("\nClient broke connection");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeConnection();
        }
    }
    //connecting to server
    private void connectToServer() throws IOException{
        showMessage("Trying to connect...\n");
        socket = new Socket(InetAddress.getByName(serverIP), 7777);
        showMessage("Now you`re connected to " + socket.getInetAddress().getHostName());
    }
    //setting input&output streams
    private void setupStreams() throws IOException{
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        outputStream.flush();
        inputStream = new ObjectInputStream(socket.getInputStream());
        showMessage("\nStreams are ready for work!");
    }
    //processing data while texting
    private void whileChatting() throws IOException{
        readyToType(true);
        do{
            try {
                message = (String) inputStream.readObject();
                showMessage("\n " + message);
            } catch (ClassNotFoundException classNotFoundException) {
                showMessage("\nBad text!");
            }
        }
        while (!message.equals("Server - *"));
    }
    //socket & streams closing
    private void closeConnection(){
        showMessage("\nConnection is closing...");
        readyToType(false);
        try {
            outputStream.close();
            inputStream.close();
            socket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }
    //sending messages to server
    private void sendMessage(String message){
        try{
            outputStream.writeObject("CLIENT: " + message);
            outputStream.flush();
            showMessage("\nCLIENT: " + message);
        } catch (IOException ioException) {
            chatWindow.append("\nSomething went wrong...");
        }
    }
    //chat window refreshing
    private void showMessage(final String msg){
        SwingUtilities.invokeLater(
                new Runnable() {
                    @Override
                    public void run() {
                        chatWindow.append(msg);
                    }
                }
        );
    }
    //permission to input text
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
