package com.wmp.tool;

import com.wmp.develop.tool.QToolUnit;
import com.wmp.develop.tool.apptools.GetPath;
import org.json.JSONObject;
import org.tinylog.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.TreeMap;

public class Main extends QToolUnit {
    @Override
    protected String setName() {
        return "翻译";
    }

    @Override
    protected Icon setIcon() {
        return null;
    }

    @Override
    protected String setVersion() {
        return "1.1";
    }

    @Override
    public void showSetsDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("设置");

        try {
            File file = new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "inf/translate.inf");

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            List<String> api = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            String apikey = api.size() > 0 ? api.get(0) : "";
            String appId = api.size() > 1 ? api.get(1) : "";

            dialog.setLayout(new BorderLayout(10, 10));
            dialog.setAlwaysOnTop(true);

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JPanel inputPanel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(5, 5, 5, 5);

            JLabel apikeyLabel = new JLabel("API Key:");
            inputPanel.add(apikeyLabel, gbc);

            JTextField apikeyField = new JTextField(apikey, 30);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            inputPanel.add(apikeyField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.weightx = 0;
            JLabel appIdLabel = new JLabel("App ID:");
            inputPanel.add(appIdLabel, gbc);

            JTextField appIdField = new JTextField(appId, 30);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 1.0;
            inputPanel.add(appIdField, gbc);

            mainPanel.add(inputPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
            JButton saveButton = new JButton("保存");
            JButton cancelButton = new JButton("取消");

            final boolean[] hasChanges = {false};

            apikeyField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { hasChanges[0] = true; }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { hasChanges[0] = true; }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { hasChanges[0] = true; }
            });

