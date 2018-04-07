package server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import Entity.Action;
import Entity.Message;

public class ChatServer {
	static List<ClientThread> activeClients = null;

	public static void main(String[] args) {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(3000);
			activeClients = new ArrayList<ClientThread>();
			System.out.println("Server started, Waiting for connection....");

			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Connected, Please start chat...\n");

				ClientThread client = new ClientThread(clientSocket, activeClients);
				Thread clientThread = new Thread(client);
				clientThread.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

class ClientThread implements Runnable {
	Socket client;
	List<ClientThread> activeClients;
	String clientUserName;
	String recipient;
	ObjectOutputStream oos;
	public ClientThread(Socket client, List<ClientThread> activeClients) {
		this.client = client;
		this.activeClients = activeClients;
	}

	@Override
	public void run() {
		InputStream is = null;
		ObjectInputStream ois = null;
		try {
			OutputStream os = this.client.getOutputStream();
			oos = new ObjectOutputStream(os);
			
			is = client.getInputStream();
			ois = new ObjectInputStream(is);

			Message receivedMessage = null;
			
			while (true) {
				if (!client.isClosed() && (receivedMessage = (Message)ois.readObject()) != null) {
					if (Action.NEW == receivedMessage.getAction()) {
						registerUserNameAndRecipient(receivedMessage.getUserName(), receivedMessage.getRecipient());
						addClient(this);
						receivedMessage.setMessage(receivedMessage.getRecipient() + " Connected...Please start chat.\n");
						oos.reset();
						oos.writeObject(receivedMessage);
						oos.flush();
					} else if (Action.END == receivedMessage.getAction()) {
						removeClient(this);
					} else {
						deliverMessage(receivedMessage);
					}
				} else {
					break;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}finally{
			try {
				client.close();
				is.close();
				ois.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void deliverMessage(Message message) {
		activeClients.forEach((aciveClient) -> {
			if (aciveClient.clientUserName.equalsIgnoreCase(this.recipient)) {
				try {
					ObjectOutputStream oos = aciveClient.oos;
					oos.reset();
					oos.writeObject(message);
					oos.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}
		});
	}

	private void registerUserNameAndRecipient(String userName, String recipient) {
		this.clientUserName = userName;
		this.recipient = recipient;
	}

	private synchronized void addClient(ClientThread clientThread) {
		this.activeClients.add(clientThread);
	}

	private synchronized void removeClient(ClientThread clientThread) {
		this.activeClients.remove(clientThread);
	}
}
