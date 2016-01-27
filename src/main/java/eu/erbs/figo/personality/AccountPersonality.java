package eu.erbs.figo.personality;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import me.figo.FigoConnection;
import me.figo.FigoException;
import me.figo.FigoSession;
import me.figo.internal.TokenResponse;
import me.figo.models.Account;
import me.figo.models.Payment;
import me.figo.models.Security;
import me.figo.models.Transaction;


public class AccountPersonality{

	private final static String TRANSACTIONS_TABLE_FORMAT = "%8.2f %15s %23s %10tB %2te, %tY %80s %n";
	private final static String SECURITY_TABLE_FORMAT = "%-3s %7.2f %6d %9.2f %9.2f %45s %n";

		private final static boolean USE_DEMO = true;

	public static void main(String[] args) throws FigoException, IOException {
		FigoSession session;

		Properties properties = loadProperties();

		if(USE_DEMO){
			//Demo Session 
			session = new FigoSession("ASHWLIkouP2O6_bgA2wWReRhletgWKHYjLqDaqb0LFfamim9RjexTo22ujRIP_cjLiRiSyQXyt2kM1eXU2XLFZQ0Hro15HikJQT_eNeT_9XQ");			
		}
		else{
			FigoConnection connection = new FigoConnection(properties.getProperty("CLIENT_ID"), properties.getProperty("CLIENT_SECRET"), "www.erbs.eu");
			TokenResponse response = connection.credentialLogin(properties.getProperty("FIGO_USER"), properties.getProperty("FIGO_PASSWORD"));
			session = new FigoSession(response.getAccessToken());
		}


		printComments(session);

//				printInformation(session);
	}

	private static Properties loadProperties() throws IOException {
		Properties properties = new Properties();
		InputStream input = null;

		input = new FileInputStream("src/main/resources/figo.properties");
		properties.load(input);

		return properties;
	}

	private static void printComments(FigoSession session) throws FigoException, IOException {
		List<String> comments = new ArrayList<String>();
		for (Account account : session.getAccounts()) {
			for (Transaction transaction : session.getTransactions(session.getAccount(account.getAccountId()))) {
				if(transaction.getBookingText() != null && transaction.getBookingText().equals("Überweisung") && transaction.getPurposeText() != null){
					comments.add(filter(transaction.getPurposeText()));
				}
			}
		}
		System.out.println(String.join("\n", comments));
	}

	private static String filter(String purposeText) {
		purposeText = purposeText
				.replaceAll("Svwz\\+"," ")
				.replaceAll("Eref\\+"," ");
		return purposeText;
	}

	private static void printInformation(FigoSession session) throws FigoException, IOException {

		Calendar c = Calendar.getInstance();

		System.out.println("---------Payments---------");
		for(Payment payment : session.getPayments()){
			c.setTime(payment.getCreationTimestamp());
			System.out.format(TRANSACTIONS_TABLE_FORMAT, payment.getAmount(), payment.getType(), payment.getAccountId(), c, c, c, payment.getPurpose());
		}
		System.out.println();

		//		// print out the list of all transactions on a specific account
		for (Account account : session.getAccounts()) {
			System.out.println(account.getAccountId() + "\t" + session.getAccountBalance(account).getBalance() + "\t" + account.getName());
			System.out.println("   ---Transactions---");
			for (Transaction transaction : session.getTransactions(session.getAccount(account.getAccountId()))) {
				c.setTime(transaction.getBookingDate());
				System.out.format(TRANSACTIONS_TABLE_FORMAT, transaction.getAmount(), transaction.getType(), transaction.getBookingText(), c, c, c, transaction.getPurposeText());
			}
			System.out.println();
		}
		System.out.println();

		System.out.println("---------Securities---------");
		for(Security security : session.getSecurities()){
			System.out.format(SECURITY_TABLE_FORMAT, security.getCurrency(), security.getPrice(), security.getQuantity(), security.getPurchasePrice(), security.getAmount(), security.getName());
		}
	}
}