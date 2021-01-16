//Pokemon Database/Repository - Kellem Deuitch
import java.awt.event.*;
import java.awt.*;
import java.io.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JButton;

public class Repository 
{
	public static void main(String[] args) 
	{
		JFrame j = new JFrame("");
	    j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    j.getContentPane().add(new Display(), BorderLayout.CENTER);
	    j.setSize(600, 400);
	    j.setVisible(true);
	}//sets up the GUI for image and data output
}

class Display extends JPanel implements MouseListener
{
	File mainIndex = new File("dataIndex.txt"); //index holding all image data
	BufferedReader buff;
	ArrayList<Item> imKeep; //list for holding Pokemon images/data
	ArrayList<Integer> ordering; //holds order of the list
	String ordername = "numerical";
	ArrayList<String> filters; //holds any filters that are applied
	String usefilt = null;
	int dispcount; //counts position in the list as iterated
	Scanner scans;
	
	public Display() //constructor for JPanel
	{
		super();
		addMouseListener(this);
		
		imKeep = new ArrayList<Item>(0);
		try 
	    {
			ordering = new ArrayList<Integer>(0); //Initialise arraylists
			filters = new ArrayList<String>(0);
			reorder("numerical"); //set starting order as numerical
			
	    	buff = new BufferedReader(new FileReader(mainIndex));
	    	String reader = buff.readLine();
		    if(reader != null)
		    {
		    	while(reader != null) //read data from mainIndex into imKeep items
		    	{
		    		int number = Integer.parseInt(reader);
		    		String imname = buff.readLine();
		    		String name = buff.readLine();
		    		Item adds = new Item(number, imname, name);
		    		String holds = buff.readLine();
		    		adds.setType(holds);
		    		imKeep.add(adds);
		    		reader = buff.readLine();
		    	}
		    }
		    buff.close();
	    }
	    catch(FileNotFoundException ee) //if file not found
	    {
	    	System.out.println("File not found");
	    	return;
	    }
	    catch(IOException ee) //to catch other file exceptions
	    {
	    	return;
	    }
		dispcount = 0; //start at first item (numerically)
		scans = new Scanner(System.in);
		System.out.println("Click on image to move through list");
	}//Display
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		
		imKeep.get(ordering.get(dispcount)).im.draw(g); //draw first Pokemon
		g.setFont(new Font("Arial",0,14));
		g.drawString(ordername + " order", 20, 20); //show the list ordering
		//draw Pokemon number
		g.drawString("#"+imKeep.get(ordering.get(dispcount)).number, 30, 200);
		//draw Pokemon name
		g.drawString(imKeep.get(ordering.get(dispcount)).name, 30, 230);
		//print Pokemon type(s)
		if(imKeep.get(ordering.get(dispcount)).type.size()==2)
			g.drawString(imKeep.get(ordering.get(dispcount)).type.get(0)+", "
				+imKeep.get(ordering.get(dispcount)).type.get(1), 30, 260);
		else if(imKeep.get(ordering.get(dispcount)).type.size()==1)
			g.drawString(imKeep.get(ordering.get(dispcount)).type.get(0), 30, 260);
		
