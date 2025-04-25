import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.function.Consumer;



public class Client extends Thread{
	Socket socketClient;
	ObjectOutputStream out;
	ObjectInputStream in;
	private Consumer<Message> callback;
	Client(Consumer<Message> call){
		callback = call;
	}
	
	public void run() {
		
		try {
		socketClient= new Socket("127.0.0.1",5555);
	    out = new ObjectOutputStream(socketClient.getOutputStream());
	    in = new ObjectInputStream(socketClient.getInputStream());
	    socketClient.setTcpNoDelay(true);
		}
		catch(Exception e) {

		}
		while(true) {
			try {
			Message message = (Message) in.readObject();
			callback.accept(message);
			}
			catch(Exception e) {}
		}
    }
	
	public void send(Message data) {
		try {
			out.writeObject(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			if (out != null) out.close();
			if (in != null) in.close();
			if (socketClient != null) socketClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendRaw(Object data) {
		try {
			out.writeObject(data);
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

}
