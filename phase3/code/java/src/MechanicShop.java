/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class MechanicShop{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public MechanicShop(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		if(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + MechanicShop.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		MechanicShop esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new MechanicShop (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. AddCustomer");
				System.out.println("2. AddMechanic");
				System.out.println("3. AddCar");
				System.out.println("4. InsertServiceRequest");
				System.out.println("5. CloseServiceRequest");
				System.out.println("6. ListCustomersWithBillLessThan100");
				System.out.println("7. ListCustomersWithMoreThan20Cars");
				System.out.println("8. ListCarsBefore1995With50000Milles");
				System.out.println("9. ListKCarsWithTheMostServices");
				System.out.println("10. ListCustomersInDescendingOrderOfTheirTotalBill");
				System.out.println("11. < EXIT");
				
				/*
				 * FOLLOW THE SPECIFICATION IN THE PROJECT DESCRIPTION
				 */
				switch (readChoice()){
					case 1: AddCustomer(esql); break;
					case 2: AddMechanic(esql); break;
					case 3: AddCar(esql); break;
					case 4: InsertServiceRequest(esql); break;
					case 5: CloseServiceRequest(esql); break;
					case 6: ListCustomersWithBillLessThan100(esql); break;
					case 7: ListCustomersWithMoreThan20Cars(esql); break;
					case 8: ListCarsBefore1995With50000Milles(esql); break;
					case 9: ListKCarsWithTheMostServices(esql); break;
					case 10: ListCustomersInDescendingOrderOfTheirTotalBill(esql); break;
					case 11: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice
	
	public static void AddCustomer(MechanicShop esql){//1

	try {
			String add_cust = "INSERT INTO Customer(id, fname, lname, phone, address)\nVALUES (";
			System.out.print("\tEnter id: ");
			String input = in.readLine();
			add_cust += input + ", ";

			System.out.print("\tEnter fname: ");
			input = in.readLine();
			add_cust += input + ", ";

			System.out.print("\tEnter lname: ");
			input = in.readLine();
			add_cust += input + ", ";

			System.out.print("\tEnter phone: ");
			input = in.readLine();
			add_cust += input + ", ";

			System.out.print("\tEnter address: ");
			input = in.readLine();
			add_cust += input + ")";

			esql.executeQueryAndPrintResult(add_cust);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	public static void AddMechanic(MechanicShop esql){//2

	try {
			String add_mechanic = "INSERT INTO Mechanic(id, fname, lname, experience)\nVALUES (";
			System.out.print("\tEnter id: ");
			String input = in.readLine();
			add_mechanic += input + ", ";

			System.out.print("\tEnter fname: ");
			input = in.readLine();
			add_mechanic += input + ", ";

			System.out.print("\tEnter lname: ");
			input = in.readLine();
			add_mechanic += input + ", ";

			System.out.print("\tEnter experience: ");
			input = in.readLine();
			add_mechanic += input + ")";

			esql.executeQueryAndPrintResult(add_mechanic);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	public static void AddCar(MechanicShop esql){//3

	try {
			String add_car = "INSERT INTO Car(vin, make, model, year)\nVALUES (";
			System.out.print("\tEnter vin: ");
			String input = in.readLine();
			add_car += input + ", ";

			System.out.print("\tEnter make: ");
			input = in.readLine();
			add_car += input + ", ";

			System.out.print("\tEnter model: ");
			input = in.readLine();
			add_car += input + ", ";

			System.out.print("\tEnter year: ");
			input = in.readLine();
			add_car += input + ")";

			esql.executeQueryAndPrintResult(add_car);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
		
	}
	
	public static void InsertServiceRequest(MechanicShop esql){//4
		
	}
	
	public static void CloseServiceRequest(MechanicShop esql) throws Exception{//5
		
	}
	
	public static void ListCustomersWithBillLessThan100(MechanicShop esql){//6
		try {
			String cust_LT_100 = "SELECT Closed_Request.date, Closed_Request.comment, Closed_Request.bill FROM Closed_Request WHERE Closed_Request.bill < 100;";
			esql.executeQueryAndPrintResult(cust_LT_100);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListCustomersWithMoreThan20Cars(MechanicShop esql){//7
		try {
			String more_than_20 = "SELECT c.fname, c.lname FROM (SELECT Owns.customer_id, COUNT(customer_id) as num FROM Customer, Owns, Car ";
			more_than_20 += "WHERE Owns.customer_id = Customer.id AND Owns.car_vin = Car.vin GROUP BY customer_id) gt20, Customer c WHERE c.id = gt20.customer_id AND gt20.num > 20;";
			esql.executeQueryAndPrintResult(more_than_20);
		}	
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListCarsBefore1995With50000Milles(MechanicShop esql){//8
		try {
			String car_md_mk_yr = "SELECT c.make, c.model, c.year ";
			car_md_mk_yr += "FROM Car c, Service_Request s ";
			car_md_mk_yr += "WHERE c.year < 1995 AND c.vin = s.car_vin AND s.odometer < 50000;";
			esql.executeQueryAndPrintResult(car_md_mk_yr);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}	
	}
	
	public static void ListKCarsWithTheMostServices(MechanicShop esql){//9
		//
		//Dummy function to test addition
		try {
			String testAdd = "SELECT * FROM Customer c WHERE c.fname = 'Bob';";
			esql.executeQueryAndPrintResult(testAdd);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	public static void ListCustomersInDescendingOrderOfTheirTotalBill(MechanicShop esql){//9
		//
		try {
			String highestBill = "SELECT c.fname, c.lname, SUM(closed.bill) as cbal ";
			highestBill += "FROM Customer c, Closed_Request closed, Service_Request s ";
			highestBill += "WHERE c.id = s.customer_id AND s.rid = closed.rid ";
			highestBill += "GROUP BY c.fname, c.lname ORDER BY cbal Desc;";
			esql.executeQueryAndPrintResult(highestBill);
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
}