		requestFocus();
	}//paintComponent
	
	public void mouseClicked(MouseEvent e)
	{
		//if image clicked, move to next Pokemon
		if(imKeep.get(dispcount).im.contains(e.getX(), e.getY()))
		{
			dispcount++;
			if(usefilt != null) shortlist(); //check if filters apply to next Pokemon
			if(dispcount>=imKeep.size()) //check if list end reached
			{
				commandblock(); //run protocol for user input
			}
		}
		repaint();
	}//mouseClicked
	
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	
	public void commandblock() //user input commands
	{
		System.out.println("Enter command (add, order, filter, unfilter, exit): ");
		Boolean looper = false;
		while(!looper) //will run until a command is recognized
		{
			looper = true;
			String runner = scans.nextLine();
			if(runner != null)
			{
				switch(runner)
				{
				case "add": //add new Pokemon to the bank
					add();
					break;
				case "order": //change to numerical, alphabetical order
					System.out.println("'numerical' or 'alphabetical'?");
					String choice = scans.next();
					if(choice.equals("numerical") || choice.equals("alphabetical"))
						reorder(choice); //reordering method
					else
					{
						System.out.println("Invalid order");
						looper = false;
					}
					break;
				case "filter": //see specific types (can include or omit)
					System.out.println("Enter desired types (',' separated):");
					filter(scans.next()); //run filtering method
					break;
				case "unfilter": //remove any current filters
					filter(null); //run filtering method to clear it
					break;
				case "exit": //save & terminate program
					exit();
					break;
				default: //if input not recognized
					System.out.println("Not a valid command");
					looper = false;
				}
			}
		}
		dispcount = 0; //reset list
		if(usefilt != null) shortlist(); //check if first item is allowed by filters
	}//commandblock
	
	public void add() //add new Pokemon to the database
	{
		//user inputs data on the new Pokemon
		System.out.println("Enter number, image title ('images/...'), name separated by spaces:");
		int num = scans.nextInt();
		String imnum = scans.next();
		String name = scans.next();
		
		Item adds = new Item(num, imnum, name); //create new Pokemon item
		Boolean confirm = false;
		while(!confirm) //user enters type data, make sure not null
		{
			System.out.println("Enter type (',' separated, max 2):");
			String types = scans.next();
			confirm = adds.setType(types); //will return true if successful
			if(!confirm) System.out.println("Nothing was entered. Try again:");
		}
		adds.add(adds, imKeep); //add to master list - new method under class Item
		//adds.add will also update the order indexes
		reorder(ordername); //update orders to include addition
		System.out.println("New Pokemon added!");
	}//add
	
	public void reorder(String choice) //changes the Pokemon list display order
	{
		File orders;
		//check if user wants numerical or alphabetical order
		if(choice.equals("numerical")) orders = new File("numIndex.txt");
		else orders = new File("nameIndex.txt");
		try
		{
			buff = new BufferedReader(new FileReader(orders));
			ordering.clear(); //wipe the previous list order
			String reader = buff.readLine();
			while(reader != null) //feed order list from the correct index
			{
				ordering.add(Integer.parseInt(reader));
				reader = buff.readLine();
			}
			buff.close();
			//change order name that displays on GUI
			if(choice.equals("numerical")) ordername = "numerical";
			else if(choice.equals("alphabetical")) ordername = "alphabetical";
		}
		catch(IOException ee)//catch any file errors
		{
			System.out.println("Reordering fail");
			return;
		}
		//System.out.println("Reordering complete");
	}//reorder
	
	public void filter(String types) //adds or removes filters on Pokemon list
	{
		if(types != null) //make sure entered filter(s) valid
		{
			if(filters != null) filters.clear();
			String[]filts = types.split(",");
			for(int i=0;i<filts.length;i++)
			{
				filters.add(filts[i]); //add each to the filters list
			}
			//ask if user wants the filters included or omitted
			System.out.println("Include or omit these? ('I' or 'O'):");
			usefilt = scans.next();
			System.out.println("Filter applied");
		}
		else //for removing filters
		{
			filters.clear(); //wipe previous filters list
			usefilt = null;
			System.out.println("Filters removed");
		}
	}//filter
	
	public void shortlist() //makes sure filters are applied
	{
		int origcount = dispcount; //to check if no applicable Pokemon found
		Boolean check = false;
		while(dispcount<imKeep.size()) //runs at max to end of the list
		{
			int typenum = imKeep.get(ordering.get(dispcount)).type.size();
			if(typenum==2) //for Pokemon with 2 types
			{
				String type1 = imKeep.get(ordering.get(dispcount)).type.get(0);
				String type2 = imKeep.get(ordering.get(dispcount)).type.get(1);
				for(int i=0;i<filters.size();i++) //run through filters list
				{
					if(usefilt.equals("I")) //inclusion of filters
					{
						//check if either type matches a filter
						if(type1.equals(filters.get(i)) || type2.equals(filters.get(i)))
							check = true;
					}
					else //omission of filters
					{
						//check if both types don't match any filter
						if(!type1.equals(filters.get(i)) && !type2.equals(filters.get(i)))
							check = true;
					}
				}
			}
			else if(typenum ==1) //for Pokemon with 1 type
			{
				String type = imKeep.get(ordering.get(dispcount)).type.get(0);
				for(int i=0;i<filters.size();i++) //run through filters list
				{
					if(usefilt.equals("I")) //inclusion of filters
					{
						//check if type matches a filter
						if(type.equals(filters.get(i)))
							check = true;
					}
					else //omission of filters
					{
						//check if type doesn't match any filter
						if(!type.equals(filters.get(i)))
							check = true;
					}
				}
			}
			if(!check) dispcount++; //if no Pokemon made it through, iterate and loop
			else return; //Pokemon escaped filters, allow to draw
		}
		//if whole list checked and no matching Pokemon found
		if(origcount==0) System.out.println("No acceptable Pokemon for this filter");
		commandblock(); //return to the command input
	}//shortlist
	
	public void exit() //saves and terminates the program
	{
		FileWriter rewrite;
		try
		{
			rewrite = new FileWriter("dataIndex.txt");
			BufferedWriter out = new BufferedWriter(rewrite);
			for(int j=0;j<imKeep.size();j++) //rewrites data file with all updated data
			{
				out.write(String.valueOf(imKeep.get(j).number));
				out.newLine();
				out.write(imKeep.get(j).imname);
				out.newLine();
				out.write(imKeep.get(j).name);
				out.newLine();
				for(int k=0;k<imKeep.get(j).type.size();k++)
				{
					out.write(imKeep.get(j).type.get(k));
					if(k<imKeep.get(j).type.size()-1) 
						out.write(",");
				}
				out.newLine();
			}
			out.close();
			System.out.println("Program saved & terminated");
			System.exit(1);
		}
		catch(IOException ee) //catches file errors
		{
			System.out.println("Exit fail");
			return;
		}
	}//exit
}//Display class

