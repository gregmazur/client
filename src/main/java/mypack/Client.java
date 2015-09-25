package mypack;

/**
 * Created by greg on 21.09.15.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;


public class Client {
    private String name;
    private static boolean connectedToServer;
    private static boolean connectionNeeded;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Messenger");
    private JTextField message = new JTextField(40);
    private JTextArea chat = new JTextArea(8, 40);
    private static JButton connectButton = new JButton("Connect");


    public Client() {
        init();
    }

    private void init() {
        // Layout GUI
        message.setEditable(false);
        chat.setEditable(false);
        frame.getContentPane().add(message, "North");
        frame.getContentPane().add(new JScrollPane(chat), "Center");
        frame.getContentPane().add(connectButton, "South");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();


        message.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                out.print("TO " + sendTo() + ":");
                out.println(message.getText());
                message.setText("");
            }
        });
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connectedToServer) {
                    connectedToServer = false;
                    connectButton.setText("Connect");
                    if (out != null) {
                        out.println("DISCONNECT");
                    }
                    chat.setText("");
                } else {
                    connectionNeeded = true;
                }
            }
        });
        connectionNeeded = true;
    }

    private String getServerAddress() {
        return JOptionPane.showInputDialog(
                frame,
                "Enter IP Address of the Server:",
                "Welcome to the Chatter",
                JOptionPane.QUESTION_MESSAGE);
    }

    private String getName() {
        return JOptionPane.showInputDialog(
                frame,
                "Choose a screen name:",
                "Screen name selection",
                JOptionPane.PLAIN_MESSAGE);
    }

    private String sendTo() {
        return JOptionPane.showInputDialog(
                frame,
                "sendTo",
                "Enter the person`s name you want to send the message",
                JOptionPane.PLAIN_MESSAGE);
    }

    private void warning(String message) {
        JOptionPane.showMessageDialog(frame, message);
    }

    private String getAddress() {
        String serverAddress = null;
        while (serverAddress == null || serverAddress.isEmpty()) {
            serverAddress = getServerAddress();
        }
        return serverAddress;
    }

    private String getValidName() {
        String name = null;
        while (name == null || name.isEmpty()) {
            name = getName();
        }
        return name;
    }

    /**
     * makes sure the returned socket is working
     *
     * @return working socket
     */
    private Socket getConnectedSocket() {
        Socket socket = null;
        while (socket == null || !socket.isConnected()) {
            String serverAddress = getAddress();
            try {
                socket = new Socket(serverAddress, 9001);
            } catch (IOException e) {
                e.printStackTrace();
                warning("Not valid ip");
            }
        }
        return socket;
    }

    private boolean getConnection() {
        socket = getConnectedSocket();
        try {
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connectButton.setText("Disconnect");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * listening to server
     */
    private void run() {
        try {
            connectedToServer = getConnection();
            while (true) {
                BASE:
                {
                    Thread.sleep(10);
                    String line = in.readLine();
                    if (line == null) {
                        break BASE;
                    }
                    if (out == null) {
                        socket.close();
                        connectedToServer = false;
                        connectButton.setText("Connect");
                    }
                    if (line.startsWith("SUBMITNAME")) {
                        name = getValidName();
                        out.println("NAME " + name);
                    } else if (line.startsWith("NAMEACCEPTED")) {
                        message.setEditable(true);
                        frame.setTitle("Messenger for " + name);
                    } else if (line.startsWith("MESSAGE")) {
                        chat.append(line.substring(8) + "\n");
                    } else if (line.startsWith("DISCONNECT")) {
                        in.close();
                        out.close();
                        socket.close();
                        connectedToServer = false;
                        connectButton.setText("Connect");
                        break;
                    } else if (line.startsWith("NOTAVAILABLE")) {
                        warning("Message was not delivered, user is offline");
                    }
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
            warning("Server error");
        } catch (IOException e) {
            e.printStackTrace();
            warning("Server error");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        Client client = new Client();
        while (true) {
            if (connectionNeeded) {
                client.run();
                connectionNeeded = false;
            }
        }

    }
}
