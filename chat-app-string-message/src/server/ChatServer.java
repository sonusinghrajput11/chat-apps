package server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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

	public ClientThread(Socket client, List<ClientThread> activeClients) {
		this.client = client;
		this.activeClients = activeClients;
	}

	@Override
	public void run() {
		InputStream is = null;
		BufferedReader br = null;
		try {
			is = client.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));

			String receivedMessage = null;
			
			while (true) {
				if (!client.isClosed() && (receivedMessage = br.readLine()) != null) {
					System.out.println(receivedMessage);
					String[] messageArray = receivedMessage.split(":");
					String action = messageArray[0];

					if (action.equalsIgnoreCase("New")) {
						recipient = messageArray[2];
						registerUserNameAndRecipient(messageArray[1], messageArray[2]);
						addClient(this);
					} else if ("End".equalsIgnoreCase(action)) {
						removeClient(this);
					} else {
						deliverMessage(messageArray[0]);
					}
				} else {
					break;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}finally{
			try {
				client.close();
				is.close();
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private synchronized void deliverMessage(String message) {
		activeClients.forEach((aciveClient) -> {
			if (aciveClient.clientUserName.equalsIgnoreCase(this.recipient)) {
				OutputStream os;
				try {
					os = aciveClient.client.getOutputStream();
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
					bw.write(message + "\n");
					bw.flush();
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
