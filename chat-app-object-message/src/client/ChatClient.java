package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;

import Entity.Action;
import Entity.Message;

public class ChatClient {
	public static void main(String[] args) {
		Socket client = null;
		try {
			client = new Socket("127.0.0.1", 3000);
			System.out.println("Connected to server...\n");

			OutputStream os = client.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(os);
			oos.flush();
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			InputStream is = client.getInputStream();
			ObjectInputStream ois = new ObjectInputStream(is);

			Thread readerThread = new Thread(() -> {
				Message receivedMessage = null;
				while (true) {
					try {
						if ((receivedMessage = (Message) ois.readObject()) != null) {
							if(receivedMessage.getAction() == Action.NEW){
								System.out.println(receivedMessage.getMessage());
							}else{
								System.out.println(receivedMessage.getUserName() + " : " + receivedMessage.getMessage());	
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
						break;
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						break;
					}
				}
			});
			readerThread.start();

			Thread writerThread = new Thread(() -> {
				Message message = new Message();
				message.setAction(Action.NEW);
				while (true) {
					try {
						if (message.getAction() == Action.NEW) {
							System.out.println("Please provide user name...\n");
							message.setUserName(br.readLine());
							System.out.println("Please provide recipient's user name...\n");
							message.setRecipient(br.readLine());
						} else {
							message.setMessage(br.readLine());
						}
						oos.reset();
						oos.writeObject(message);
						oos.flush();
						if (message.getAction() == Action.NEW) {
							message.setAction(Action.MSG);
						}
					} catch (IOException e) {
						e.printStackTrace();
						break;
					}
				}
			});
			writerThread.start();

			readerThread.join();
			writerThread.join();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			try {
				client.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
}
