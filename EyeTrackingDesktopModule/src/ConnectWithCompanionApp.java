import javax.swing.*;

public class ConnectWithCompanionApp {
    public JPanel panel;
    public JLabel prompt;
    public JLabel ip_prompt;
    public JLabel port_prompt;
    private JLabel loading;

    public void setPortNo(int no){
        port_prompt.setText("Port No: " + no);
    }

    public void setIPAddress(String add){
        ip_prompt.setText("IP Address: " + add);
    }
}
