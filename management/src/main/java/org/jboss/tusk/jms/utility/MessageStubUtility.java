package org.jboss.tusk.jms.utility;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;

/**
 * The MessageStubUtility assembles test messages for the Tusk application.  The stubMessage operation performs
 * the work necessary to build the list of test messages.
 * 
 * @author cabynum (Original author is likely Brad Davis - bdavis@redhat.com)
 *
 */
public class MessageStubUtility {

	private static final String[] firstNames = {"Brad", "Mike", "Luke", "Jamie", "Nathan", "Tim", "Dave", "Lee", "Bruce", "Mike"};
	private static final String[] lastNames = {"Brad", "Mike", "Luke", "Jamie", "Nathan", "Tim", "Dave", "Lee", "Bruce", "Mike"};
	private static final String[] street = {"Holmes Drive","Hightower Road","Aaron Drive","Capitol Avenue","Williams Drive","Simpson Street","Brawley Drive","Chestnut Street","Hill Drive","Butler Street","Portman Boulevard","Harris Street","Dobbs Avenue","Houston Street","Boone Boulevard","Simpson Street","Lowery Boulevard","Ashby Street","Maiden Lane","Grove Street","MLK Drive","Hunter Street","Memorial Drive","Fair Street","Metropolitan Parkway","Stewart Avenue","Monroe Drive","N. Boulevard","Park Avenue","Foundry Street","Peachtree Center Avenue","Ivy Street","Peachtree Street","Whitehall Street","Abernathy Boulevard","Gordon Street","McGill Boulevard","Forrest Avenue","Marcus Boulevard","Marian Road","Spring Street","Madison Avenue","Thompson Street","Raymond Street","Trinity Avenue","Peters Street","Washington Street","South Collins Street","Borders Drive","Yonge Street"};
	private static final String[] city = {"Alachua","Briny Breezes","Bristol","Bronson","Brooker","Cooper City","Coral Gables","Daytona Beach Shores","DeBary","Deerfield Beach","DeFuniak Springs","DeLand","Delray Beach","Deltona","Destin","Doral","Dundee","Ebro","Edgewater","Edgewood","Fort Lauderdale","Fanning Springs","Fellsmere","Fernandina Beach","Fort Pierce","Fort Walton Beach","Fort White","Freeport","Frostproof","Greenville","Greenwood","Gretna","Groveland","Gulf Breeze","Gulf Stream","Hillsboro Beach","Holly Hill","Hollywood","Jacksonville","Jacob City","Jasper"};
	private static final String[] state = {"Alabama","Alaska","American Samoa","Arizona","Arkansas","California","Colorado","Connecticut","Delaware","District of Columbia","Florida","Georgia","Guam","Hawaii","Idaho","Illinois","Indiana","Iowa","Kansas","Kentucky","Louisiana","Maine","Maryland","Massachusetts","Michigan","Minnesota","Mississippi","Missouri","Montana","Nebraska","Nevada","New Hampshire","New Jersey","New Mexico","New York","North Carolina","North Dakota","Northern Marianas Islands","Ohio","Oklahoma","Oregon","Pennsylvania","Puerto Rico","Rhode Island","South Carolina","South Dakota","Tennessee","Texas","Utah","Vermont","Virginia","Virgin Islands","Washington","West Virginia","Wisconsin","Wyoming"};
	private static final String[] zips = {"86001","93001","72001","81001","90001","95002","72002","90002","94002","06002","96002","80002","96003","80003","72003","85003","81003","95003","93003","36003","90003","92003","81004","80004"};

	private String messageTemplate;
	
	/**
	 * The MessageStubUtility constructor uses a pre-defined template to build the messages to send to
	 * Tusk.
	 * 
	 * @throws IOException
	 */
	public MessageStubUtility() throws IOException {
		InputStream in = this.getClass().getClassLoader().getResourceAsStream("xml/person-template.xml");
		messageTemplate = IOUtils.toString(in);
	}
	
	/**
	 * The stubMessages operation is used to retrieve a list of stubbed out messages from this MessageStubUtility
	 * class.  The caller may specify the number of messages to be stubbed out.
	 * 
	 * @param number
	 * @return
	 */
	public List<String> stubMessages(int number) {
		List<String> stubbed = new ArrayList<String>();
		
		for(int i=0; i<number; i++) {
			stubbed.add(stubMessage());
		}
		
		return stubbed;
	}
	
	/**
	 * The stubMessage operation builds a message structured based off of the template specified in the 
	 * MessageStubUtility constructor.  The contents of each message is ensured to be unique through the use
	 * of RandomUtils on each property of the message.
	 * 
	 * @return
	 */
	public String stubMessage() {
		String msg = StringUtils.replace(messageTemplate, "${bigdata-index}", Integer.toString(RandomUtils.nextInt(10000)));
		msg = StringUtils.replace(msg, "${bigdata-first-name}", firstNames[RandomUtils.nextInt(firstNames.length)]);
		msg = StringUtils.replace(msg, "${bigdata-last-name}", lastNames[RandomUtils.nextInt(lastNames.length)]);
		msg = StringUtils.replace(msg, "${bigdata-address-line-one}", (100+RandomUtils.nextInt(1000)) + " " + street[RandomUtils.nextInt(street.length)]);
		msg = StringUtils.replace(msg, "${bigdata-city}", city[RandomUtils.nextInt(city.length)]);
		msg = StringUtils.replace(msg, "${bigdata-state}", state[RandomUtils.nextInt(state.length)]);
		msg = StringUtils.replace(msg, "${bigdata-zip-code}", zips[RandomUtils.nextInt(zips.length)]);

		return msg;
	}
	
	
}