            appIdField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { hasChanges[0] = true; }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { hasChanges[0] = true; }
                public void changedUpdate(javax.swing.event.DocumentEvent e) { hasChanges[0] = true; }
            });

            saveButton.addActionListener(e -> {
                try {
                    FileWriter writer = new FileWriter(file, false);
                    writer.write(apikeyField.getText() + "\n");
                    writer.write(appIdField.getText() + "\n");
                    writer.close();

                    GetTranslate.setAPIKey(apikeyField.getText(), appIdField.getText());

                    JOptionPane.showMessageDialog(dialog, "保存成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    hasChanges[0] = false;
                    dialog.dispose();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, "保存失败：" + ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
                }
            });

            cancelButton.addActionListener(e -> {
                if (hasChanges[0]) {
                    int result = JOptionPane.showConfirmDialog(dialog, "是否保存更改？", "确认",
                            JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        saveButton.doClick();
                    } else if (result == JOptionPane.NO_OPTION) {
                        dialog.dispose();
                    }
                } else {
                    dialog.dispose();
                }
            });

            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            dialog.add(mainPanel);

            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    cancelButton.doClick();
                }
            });

            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);

        } catch (Exception e) {
            Logger.error("打开设置失败：{}", e.getMessage());
            JOptionPane.showMessageDialog(null, "打开设置失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static final String infoRegular = "源语言：%s 目标语言：%s 其他：%s";
    private static final String tipsRegular = "错误码：%s 错误信息：%s";

    private JLabel infoLabel = new JLabel(infoRegular);
    private JLabel tips = new JLabel(tipsRegular);

    JComboBox<String> fromComboBox = new JComboBox<>();
    JComboBox<String> toComboBox = new JComboBox<>();


    @Override
    public void showTool() {
        try {
            File file = new File(GetPath.getAppPath(GetPath.SOURCE_FILE_PATH), "inf/translate.inf");

            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            List<String> api = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
            String apikey = api.get(0);//在'控制台->API应用'中查看
            String appId = api.get(1);//应用唯一标识,在'控制台->API应用'中查看
            GetTranslate.setAPIKey(apikey, appId);

            JDialog dialog = new JDialog();
            dialog.setTitle("翻译(使用小牛翻译API)");
            dialog.setLayout(new BorderLayout());
            dialog.setAlwaysOnTop(true);

            initComponent(dialog);

            dialog.setSize(500, 300);
            dialog.setLocationRelativeTo(null);

            dialog.setVisible(true);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void initComponent(JDialog dialog) {
        TreeMap<String, String> languageMap = LanguageList.getLanguageMap();

        fromComboBox.removeAllItems();
        languageMap.forEach((key, value) -> fromComboBox.addItem(key));
        fromComboBox.setSelectedItem("中文(简体)");
        fromComboBox.setEditable(true);

        toComboBox.removeAllItems();
        languageMap.forEach((key, value) -> toComboBox.addItem(key));
        toComboBox.setSelectedItem("英语");
        toComboBox.setEditable(true);


        fromComboBox.addActionListener(e->{
            //如果文字内容改变，修改infoLabel
            if (fromComboBox.getSelectedItem() != null) {
                infoLabel.setText(String.format(infoRegular,
                        fromComboBox.getSelectedItem(),
                        toComboBox.getSelectedItem(),
                        ""));
            }
        });
        toComboBox.addActionListener(e->{
            //如果文字内容改变，修改infoLabel
            if (toComboBox.getSelectedItem() != null) {
                infoLabel.setText(String.format(infoRegular,
                        fromComboBox.getSelectedItem(),
                        toComboBox.getSelectedItem(),
                        ""));
            }
        });
        infoLabel.setText(String.format(infoRegular,
                fromComboBox.getSelectedItem(),
                toComboBox.getSelectedItem(),
                ""));


        JTextArea srcTextArea = new JTextArea();
        srcTextArea.setLineWrap(true);
        JTextArea transTextArea = new JTextArea();
        transTextArea.setLineWrap(true);
        transTextArea.setEditable(false);

        JButton transButton = new JButton("翻译");
        transButton.setFont(UIManager.getFont("h2.font"));
        transButton.addActionListener(e -> {
            translate(dialog, srcTextArea, languageMap, fromComboBox, toComboBox, transTextArea);
        });

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
        infoPanel.add(new JLabel("源语言"));
        infoPanel.add(fromComboBox);
        infoPanel.add(new JLabel("目标语言"));
        infoPanel.add(toComboBox);
        dialog.add(new JScrollPane(infoPanel), BorderLayout.NORTH);

        JPanel textPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 5, 5);

        tips.setVisible(false);
        tips.setForeground(Color.RED);

        textPanel.add(new JLabel("源文本"), gbc);
        gbc.gridy++;
        textPanel.add(new JScrollPane(srcTextArea), gbc);
        gbc.gridy++;
        textPanel.add(new JLabel("翻译结果"), gbc);
        gbc.gridy++;
        textPanel.add(new JScrollPane(transTextArea), gbc);
        gbc.gridy++;
        textPanel.add(tips, gbc);
        gbc.gridy++;
        textPanel.add(infoLabel, gbc);
        dialog.add(textPanel, BorderLayout.CENTER);

        dialog.add(transButton, BorderLayout.SOUTH);
    }

    private void translate(JDialog dialog, JTextArea srcTextArea, TreeMap<String, String> languageMap, JComboBox<String> fromComboBox, JComboBox<String> toComboBox, JTextArea transTextArea) {
        tips.setVisible(false);
        String srcText = srcTextArea.getText();
        String transText = GetTranslate.translate(srcText,
                languageMap.get(fromComboBox.getSelectedItem().toString()),
                languageMap.get(toComboBox.getSelectedItem().toString()));
        JSONObject transJson = new JSONObject(transText);
        if (!transJson.has("tgtText")) {
            Logger.error("翻译失败! 错误码：{} 错误信息：{}",
                    transJson.getString("errorCode"), transJson.getString("errorMsg"));
            tips.setText(String.format(tipsRegular, transJson.getString("errorCode"), transJson.getString("errorMsg")));
            tips.setVisible(true);
            return;
        }
        if (transJson.has("srcText")){
            Logger.warn("存在未翻译的文本：{}", transJson.getString("srcText"));
            infoLabel.setText(String.format(infoRegular,
                    fromComboBox.getSelectedItem(),
                    fromComboBox.getSelectedItem(),
                    "未翻译：" + transJson.getString("srcText")));
        }
        transTextArea.setText(transJson.getString("tgtText"));
        Logger.info("成功将[{}]->[{}]|[{}]->[{}]", srcText, transText,
                fromComboBox.getSelectedItem(), toComboBox.getSelectedItem());
    }

    static void main() {
        new Main().showTool();
    }
}
