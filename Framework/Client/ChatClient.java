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

public class ChatClient extends JApplet implements Runnable 
{
	// Constants
	private final int TOTAL_CARDS = 52, NO_OF_PLAYERS = 4;
	private final int SIZE_OF_HAND = TOTAL_CARDS/NO_OF_PLAYERS;
	private final int card_spacing = 25, dist = 30, margin = 12;
	private final int extra_width = 300, frame_width = 1024, frame_height = 800, frame_height_actual = 768;
	private final int button_width = 100, button_height = 30;
	private final int card_width = 72, card_height = 96;
	private final String [] names = {"North", "East", "South", "West"};
	private final String directory = "images/";
	//network
	Socket sock;
	DataInputStream dis;
	PrintStream ps;
	Thread kicker = null;
	
	// JAPPLET Stuff....
	JPanel p1, p2;
	private JLabel[] cur_hand = new JLabel[NO_OF_PLAYERS];
	private JLabel[] player_label = new JLabel[NO_OF_PLAYERS];
	private JLabel[][] handLbl = new JLabel[NO_OF_PLAYERS][ SIZE_OF_HAND ]; 
	private JLabel background, scoreLbl, background2;
	JTextField inputField;
	JTextArea outputArea;
	JButton B1,B2,B3,B4;
	JScrollPane scrollPane;
	
	ImageIcon[][] Image_Icons = new ImageIcon[NO_OF_PLAYERS][SIZE_OF_HAND];
	ImageIcon card_back;

	String name, theHost;
	int thePort;
	private final SymAction lSymAction = new SymAction();

	public void init() 
	{	
		try {
		/* first, assign a BorderLayout and add the two Panels */
		p2 = new JPanel();
		p2.setLayout(null);
		p2.setBounds(frame_width,0,extra_width,frame_height_actual);
		//p2.setBackground(java.awt.Color.black);
		p1 = new JPanel();
		p1.setLayout(null);
		p1.setBounds(0,0,frame_width,frame_height_actual);
		setSize(frame_width+extra_width,frame_height_actual);
		add(p1);
		add(p2);

		String temp;
		temp = getParameter("host");
		if( temp == null)
			theHost = "localhost";
		else
			theHost = temp;
		temp = getParameter("port");

		try 
		{
			thePort = Integer.valueOf(temp).intValue();
		}
		catch(Exception e)
		{
			thePort = 31123;
		}
		
		loadImageIcons();

		setPanel2();
		register_buttons();
		
		initJLabels();

		setBackground();
		
		//setIconsJLabels();

		}
		catch (NullPointerException e)
		{
			
		}
	}

