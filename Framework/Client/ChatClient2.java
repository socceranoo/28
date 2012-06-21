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

public class ChatClient2 extends JApplet implements Runnable 
{
	/* Constants
	private final int SIZE_OF_HAND = TOTAL_CARDS/NO_OF_PLAYERS;
	private final String [] names = {"North", "East", "South", "West"};
	*/
	private final int TOTAL_RANKS = 13, TOTAL_SUITS= 4, NO_OF_CARD_BACKS= 2;
	static String Thread_Switcher = "kicker";
	
	private final int card_spacing = 25, dist = 30, margin = 12;
	private final int extra_width = 300, frame_width = 1024, frame_height = 800, frame_height_actual = 768;
	private final int button_width = 100, button_height = 30;
	private final int new_card_width = 150, new_card_height = 215;
	//private final int card_width = new_card_width, card_height = new_card_height;
	private final int card_width = 72, card_height = 96;
	private final int player_position = 2; // Default is South position
	private final String directory = "images/";
	//network
	Socket sock;
	DataInputStream dis;
	PrintStream ps;
	Thread kicker = null;

	Socket sock2;
	ObjectOutputStream os;
	ObjectInputStream is;
	Thread finisher = null;

	private int TOTAL_CARDS = 0, NO_OF_PLAYERS = 0, SIZE_OF_HAND = 0;  
	private String [] names = null;
	private int player_id = -1;
	private int pos = -1;
	private int trumpCardPos;
	private int whoHasSetTrump = -1;
	private int [][]pos_change = null;
	private int []init_array =null;
	private int []card_array = null;


	// JAPPLET Stuff....
	JPanel p1, p2;
	private JLabel[] cur_hand;
	private JLabel[] player_label;
	private JLabel[][] handLbl; 
	private JLabel background, scoreLbl, background2, trump_label;
	JTextField inputField;
	JTextArea outputArea;
	JButton B1,B2,B3,B4,B5,B6;
	JButton [] bidButtons = new JButton[7];
	JScrollPane scrollPane;
	
	ImageIcon[]Image_Icons = new ImageIcon[52];
	ImageIcon[]card_backs = new ImageIcon[NO_OF_CARD_BACKS];
	ImageIcon card_back;
	ImageIcon trumpImageIcon;

	String name, theHost;
	int thePort;
	private final SymAction lSymAction = new SymAction();

	public boolean setTrumpAction;

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
		
		setTrumpAction = false;
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
		
