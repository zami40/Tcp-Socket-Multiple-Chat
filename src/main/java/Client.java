import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

// Tcp Socket chatting

public class Client implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private boolean done;

    @Override
    public void run(){
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("System IP Address : " +
                          (localhost.getHostAddress()).trim());
            // Socket s = new Socket();
            // System.out.println(s.getLocalAddress().getHostName());

            client = new Socket("192.168.56.1",9999);
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            InputHandler inputHandler = new InputHandler();
            Thread thread = new Thread(inputHandler);
            thread.start();

            String inMessage;
            while ((inMessage = in.readLine()) != null ){
                System.out.println(inMessage);
            }

        }catch(IOException e){
            shutdown();
        }
    }

    public void shutdown(){
        done = true;
        try{
            in.close();
            out.close();
            if(!client.isClosed()){
                client.close();
            }
        } catch (IOException e) {
//            shutdown();
        }
    }

    class InputHandler implements Runnable{
        @Override
        public void run(){
            try {
                BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
                while (!done){
                    String message = inReader.readLine();
                    if(message.equals("/Q")){
                        out.println(message);
                        inReader.close();
                        shutdown();
                    }else {
                        out.println(message);
                    }
                }
            } catch (IOException e) {
//                throw new RuntimeException(e);
                shutdown();
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.run();
    }
}
