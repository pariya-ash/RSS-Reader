import org.jsoup.Jsoup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Scanner;

public class Main {

    static int cmd;
    static Scanner sc=new Scanner(System.in);
    static File myFile=new File("data.txt");
    static File tmpFile;


    public static void main(String[] args){
        while(true){

            welcome();


            cmd=sc.nextInt();
            if (cmd==1){
                try {
                    showup();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }


            }
            if (cmd==2){
                try{
                    add();
                }
                catch (Exception e){
                    System.out.println("sth went wrong");
                }

            }
            if (cmd==3){
                try {
                    remove();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }


            }
            if (cmd==4){
                return;

            }


        }

    }

    public static boolean isURL(String urlstr) {
        try {
            URL url=new URL(urlstr);
            url.toURI();
            return true;
        }
        catch (Exception ex) { }
        return false;
    }
    public static boolean URLexists(String url)  {
        try{
            FileReader fileReader = new FileReader(myFile);
            BufferedReader reader = new BufferedReader(fileReader);
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                //  System.out.println(parts[0]+" "+parts[1]+" "+parts[2]);
                String tmpurl=url+"index.html";
                if (parts[1].equals(tmpurl)) {
                    return true;
                }
            }
            return false;

        }
        catch(Exception ex){
            return false;
        }




    }

    public static void welcome(){
        System.out.println("Welcome to RSS Reader");
        System.out.println("Type a valid number for yor desired action: ");
        System.out.println("[1] Show updates ");
        System.out.println("[2] Add URL");
        System.out.println("[3] Remove URL");
        System.out.println("[4] Exit");

    }
    static void showup() throws Exception {

        FileReader reader=new FileReader(myFile);
        BufferedReader bfreader=new BufferedReader(reader);
        String currentstr;

        while (((currentstr = bfreader.readLine()) != null)) {

            String[] splitted = currentstr.split(";");
            retrieveRssContent(splitted[2]);
        }

    }
    static void add()throws Exception{
        System.out.println("Please enter website URL to add: ");
        String url= sc.next();
        sc.nextLine();
        if (isURL(url)==true){
            if (URLexists(url)==false){
                try{
                    //  File myFile=new File("data.txt");
                    myFile=new File("data.txt");
                    FileWriter writer=new FileWriter(myFile);
                    BufferedWriter bfwriter=new BufferedWriter(writer);
                    String html=fetchPageSource(url);
                    bfwriter.write(extractPageTitle(html)+";"+url+"index.html"+";"+extractRssUrl(url));
                    bfwriter.newLine();


                    bfwriter.close();
                    System.out.println("added "+url+" successfully");
                }
                catch (ConnectException e) {
                    System.out.println("check your internet connection");
                    //e.printStackTrace();
                }


            }
            else{
                System.out.println("URL already exists");
            }

        }
        else{
            System.out.println("URL is not valid");
        }


    }
    static void remove()throws Exception {
        System.out.println("please enter website URl to remove");{
            String removed= sc.next();
            int ind=0;
            if(URLexists(removed)){
                String[] str=new String[1];
                FileReader reader=new FileReader(myFile);
                BufferedReader bfreader=new BufferedReader(reader);
                String currentstr;
                while (((currentstr = bfreader.readLine()) != null)){
                    String[] splitted= currentstr.split(";");
                    String tmpremoved=removed+"index.html";
                    if(tmpremoved.equals(splitted[1])){
                        continue;
                    }
                    str= Arrays.copyOf(str,ind+1);
                    str[ind]=currentstr;
                    ind++;

                }
                bfreader.close();
                FileWriter writer=new FileWriter(myFile);
                BufferedWriter bfwriter=new BufferedWriter(writer);
                for (int i=0;i<ind;i++){
                    bfwriter.write(str[i]);
                    bfwriter.newLine();
                }
                bfwriter.close();
                System.out.println("removed "+removed+" successfully");
            }
            else{
                System.out.println(removed+" doesn't exists");
            }
        }
    }





    public static String extractPageTitle(String html)
    {
        try
        {
            org.jsoup.nodes.Document doc = Jsoup.parse(html);
            return doc.select("title").first().text();
        }
        catch (Exception e)
        {
            return "Error: no title tag found in page source!";
        }
    }
    public static void retrieveRssContent(String rssUrl)
    {
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");

            for (int i = 0; i < 5; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).getTextContent());
                }
            }
        }
        catch (UnknownHostException ex){
            System.out.println("Check internet connection");
        }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }
    public static String extractRssUrl(String url) throws IOException
    {
        org.jsoup.nodes.Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }
    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null)
            stringBuilder.append(inputLine);

        return stringBuilder.toString();
    }
    public static String fetchPageSource(String urlString) throws Exception
    {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }


}

