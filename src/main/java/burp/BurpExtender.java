package burp;

import java.awt.Component;

import burp.userinterface.LoggerOptionsPanel;
import otib.kafka.Kafka;

import java.util.*;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import java.io.PrintWriter;



public class BurpExtender extends AbstractTableModel implements IBurpExtender, ITab, IHttpListener, IMessageEditorController {
    private IBurpExtenderCallbacks callbacks;
    private IExtensionHelpers helpers;


    private JSplitPane splitPane;
    private LoggerOptionsPanel optionsPane;
    private IMessageEditor requestViewer;

    //localhost proxy for correct request build
    private int LocalProxyPort = 8080;

    public final static String KAFKA_HEADER = "BURP_KAFKA_EXT";

    //root tabs
    private JTabbedPane tabbedWrapper;

    private IMessageEditor responseViewer;
    public final List<LogEntry> log = new ArrayList<LogEntry>();
    private LogEntry currentlyDisplayedItem;
    private PrintWriter stdout;

    //
    private Kafka list;
    private Thread ListThread = null;

    //
    // implement IBurpExtender
    //

    @Override
    public void registerExtenderCallbacks(final IBurpExtenderCallbacks callbacks) {
        BurpExtender burpExtender = this;
        // keep a reference to our callbacks object
        this.callbacks = callbacks;

        // obtain an extension helpers object
        helpers = callbacks.getHelpers();

        // set our extension name
        callbacks.setExtensionName("Kafka logger");

        stdout = new PrintWriter(callbacks.getStdout(), true);

        // create our UI
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                stdout.println("TEST!!!");
                // main split pane
                splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                optionsPane = new LoggerOptionsPanel();
                tabbedWrapper = new JTabbedPane();

                // table of log entries
                Table logTable = new Table(BurpExtender.this);
                JScrollPane scrollPane = new JScrollPane(logTable);
                splitPane.setLeftComponent(scrollPane);

                // tabs with request/response viewers
                JTabbedPane tabs = new JTabbedPane();
                requestViewer = callbacks.createMessageEditor(BurpExtender.this, false);
                tabs.addTab("Request", requestViewer.getComponent());
                splitPane.setRightComponent(tabs);
                tabbedWrapper.addTab("View Logs", null, splitPane, null);
                ;
                tabbedWrapper.addTab("Options", optionsPane);

                // customize our UI components
                callbacks.customizeUiComponent(splitPane);
                callbacks.customizeUiComponent(logTable);
                callbacks.customizeUiComponent(scrollPane);
                callbacks.customizeUiComponent(tabs);

                // add the custom tab to Burp's UI
                callbacks.addSuiteTab(BurpExtender.this);

                // register ourselves as an HTTP listener
                callbacks.registerHttpListener(BurpExtender.this);

                //
                list = new Kafka();
                list.Callbacks = callbacks;
                list.helpers = helpers;
                list.log = log;
                list.burpExtender = burpExtender;
                ListThread = new Thread(list);
                ListThread.start();
            }
        });
    }

    //
    // implement ITab
    //

    @Override
    public String getTabCaption() {
        return "Kafka logger";
    }

    @Override
    public Component getUiComponent() {
        return tabbedWrapper;
    }

    //
    // implement IHttpListener
    //

    @Override
    public void processHttpMessage(int toolFlag, boolean messageIsRequest, IHttpRequestResponse messageInfo) {
        // only process request with response from repeater & intruder contains kafka header
        if ((toolFlag == callbacks.TOOL_REPEATER || toolFlag == callbacks.TOOL_INTRUDER) && !messageIsRequest) {
            IRequestInfo request = helpers.analyzeRequest(messageInfo);
            if (request.getHeaders().contains(KAFKA_HEADER)) {
                String topic =  request.getUrl().getPath();
                byte[] message = Base64.getDecoder().decode(
                        helpers.bytesToString(messageInfo.getRequest()).substring(
                                request.getBodyOffset()
                        )
                );
                if (list.sendMessage(topic, message)) {
                    messageInfo.setResponse(helpers.stringToBytes("HTTP/1.1 200 OK\n\r" +
                            "\n\nKafka Message was successfully sent!"));
                }
            }

        }

        // Usage example
        /*else if (messageIsRequest)

        {
            List<String> headers = new ArrayList<String>();
            headers.add("GET /analytics.js HTTP/1.1");
            headers.add("Host: 127.0.0.1");
            headers.add(KafkaMarker);

            String body = "Test body";
            byte[] request = helpers.buildHttpMessage(headers, helpers.stringToBytes(body));

            // create a new log entry with the message details
            synchronized (log) {
                int row = log.size();
                log.add(new LogEntry("Topic " + String.valueOf(row), request, "Message"));
                //log.add(new LogEntry(toolFlag, callbacks.saveBuffersToTempFiles(messageInfo),
                //       helpers.analyzeRequest(messageInfo).getUrl()));
                fireTableRowsInserted(row, row);
            }
        }*/
    }

    //
    // extend AbstractTableModel
    //

    @Override
    public int getRowCount() {
        return log.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Topic";
            case 1:
                return "Message";
            default:
                return "";
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LogEntry logEntry = log.get(rowIndex);

        switch (columnIndex) {
            case 0:
                return "Test topic " + logEntry.topic.toString();
            //return callbacks.getToolName(logEntry.top);
            case 1:
                return "Test message " + logEntry.message.toString();
            //return logEntry.url.toString();
            default:
                return "";
        }
    }

    //
    // implement IMessageEditorController
    // this allows our request/response viewers to obtain details about the messages being displayed
    //

    @Override
    public byte[] getRequest() {
        return currentlyDisplayedItem.request;
    }

    @Override
    public byte[] getResponse() {
        return null;
    }


    @Override
    public IHttpService getHttpService() {
        return helpers.buildHttpService("127.0.0.1", LocalProxyPort, false);
    }

    //
    // extend JTable to handle cell selection
    //

    private class Table extends JTable {
        public Table(TableModel tableModel) {
            super(tableModel);
        }

        @Override
        public void changeSelection(int row, int col, boolean toggle, boolean extend) {


            // show the log entry for the selected row
            LogEntry logEntry = log.get(row);
            IParameter test = helpers.buildParameter("Kafka", "True", IParameter.PARAM_BODY);

            requestViewer.setMessage(logEntry.request, true);

            //responseViewer.setMessage(logEntry.requestResponse.getResponse(), false);
            currentlyDisplayedItem = logEntry;

            super.changeSelection(row, col, toggle, extend);
        }
    }

    //
    // class to hold details of each log entry
    //

    public static class LogEntry {
        final String topic;
        final byte[] request;
        final String message;

        public LogEntry(String topic, byte[] request, String message) {
            this.topic = topic;
            this.request = request;
            this.message = message;
        }
    }
}