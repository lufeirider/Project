package burp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;

public class XxeOption implements ITab, ActionListener {
    JPanel jp;
    JTextField jtfId,jtfToken;
    JButton jb;
    JLabel jlId,jlToken;
    private final IBurpExtenderCallbacks callbacks;


    public XxeOption(final IBurpExtenderCallbacks callbacks) {
        this.callbacks = callbacks;


        jp = new JPanel();


        jlId = new JLabel("Identifier:");
        jlToken = new JLabel("API Token:");
        jtfId = new JTextField(10);
        jtfToken = new JTextField(20);


        //设置Id,Token文本框
        File file = new File("xxe.config");
        if(file.exists()){
            String info = ReadConfig();
            if(info.contains("|"))
            {
                jtfId.setText(info.split("\\|")[0]);
                jtfToken.setText(info.split("\\|")[1]);
            }

        }

        jb = new JButton("保存");
        jb.addActionListener(this);

        jp.add(jlId);
        jp.add(jtfId);
        jp.add(jlToken);
        jp.add(jtfToken);
        jp.add(jb);


        callbacks.customizeUiComponent(jtfToken);
        callbacks.addSuiteTab(XxeOption.this);
    }

    //写入配置
    public void WriteConfig(String data)
    {
        try{

            File file = new File("xxe.config");

            //if file doesnt exists, then create it
            if(!file.exists()){
                file.createNewFile();
            }

            //true = append file
            FileWriter fileWritter = new FileWriter(file.getName(),false);
            BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
            bufferWritter.write(data);
            bufferWritter.close();

        }catch(IOException e){

            e.printStackTrace();
        }

    }

    //读取配置
    public String ReadConfig()
    {
        StringBuilder result = new StringBuilder();
        try{
            BufferedReader br = new BufferedReader(new FileReader("xxe.config"));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result.append(s);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }



    @Override
    public String getTabCaption() {
        return "XXEScanner";
    }

    @Override
    public Component getUiComponent() {
        return jp;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if((jtfId.getText() != "") && (jtfToken.getText() != ""))
        {
            String path = "";
            File directory  = new File(".");
            try {
                path = directory.getCanonicalPath() + "\\xxe.config";
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            WriteConfig(jtfId.getText() + "|" +jtfToken.getText());
        }else {
            JOptionPane.showMessageDialog(jp, "ID 和 Token不能为空", "提示",JOptionPane.WARNING_MESSAGE);
        }


    }
}