		setBackground();
		
		}
		catch (NullPointerException e)
		{
			
		}
	}

	public void run() {
		
		int count = 0;
		if (Thread_Switcher.equals("finisher"))
		{
			run2();
			return;	
		}
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
				sleep_time(5000);
			}
			sleep_time(100);
			count++;
		}

		output("login||"+name);
		outputArea.append("Logged in to server successfully.\n");
		B2.setEnabled(true);
		B1.setEnabled(false);
		B5.setEnabled(true);
		inputField.addActionListener(lSymAction);
		//setIconsJLabels();
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
				kickerstop();
				kicker.stop();
			}
		}	
	}

	public void run2()
	{
		int count = 0;	
		while (sock2 == null && finisher != null )
		{
			try
			{
				sock2 = new Socket(theHost, thePort+1);
				os = new ObjectOutputStream(sock2.getOutputStream());
				os.flush();
				is=new ObjectInputStream(sock2.getInputStream());		
			}
			catch (Exception e)
			{
				System.out.println("not able to connect to the game");
				if (count < 10)
					outputArea.append("not able to connect to the game.\n" + e);
				sock2 = null;
				sleep_time(5000);
			}
			sleep_time(100);
			count++;
		}
		
		outputArea.append("Logged in to game successfully.\n");
		try {

			os.writeObject(name);	
			os.flush();
			while (sock2 != null && is != null && finisher != null) 
			{
				String str = (String) is.readObject();
				System.out.println("Got: "+str);
				if(str != null) 
				{
					if(str.equals("init"))
					{
						init_array = (int [])is.readObject();
						outputArea.append("Received init " + init_array[0]+" "+init_array[1]+" "+init_array[2]+" "+"\n");
						String [] temp = (String [])is.readObject();
						outputArea.append("Received name" +temp[0]+temp[1]+temp[2]+temp[3]+"\n");
						NO_OF_PLAYERS=init_array[0];
						TOTAL_CARDS=init_array[1];
						player_id=init_array[2];
						SIZE_OF_HAND = TOTAL_CARDS/NO_OF_PLAYERS;
						card_array = new int[SIZE_OF_HAND];
						assign_names(temp);
						initJLabels();
					}
					else if(str.equals("cards"))
					{
						int [] temp = (int [])is.readObject();
						//outputArea.append("\nReceived cards"); 
						for (int i = 0; i < SIZE_OF_HAND; i++)
						{
							card_array[i] = temp[i];
						//	outputArea.append(" "+card_array[i]);
						}
						setIconsJLabels();

					}
					else if(str.equals("first"))
					{
						setIconsJLabels_player(card_array, 1);
					}
					else if(str.equals("second"))
					{
						setIconsJLabels_player(card_array, 2);
					}
					else if(str.equals("bid"))
					{
						B3.setEnabled(true);
						B4.setEnabled(true);
						//Put sound here	
					}
					else if(str.equals("pos"))
					{
						int cur_player= (Integer)is.readObject();
						int pos = (Integer)is.readObject();
						int idx = (Integer)is.readObject();
						after_other_players_card(cur_player, pos, idx);
						/*String done = "done";
						os.writeObject(done);
						os.flush();*/
					}
					else if(str.equals("play"))
					{
						add_mouse_player();	
						B3.setEnabled(false);
						B4.setEnabled(false);
						if (whoHasSetTrump == player_id)
							B6.setEnabled(true);
						//play sound here too
					}
					else if(str.equals("valid"))
					{
						int valid = (Integer)is.readObject();
						if (valid == 1)
						{
							after_my_card();
						}
					}
					else if(str.equals("round"))
					{
						removeIconCurrentHand();
						int score = (Integer)is.readObject();
						scoreLbl.setText("Round:"+score);
					}
					else if(str.equals("trumpset"))
					{
						int cur_player= (Integer)is.readObject();
						int pos = (Integer)is.readObject();
						setTrumpLabel(cur_player, pos);
					}
					else if (str.equals("settrump"))
					{
						setTrumpAction = true;
						add_mouse_player();	
					}
					else if(str.equals("game"))
					{
						int score = (Integer)is.readObject();
						scoreLbl.setText("Game:"+score);
						add_remove_background(false);
						add_JLabels_to_pane();
						setIconsJLabels();
						add_remove_background(true);
						p1.repaint();
					}
				}
			}	
		}
		catch (Exception e)
		{
			System.out.println("Connection lost.");
			finisherstop();
			//finisher.stop();	
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
			is.close();
			os.close();
			sock2.close();
		}
		catch (Exception e)
		{
		}
		sock = null;
		sock2 = null;
		outputArea.append("Logged out from server.\n");
		kicker = null;
		finisher = null;
	}

	public void kickerstop()
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
		kicker = null;

		/* reset our affected GUI components */
		B2.setEnabled(false);
		B1.setEnabled(true);
		inputField.setText(name);
		inputField.addMouseListener(lSymAction);
		inputField.removeActionListener(lSymAction);
		p2.layout();
	}	
	

	public void finisherstop()
	{
		try
		{
			is.close();
			os.close();
			sock2.close();
		}
		catch (Exception e)
		{
		}
		sock2 = null;
		outputArea.append("Disconnected from game.\n");

		removeIconsJLabels();
		finisher = null;
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
				bid_actionPerformed(event);
			else if (object == B4)
				pass_actionPerformed(event);
			else if (object == B5)
				iamready_actionPerformed(event);
			else if (object == B6)
				reveal_trump_actionPerformed(event);
			else if (object == inputField)
				say_actionPerformed(event);
				
		}
		public void mouseClicked(MouseEvent e) 
		{
			Object object = e.getSource();
			if (object == inputField)
			{
				inputField.setText("");
				inputField.removeMouseListener(lSymAction);
				return;
			}
			if (object == trump_label)
			{
				if ((ImageIcon)trump_label.getIcon() == card_back)
					trump_label.setIcon(trumpImageIcon);
				else
					trump_label.setIcon(card_back);
				return;
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
			*/
			pos = getIndexOfCardPressed(object);
			if ( pos >= 0 )
			{
				sendClickedCard(pos);
				if (setTrumpAction)
				{
					whoHasSetTrump = player_id;
					trumpCardPos = pos;
					setTrumpLabel(player_id, pos);
					setTrumpAction = false;
					add_mouse_event(trump_label);
					remove_mouse_player();
				}
			}
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
		B5.addActionListener(lSymAction);
		B6.addActionListener(lSymAction);
	}

	public boolean login_actionPerformed(java.awt.event.ActionEvent event)	
	{
		Thread_Switcher = "kicker";
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
		kickerstop();
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

	public boolean bid_actionPerformed(java.awt.event.ActionEvent event)
	{
		try 
		{
			int bid = 14;
			if (inputField.getText() != null)
			{
				String temp = inputField.getText();
				bid = Integer.parseInt(temp);
				inputField.setText("");
			}
			if (bid < 14 && bid > 28)
			{
				bid = 14;	
			}
			os.writeObject(bid);	
			os.flush();
		}	
		catch(Exception e)
		{

		}
		
		B3.setEnabled(false);
		B4.setEnabled(false);
		return true;
	}

	public boolean pass_actionPerformed(java.awt.event.ActionEvent event)
	{
		try 
		{
			int bid = 0;
			os.writeObject(bid);	
			os.flush();
		}	
		catch(Exception e)
		{

		}
		B3.setEnabled(false);
		B4.setEnabled(false);
		return true;
	}

	public boolean reveal_trump_actionPerformed(java.awt.event.ActionEvent event)
	{
		try 
		{
			// Send the trump to all the players or request the trump to 
			//os.writeObject(bid);	
			//os.flush();
			reveal_trump();
		}	
		catch(Exception e)
		{

		}
		return true;
	}

	public boolean whisper_actionPerformed(java.awt.event.ActionEvent event)
	{
		outputArea.append("You whisper to "+name+ ": "+inputField.getText()+"\n");
		output("whisper||"+inputField.getText()+"||"+name );
		inputField.selectAll();
		return true;
	}

	public boolean iamready_actionPerformed(java.awt.event.ActionEvent event)
	{
		Thread_Switcher = "finisher";
		outputArea.append("Getting ready for the game\n");
		inputField.selectAll();
		outputArea.append("Iam Ready for the game\n");
		B5.setEnabled(false);
		finisher = new Thread(this);
		finisher.start();
		return true;
	}

	public void reveal_trump()
	{
		B6.setEnabled(false);
		handLbl[player_position][trumpCardPos].setIcon(trumpImageIcon);
		p1.add(handLbl[player_position][trumpCardPos]);
		repaint();
		add_mouse_event(handLbl[player_position][trumpCardPos]);
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
		outputArea = new JTextArea(10, 20);
		outputArea.setBounds(frame_width,((frame_height_actual/2) + 30),extra_width,((frame_height_actual/2) - 30));
		outputArea.setEditable(false);
		outputArea.setFont(font);
		outputArea.setOpaque(false);
		scrollPane = new JScrollPane(outputArea);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy (ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setBounds(frame_width,((frame_height_actual/2) + 30),extra_width,((frame_height_actual/2) - 30));
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque( false );
		p2.add(scrollPane);

		/* now for the Buttons.  Make the first Button to let the user "login" */
		B1 = new JButton("Chat");
		int buttonPos = frame_width + (extra_width - (2 * button_width))/2;
		B1.setBounds(buttonPos, (frame_height_actual/6), button_width, button_height);  
		p2.add(B1);

		/* The second Button allows the user to "logout", but is initially disabled */
		B2 = new JButton("logout");
		B2.setBounds(buttonPos+button_width, (frame_height_actual/6), button_width, button_height);  
		p2.add(B2);
		B2.setEnabled(false);

		/* third Button to say Iam Ready */
		B3 = new JButton("bid");
		B3.setBounds(buttonPos, ((frame_height_actual/2) - (frame_height_actual/6) - button_height), button_width, button_height);  
		B3.setEnabled(false);
		p2.add(B3);
		
		/* Fourth Button to send message */
		B4 = new JButton("pass");
		B4.setBounds(buttonPos + button_width, ((frame_height_actual/2) - (frame_height_actual/6) - button_height), button_width, button_height);  
		B4.setEnabled(false);
		p2.add(B4);

		/* Fifth Button to join */
		B5 = new JButton("Join");
		B5.setBounds(buttonPos, ((frame_height_actual/2) - (frame_height_actual/12) - button_height/2), 2 * button_width, button_height);  
		B5.setEnabled(false);
		p2.add(B5);

		B6 = new JButton("Reveal");
		B6.setBounds(margin, margin+card_height+30, card_width, button_height);  
		B6.setEnabled(false);
		p2.add(B6);

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
		add_remove_background(false);
		cur_hand = new JLabel[NO_OF_PLAYERS];
		player_label = new JLabel[NO_OF_PLAYERS];
		trump_label = new JLabel();
		handLbl = new JLabel[NO_OF_PLAYERS][ SIZE_OF_HAND ]; 
		int xPos = 0, yPos = 0;
		int plxPos = 0, plyPos = 0, plw =40, plh = 30;
		int chxPos = 0, chyPos = 0;
		
		trump_label.setOpaque(false);
		trump_label.setForeground(java.awt.Color.black);
		trump_label.setBounds(margin,margin,card_width,card_height + 20);
		trump_label.setIcon(null);
		trump_label.setText(null);
		p1.add(trump_label);

		for ( int k = 0 ; k < NO_OF_PLAYERS ;k++)
		{
			player_label[k] = new JLabel();
			cur_hand[k] = new JLabel();
			switch (k)
			{
				case 0:
					{
						xPos = ((frame_width-card_width)/2) +((SIZE_OF_HAND-1)/2 * card_spacing);
						yPos = margin;
						plxPos = xPos+(margin/2)+card_width;
						plyPos = yPos+(card_height/2) ;
						chxPos = (frame_width/2) - (card_width/2);
						chyPos = (frame_height_actual/2) - dist - card_height;
						break;
					}
				case 3:
					{
						xPos = margin;
						yPos = ((frame_height-card_height)/2) +((SIZE_OF_HAND-1)/2 * card_spacing);
						plxPos = xPos + (card_width/2);
						plyPos = yPos +(margin/2)+card_height;
						chxPos = (frame_width/2) - dist - card_width;
						chyPos = (frame_height/2) - (card_height/2);
						break;
					}
				case 2:
					{
						xPos = ((frame_width-card_width)/2) +((SIZE_OF_HAND-1)/2 * card_spacing);
						yPos = frame_height_actual - margin - card_height;
						plxPos = xPos+(margin/2)+card_width;
						plyPos = yPos+(card_height/2) ;
						chxPos = (frame_width/2) - (card_width/2);
						chyPos = (frame_height_actual/2) + (2 *dist);
						break;
					}
				case 1:
					{
						xPos = frame_width - margin - card_width;
						yPos = ((frame_height-card_height)/2) +((SIZE_OF_HAND-1)/2 * card_spacing);
						plxPos = xPos + (card_width/2);
						plyPos = yPos+(margin/2)+card_height;
						chxPos = (frame_width/2) + dist;
						chyPos = (frame_height/2) - (card_height/2);
						break;
					}
			}
			
			player_label[k].setForeground(java.awt.Color.black);
			player_label[k].setFont(new Font("Dialog", Font.BOLD, 10));
			player_label[k].setBounds(plxPos,plyPos,plw,plh);
			player_label[k].setText(null);
			p1.add(player_label[k]);

			cur_hand[k].setOpaque(false);
			cur_hand[k].setForeground(java.awt.Color.black);
			cur_hand[k].setBounds(chxPos,chyPos,card_width,card_height);
			cur_hand[k].setIcon(null);
			p1.add(cur_hand[k]);

			for ( int i = 0 ; i < SIZE_OF_HAND; i++ ) 
			{
				handLbl[k][i] = new JLabel();
				handLbl[k][i].setOpaque(false);
				handLbl[k][i].setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
				handLbl[k][i].setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
				handLbl[k][i].setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
				//handLbl[k][i].setText("Card");
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
		scoreLbl = new JLabel();
		scoreLbl.setToolTipText("Sum of all cards");
		scoreLbl.setForeground(java.awt.Color.black);
		scoreLbl.setBounds(636,156,204,29);
		scoreLbl.setText(null);
		p1.add(scoreLbl);

		//setBackground();
		add_remove_background(true);
		p1.repaint();
	}

	void setIconsJLabels()
	{
		int temp = player_position;
		int temp2 = player_id;
		for (int k = 0; k < NO_OF_PLAYERS; k++)
		{
			for (int i = 0; i < SIZE_OF_HAND; i++)
			{
				if (k <2)
					handLbl[k][SIZE_OF_HAND - i - 1].setIcon(card_back);
				else
					handLbl[k][i].setIcon(card_back);
				//handLbl[k][i].setIcon(Image_Icons[k][i]);
				//p1.repaint();
				//sleep_time(15);
			}
			player_label[temp].setText(names[temp2]);
			temp++;
			temp= temp%NO_OF_PLAYERS;
			temp2++;
			temp2 = temp2 % NO_OF_PLAYERS;
		}
		p1.repaint();
	}
	
	void setTrumpLabel(int cur_player, int card_position)
	{
		int pos = (cur_player + player_position - player_id +NO_OF_PLAYERS) % NO_OF_PLAYERS;	
		trumpImageIcon = (ImageIcon)handLbl[pos][card_position].getIcon();
		trump_label.setIcon(card_back);
		trump_label.setText("Set by "+names[cur_player]);
		handLbl[pos][card_position].setIcon(null);
		p1.remove(handLbl[pos][card_position]);
		p1.repaint();

	}
	void setIconsJLabels_player(int [] cards, int option)
	{
		int k = player_position;
		int start, end;
		if (option == 1)	
		{
			start = 0;
			end = SIZE_OF_HAND/2;
		}
		else
		{
			start = SIZE_OF_HAND/2;
			end = SIZE_OF_HAND;

		}
		for (int i = start; i < end; i++)
		{
			handLbl[k][i].setIcon(Image_Icons[cards[i]]);
			p1.repaint();
			sleep_time(15);
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
		for (int k = 0; k < 4; k++)
		{
			for (int i = 0; i < 13; i++)
			{
				String imageFile = directory + cards.charAt(i) + suit.charAt(k) +".gif";
				/*Toolkit toolkit = Toolkit.getDefaultToolkit();
				Image image = toolkit.getImage(imageFile);
				Image scaledImage = image.getScaledInstance(new_card_width, new_card_height, Image.SCALE_DEFAULT);   
				Image_Icons[k*13 +i]=new ImageIcon(scaledImage); */
				Image_Icons[k*13 +i] = new ImageIcon( getImage( getCodeBase(), imageFile ) );
			}

		}
		
		for (int j = 0; j< NO_OF_CARD_BACKS; j++)
		{
			String imageFile = directory + "cardback"+(j+1)+".gif";
			//String imageFile = directory + "chelcardback.gif";
			card_backs[j]= new ImageIcon( getImage( getCodeBase(), imageFile ) );
		}
		card_back = card_backs[0];
	}

	void assign_names(String [] temp)
	{
		names = new String[NO_OF_PLAYERS];
		for (int k = 0; k < NO_OF_PLAYERS; k++)
		{
			if ( k == player_id)	
			{
				names[k] = "You";	
			}
			else
				names[k] = temp[k];
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

	void add_remove_background(boolean choice)
	{
		if (choice == true)
			p1.add(background);	
		else
			p1.remove(background);	
	}

	// player
	void add_mouse_player()
	{
		for ( int i = 0; i < SIZE_OF_HAND; i++ ) 
		{
			handLbl[player_position][i].addMouseListener(lSymAction);
		}
	}

	void remove_mouse_player()
	{
		for ( int i = 0; i < SIZE_OF_HAND; i++ ) 
		{
			handLbl[player_position][i].removeMouseListener(lSymAction);
		}
	}

	int getIndexOfCardPressed(Object o1)
	{
		for ( int i = 0 ; i < SIZE_OF_HAND; i++ ) 
		{
			if (o1 == handLbl[player_position][i])
			{
				return i;
			}
		}	 			 
		return -1;
	}
	
	void after_other_players_card(int cur_player, int cardPos, int cardIndex)
	{
		int pos = (cur_player + +player_position - player_id +NO_OF_PLAYERS) % NO_OF_PLAYERS;	
		handLbl[pos][cardPos].setIcon(null);	
		p1.remove(handLbl[pos][cardPos]);	
		cur_hand[pos].setIcon(Image_Icons[cardIndex]);	
		p1.repaint();	
	}
	
	void sendClickedCard(int pos)
	{
		try 
		{
			os.writeObject(pos);
			os.flush();
		}
		catch (Exception e)
		{

		}
	}

	//Single Label
	void add_mouse_event(JLabel j1)
	{
		j1.addMouseListener(lSymAction);
	}

	void remove_mouse_event(JLabel j1)
	{
		j1.removeMouseListener(lSymAction);
	}

	void add_JLabels_to_pane()
	{  
		for ( int k = 0; k < NO_OF_PLAYERS; k++ ) 
		{
			for ( int i = 0; i < SIZE_OF_HAND; i++ ) 
			{
				p1.add(handLbl[k][i]);
			}
		}
	
	}

	void removeIconCurrentHand()
	{
		for ( int k = 0; k < NO_OF_PLAYERS; k++ ) 
		{
			cur_hand[k].setIcon(null);
		}

	}

	void after_my_card()
	{
		int k = player_position;
		int i = pos;
		if ( pos >=0 )
		{
			ImageIcon temp = (ImageIcon) handLbl[k][i].getIcon();
			handLbl[k][i].setIcon(null);
			remove_mouse_event(handLbl[k][i]);
			p1.remove(handLbl[k][i]);	
			cur_hand[k].setIcon(Image_Icons[card_array[pos]]);
			p1.repaint();	
		}
		remove_mouse_player();
	}
}