	public void run() {
		
		int count = 0;

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
					outputArea.append("Unable to contact host. Retrying...\n" + e);
				sock = null;
			}
			try
			{
				Thread.sleep( 5000 );
			}
			catch(Exception e)
			{
			}
			count++;
		}

		output("login||"+name);
		outputArea.append("Logged in to server successfully.\n");
		B2.setEnabled(true);
		B1.setEnabled(false);
		B3.setEnabled(true);
		B4.setEnabled(true);
		setIconsJLabels();
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
								outputArea.append(val+"\n");
								validate();
							}
							else
								if(cmd.equals("login")) 
								{
									outputArea.append(st.nextToken()+"\n");
								}
								else
									outputArea.append( val + "\n" );
					}
					else
						outputArea.append(str + "\n");
			}
			catch (IOException e)
			{
				System.out.println("Connection lost.");
				kicker.stop();
			}
		}	
	}

	public void stop() 
	{
		output("logout||"+name);
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
		outputArea.append("Logged out from server.\n");

		/* reset our affected GUI components */
		B2.setEnabled(false);
		B1.setEnabled(true);
		inputField.setText("<Enter Name and Press Login>");
		inputField.addMouseListener(lSymAction);
		p2.layout();
		removeIconsJLabels();

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

	class SymAction implements java.awt.event.ActionListener, MouseListener
	{
		public void actionPerformed(java.awt.event.ActionEvent event)
		{
			Object object = event.getSource();
			if (object == B1)
				login_actionPerformed(event);
			else if (object == B2)
				logout_actionPerformed(event);
			else if (object == B3)
				say_actionPerformed(event);
			else if (object == B4)
				whisper_actionPerformed(event);
		}
		public void mouseClicked(MouseEvent e) 
		{
			Object object = e.getSource();
			if (object == inputField)
			{
				inputField.setText("");
				inputField.removeMouseListener(lSymAction);
			}
			/*if (object == background)
			{
				after_one_round();	
				if (game_over == true)
				{
					after_one_game();
					getContentPane().remove(background);
					add_JLabels_to_pane();
					getContentPane().add(background);
					getContentPane().repaint();
					
				}
				remove_mouse_event(background);
				scoreLbl.setText(null);
				return;
			}
			int pos = card_key_map_for_JLabel(object, cur_player);
			if ( pos >= 0 )
			{
				Card card = cardDeck.getCard(pos);	
				if (card != null)
				{
					int k = cur_player;
					int i = pos % SIZE_OF_HAND;	
					handLbl[k][i].setIcon(null);	
					//remove_mouse_event(handLbl[k][i]);
					getContentPane().remove(handLbl[k][i]);	
					getContentPane().repaint();	
					cur_hand[k].setIcon(card.getCardImage());
					after_one_card(card);
					//cur_hand[k].setText( card.toString() );
				}
			}
			*/
		}	
		/* Ignore all  the following events	*/
		public void mouseExited(MouseEvent e) {}
		public void mouseDragged(MouseEvent e) {}
		public void mouseMoved   (MouseEvent e) {} 
		public void mouseEntered(MouseEvent e) {}  
		public void mouseReleased(MouseEvent e) {}  
		public void mousePressed(MouseEvent e) {}  

	}

	public boolean handleEvent(Event evt) 
	{
		/* if the Event is not one of the ones we can handle, we should pass it along the chain-of-command to our super-class */
		return super.handleEvent(evt);
	}
	
	void register_buttons()
	{
		B1.addActionListener(lSymAction);
		B2.addActionListener(lSymAction);
		B3.addActionListener(lSymAction);
		B4.addActionListener(lSymAction);
	}

	public boolean login_actionPerformed(java.awt.event.ActionEvent event)	
	{
		outputArea.append("Logging in...\n");
		name = inputField.getText(); /* find out what alias the user wants to use */
		inputField.setText("");

		/* create a new Thread and start it */
		kicker = new Thread(this);
		kicker.start();
		return true;
	}

	public boolean logout_actionPerformed(java.awt.event.ActionEvent event)	
	{
		outputArea.append("Logging out...\n");
		/* stop the Thread (which will disconnect) */
		stop();
		kicker.stop();
		return true;
	}
	
	public boolean say_actionPerformed(java.awt.event.ActionEvent event)
	{
		if (inputField.getText() != null)
		{
			output("say||"+inputField.getText());
			inputField.selectAll();
			inputField.setText("");
		}
		return true;
	}


	public boolean whisper_actionPerformed(java.awt.event.ActionEvent event)
	{
		//outputArea.append("You whisper to "+name+ ": "+inputField.getText()+"\n");
		output("whisper||"+inputField.getText()+"||"+name );
		inputField.selectAll();
		return true;
	}

	void setBackground()
	{
		//Set Background 
		background = new JLabel();
		p1.add(background);
		String imageFile = directory + "background.gif";
		ImageIcon backImage = new ImageIcon( getImage( getCodeBase(), imageFile ) );
		//URL imageURL = cldr.getResource(imageFile);
		//ImageIcon backImage = new ImageIcon(imageURL);
		background.setBounds(0,0,frame_width, frame_height_actual);
		background.setIcon(backImage);
	}

	void setPanel2()
	{
		/* next create the Field used for input.  For fun, make it 80 columns wide.  Add it to the south Panel */
		inputField = new JTextField(20);
		inputField.setBounds(frame_width,(frame_height_actual/2),extra_width ,30);
		Font font = new Font("Verdana",Font.ITALIC, 12);
		Font font2 = new Font("Verdana",Font.ITALIC, 6);
		inputField.setBackground(Color.LIGHT_GRAY);
		//inputField.setOpaque(false);
		inputField.setFont(font);
		inputField.setText("<Enter Name and Press Login>");
		inputField.addMouseListener(lSymAction);
		p2.add( inputField );

		/* create the output Area.  Make it 10 rows by 60 columns.  Add it to north Panel  don't let the user edit the contents, and make the background color Cyan - because it looks nice */
		outputArea = new JTextArea(10, 60);
		outputArea.setBounds(frame_width,((frame_height_actual/2) + 30),extra_width,((frame_height_actual/2) - 30));
		p2.add(outputArea);
		outputArea.setEditable(false);
		outputArea.setFont(font);
		outputArea.setOpaque(false);

		/* now for the Buttons.  Make the first Button to let the user "login" */
		B1 = new JButton("login");
		int buttonPos = frame_width + (extra_width - (2 * button_width))/2;
		B1.setBounds(buttonPos, (frame_height_actual/6), button_width, button_height);  
		p2.add(B1);

		/* The second Button allows the user to "logout", but is initially disabled */
		B2 = new JButton("logout");
		B2.setBounds(buttonPos+button_width, (frame_height_actual/6), button_width, button_height);  
		p2.add(B2);
		B2.setEnabled(false);

		/* third Button to say Iam Ready */
		B3 = new JButton("say");
		B3.setBounds(buttonPos, ((frame_height_actual/2) - (frame_height_actual/6) - button_height), button_width, button_height);  
		B3.setEnabled(false);
		p2.add(B3);
		
		/* Fourth Button to send message */
		B4 = new JButton("whisper");
		B4.setBounds(buttonPos + button_width, ((frame_height_actual/2) - (frame_height_actual/6) - button_height), button_width, button_height);  
		B4.setEnabled(false);
		p2.add(B4);

		setBackground2();
	}
	
	void setBackground2()
	{
		//Set Background2 
		background2 = new JLabel();
		p2.add(background2);
		String imageFile = directory + "background2.gif";
		ImageIcon backImage = new ImageIcon( getImage( getCodeBase(), imageFile ) );
		//URL imageURL = cldr.getResource(imageFile);
		//ImageIcon backImage = new ImageIcon(imageURL);
		background2.setBounds(frame_width,0,extra_width,frame_height_actual);
		background2.setIcon(backImage);
	}
	
	void initJLabels()
	{
		int xPos = 0, yPos = 0;
		int plxPos = 0, plyPos = 0, plw =40, plh = 30;
		int chxPos = 0, chyPos = 0;
		
		for ( int k = 0 ; k < NO_OF_PLAYERS ;k++)
		{
			player_label[k] = new JLabel();
			cur_hand[k] = new JLabel();
			switch (k)
			{
				case 0:
					{
						xPos = ((frame_width-card_width)/2) +(6 * card_spacing);
						yPos = margin;
						plxPos = xPos+6+card_width;
						plyPos = yPos+(card_height/2) ;
						chxPos = (frame_width/2) - (card_width/2);
						chyPos = (frame_height_actual/2) - dist - card_height;
						break;
					}
				case 3:
					{
						xPos = margin;
						yPos = ((frame_height-card_height)/2) +(6 * card_spacing);
						plxPos = xPos + (card_width/2);
						plyPos = yPos +6+card_height;
						chxPos = (frame_width/2) - dist - card_width;
						chyPos = (frame_height/2) - (card_height/2);
						break;
					}
				case 2:
					{
						xPos = ((frame_width-card_width)/2) +(6 * card_spacing);
						yPos = frame_height_actual - margin - card_height;
						plxPos = xPos+6+card_width;
						plyPos = yPos+(card_height/2) ;
						chxPos = (frame_width/2) - (card_width/2);
						chyPos = (frame_height_actual/2) + (2 *dist);
						break;
					}
				case 1:
					{
						xPos = frame_width - margin - card_width;
						yPos = ((frame_height-card_height)/2) +(6 * card_spacing);
						plxPos = xPos + (card_width/2);
						plyPos = yPos+6+card_height;
						chxPos = (frame_width/2) + dist;
						chyPos = (frame_height/2) - (card_height/2);
						break;
					}
			}
			
			p1.add(player_label[k]);
			player_label[k].setForeground(java.awt.Color.black);
			player_label[k].setFont(new Font("Dialog", Font.BOLD, 10));
			player_label[k].setBounds(plxPos,plyPos,plw,plh);

			p1.add(cur_hand[k]);
			cur_hand[k].setForeground(java.awt.Color.black);
			cur_hand[k].setBounds(chxPos,chyPos,card_width,card_height);

			for ( int i = 0 ; i < SIZE_OF_HAND; i++ ) 
			{
				handLbl[k][i] = new JLabel();
				handLbl[k][i].setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
				handLbl[k][i].setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
				handLbl[k][i].setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
				//handLbl[k][i].setText("Card");
				handLbl[k][i].setOpaque(false);
				handLbl[k][i].setToolTipText("This is a card.");
				p1.add(handLbl[k][i]);
				handLbl[k][i].setForeground(java.awt.Color.black);
				handLbl[k][i].setFont(new Font("Dialog", Font.BOLD, 10));
				handLbl[k][i].setBounds(xPos,yPos,card_width,card_height);
				if (k % 2 == 0)
					xPos-=card_spacing;
				else
					yPos-=card_spacing;
			}
		}

		//Initiate Score Label
		/*scoreLbl = new JLabel();
		scoreLbl.setToolTipText("Sum of all cards");
		p1.add(scoreLbl);
		scoreLbl.setForeground(java.awt.Color.black);
		scoreLbl.setBounds(636,156,204,29);

		*/
	}

	void setIconsJLabels()
	{
		for (int k = 0; k < NO_OF_PLAYERS; k++)
		{
			for (int i = 0; i < SIZE_OF_HAND; i++)
			{
				if (k <2)
					handLbl[k][SIZE_OF_HAND - i - 1].setIcon(card_back);
				else
					handLbl[k][i].setIcon(card_back);
				//handLbl[k][i].setIcon(Image_Icons[k][i]);
				p1.repaint();
				sleep_time(15);
			}
		player_label[k].setText(names[k]);

		}
	}

	void removeIconsJLabels()
	{
		for (int k = 0; k < NO_OF_PLAYERS; k++)
		{
			for (int i = 0; i < SIZE_OF_HAND; i++)
			{
				handLbl[k][i].setIcon(null);
			}
		player_label[k].setText(null);

		}
		p1.repaint();
	}
	void loadImageIcons()
	{
		String cards = "a23456789tjqk";
		String suit = "cshd";
		for (int k = 0; k < NO_OF_PLAYERS; k++)
		{
			for (int i = 0; i < SIZE_OF_HAND; i++)
			{
				String imageFile = directory + cards.charAt(i) + suit.charAt(k) +".gif";
				Image_Icons[k][i] = new ImageIcon( getImage( getCodeBase(), imageFile ) );
			}

		}
		
		String imageFile = directory + "cardback.gif";
		//String imageFile = directory + "chelcardback.gif";
		card_back = new ImageIcon( getImage( getCodeBase(), imageFile ) );
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