class Item //the Pokemon/image structure
{
	int number; //Pokedex number
	ImageShape im; //an Image with a contains() method (for Pokemon image)
	String imname; //name of the image
	String name; //Pokemon name
	ArrayList<String> type; //Pokemon type(s), max 2
	
	public Item(int number, String imname, String name)
	{
		this.number = number;
		im = new ImageShape();
		this.imname = imname;
		im.setPicture(imname);
		this.name = name;
		type = new ArrayList<String>(0);
	}//constructor
	
	public Boolean setType(String types) //sets the Pokemon's type
	{
		if(types != null) //make sure input is valid
		{
			String[]typearr = types.split(",");
			for(int i=0;i<typearr.length;i++) //add the type(s) to the type list
				type.add(typearr[i]);
			return true;
		}
		else return false;
	}//setType
	
	public void add(Item toAdd, ArrayList<Item> list) //adds new Pokemon into order indexes
	{
		list.add(toAdd); //adds new Pokemon to the data index
		
		ArrayList<Integer> store; //will hold previous index data
		File index;
		for(int i=0;i<2;i++) //will update both indexes
		{
			if(i==0) index = new File("nameIndex.txt");
			else index = new File("numIndex.txt");
			try
			{
				BufferedReader buff = new BufferedReader(new FileReader(index));
				String reader = buff.readLine();
				store = new ArrayList<Integer>(0);
				while(reader != null) //read previous data into temp holding
				{
					Integer storeInt = Integer.parseInt(reader);
					store.add(storeInt);
					reader = buff.readLine();
				}
				buff.close();
				int count = 0;
				if(i==0) //input new data index reference into store based alphabetically
				{
					//iterates through until correct position found
					while(count < store.size() && toAdd.name.compareTo(list.get(store.get(count)).name) > 0)
					{
						count++;
					}
				}
				else //input new data index reference into store based numerically
				{
					//iterates through until correct position found
					while(count < store.size() && toAdd.number > list.get(store.get(count).intValue()).number)
					{
						count++;
					}
				}
				store.add(count, list.size()-1);
			}
			catch(IOException ee) //catches file errors
			{
				System.out.println("File not found");
				return;
			}
			//rewrites each index with the new Pokemon reference
			try
			{
				FileWriter rewrite;
				if(i==0) rewrite = new FileWriter("nameIndex.txt");
				else rewrite = new FileWriter("numIndex.txt");
				BufferedWriter out = new BufferedWriter(rewrite);
				for(int j=0;j<store.size();j++)
				{
					out.write(String.valueOf(store.get(j).intValue()));
					out.newLine();
				}
				out.close();
			}
			catch(IOException ee) //catches file errors
			{
				if(i==0) System.out.println("Name update fail");
				else System.out.println("Num update fail");
				return;
			}
		}
	}//add
}//Item class
