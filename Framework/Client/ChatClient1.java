package Client;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JList;
import java.awt.List;
import java.awt.*;
import javax.swing.*;
import java.util.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.net.*;
import java.lang.*;
import java.io.*;
import java.util.*;

public class ChatClient1 extends Thread 
{
	Socket sock;
	ClientGUI parent;
	DataInputStream dis;
	PrintStream ps;
	static Thread kicker;
	
	String name, theHost;
	int thePort;

	public ChatClient1(ClientGUI s, String alias, String Host, int Port)
	{
		parent = s;
		theHost = Host;
		thePort = Port;
		name = alias;	
		kicker = null;
			
	}

	public void run() {
		
		int count = 0;
		kicker = Thread.currentThread();

		while (sock == null && kicker != null )
		{
			try
			{
				sock = new Socket(theHost,thePort);
				dis = new DataInputStream( sock.getInputStream() );
				ps = new PrintStream( sock.getOutputStream() );		
			}
			catch (Exception e)
			{
				System.out.println("Unable to contact host.");
				if (count < 10)
					parent.outputArea.append("Unable to contact host. Retrying...\n" + e);
				sock = null;
			}
			sleep_time( 5000 );
			count++;
		}

		output("login||"+name);
		parent.outputArea.append("Logged in to server successfully.\n");
		parent.B2.setEnabled(true);
		parent.B1.setEnabled(false);
		parent.B3.setEnabled(true);
		parent.B4.setEnabled(true);
		parent.setIconsJLabels();
		while (sock != null && dis != null && kicker != null) 
		{
			try 
			{
				String str = dis.readLine();
				System.out.println("Got: "+str);
				if(str != null) 
					if(str.indexOf("||") != -1)
					{
						StringTokenizer st = new StringTokenizer(str,"||");
						String cmd = st.nextToken();
						String val = st.nextToken();
						if(cmd.equals("list"))
						{ 
						}
						else
							if(cmd.equals("logout")) 
							{
								parent.outputArea.append(val+"\n");
								parent.validate();
							}
							else
								if(cmd.equals("login")) 
								{
									parent.outputArea.append(st.nextToken()+"\n");
								}
								else
									parent.outputArea.append( val + "\n" );
					}
					else
						parent.outputArea.append(str + "\n");
			}
			catch (IOException e)
			{
				System.out.println("Connection lost.");
				parent.outputArea.append("Connection lost.\n" + e);
				kicker.stop();	
			}
		}	
	}

	public void kicker_stop() 
	{
		output("logout||"+name);
		sleep_time(2000);
		try
		{
			dis.close();
			ps.close();
			sock.close();
		}
		catch (Exception e)
		{
		}
		sock = null;
		kicker.stop();
		kicker = null;
	}

	public boolean output(String str) 
	{
		try
		{
			ps.println(str);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}

	void sleep_time(int time)
	{
		try 
		{
			Thread.sleep(time);
		}
		catch (InterruptedException e)
		{
			
		}
	}
}
