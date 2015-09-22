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
    private boolean connectedToServer;
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame = new JFrame("Messenger");
    private JTextField message = new JTextField(40);
    private JTextArea chat = new JTextArea(8, 40);
    private JButton connectButton = new JButton("Connect/Disconnect");


    public Client() {
       init();
    }
    private void init(){
        // Layout GUI
        message.setEditable(false);
        chat.setEditable(false);
        frame.getContentPane().add(message, "North");
        frame.getContentPane().add(new JScrollPane(chat), "Center");
        frame.getContentPane().add(connectButton, "South");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        frame.pack();

        // Add Listeners
        message.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                out.print(sendTo() + '|');
                out.println(message.getText());
                message.setText("");
            }
        });
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (connectedToServer) {
                    connectedToServer = false;
                    chat.setText("");
                } else {
                    run();
                }
            }
        });
        run();
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
    private void warning() {
        JOptionPane.showMessageDialog(frame, "Problem with the server");
    }


    private void run() {
        try {
            // Make connection and initialize streams
            String serverAddress = getServerAddress();
            Socket socket = new Socket(serverAddress, 9001);
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            connectedToServer = true;
            // Process all messages from server, according to the protocol.
            while (connectedToServer) {
                String line = in.readLine();
                if (line.startsWith("SUBMITNAME")) {
                    name = getName();
                    out.println(name);
                } else if (line.startsWith("NAMEACCEPTED")) {
                    message.setEditable(true);
                } else if (line.startsWith("MESSAGE")) {
                    chat.append(line.substring(8) + "\n");
                }
            }
            if(!connectedToServer){
                out.close();
                in.close();
                socket.close();

            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            warning();
        } catch (IOException e) {
            e.printStackTrace();
            warning();
        }
    }


    public static void main(String[] args) throws Exception {
        Client client = new Client();

    }
}
