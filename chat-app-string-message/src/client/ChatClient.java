package client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ChatClient {
	public static void main(String[] args) {
		Socket client = null;
		try {
			client = new Socket("127.0.0.1", 3000);
			System.out.println("Please start chat...\n");

			OutputStream os = client.getOutputStream();
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os));
			BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

			InputStream is = client.getInputStream();
			BufferedReader brInput = new BufferedReader(new InputStreamReader(is));

			Thread readerThread = new Thread(() -> {
				String receivedMessage = null;
				while (true) {
					try {
						if ((receivedMessage = brInput.readLine()) != null) {
							System.out.println(receivedMessage);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			readerThread.start();

			Thread writerThread = new Thread(() -> {
				String sentMessage = null;
				while (true) {
					try {
						if ((sentMessage = br.readLine()) != null) {
							bw.write(sentMessage + "\n");
							bw.flush();
						}
					} catch (IOException e) {
						e.printStackTrace();
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
