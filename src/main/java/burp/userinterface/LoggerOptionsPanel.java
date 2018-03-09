package burp.userinterface;

import javax.swing.*;
import java.awt.*;

public class LoggerOptionsPanel extends JScrollPane{
    private JToggleButton tglbtnIsEnabled = new JToggleButton("Kafka logger is running");
    private JTextField kafkaServerAddress = new JTextField("");
    private JTextField kafkaServerPort = new JTextField("");

    private final JPanel contentWrapper;

    public LoggerOptionsPanel() {
            contentWrapper = new JPanel(new GridLayout(3,2,2,2));
        JLabel connectionSettings = new JLabel("Kafka connection settings:");

        contentWrapper.add(connectionSettings);
        contentWrapper.add(kafkaServerAddress);
        this.setViewportView(contentWrapper);
        /*GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{53, 94, 320, 250, 0, 0};
        gridBagLayout.rowHeights = new int[]{0, 43, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 42, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        contentWrapper.setLayout(gridBagLayout);
        this.setViewportView(contentWrapper);

        JLabel connectionSettings = new JLabel("Kafka connection settings:");
        connectionSettings.setVerticalTextPosition(SwingConstants.CENTER);
        connectionSettings.setVerticalAlignment(SwingConstants.CENTER);
        connectionSettings.setFont(new Font("Tahoma", Font.BOLD, 14));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.SOUTHWEST;
        gbc.insets = new Insets(0, 0, 5, 5);
        gbc.gridx = 1;
        gbc.gridy = 1;
        contentWrapper.add(connectionSettings, gbc);
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx =2;
        kafkaServerAddress.setFont(new Font("Tahoma", Font.PLAIN, 13));
        contentWrapper.add(kafkaServerAddress, gbc);
        gbc.gridx = 3;
        kafkaServerPort.setFont(new Font("Tahoma", Font.PLAIN, 13));
        contentWrapper.add(kafkaServerPort, gbc);
*/


    }




}
