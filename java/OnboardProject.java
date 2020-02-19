/*
 * OnBoard a new development project from an access request record.
 * - create safe in EPV
 * - create accounts in safe
 * - create identity for project
 * - apply policy for project
 */

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.security.NoSuchAlgorithmException;
import java.security.KeyManagementException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;
import java.util.Random;
import com.google.gson.Gson;

public class OnboardProject {

    public static Boolean DEBUG=false;

    public static void main(String[] args) {

	// set to true to enable debug output
	PASJava.DEBUG=false;
	DAPJava.DEBUG=false;
	JavaREST.DEBUG=false;
	OnboardProject.DEBUG=false;

	// turn off all cert validation - FOR DEMO ONLY
	disableSSL(); 

	// Initialize connection to PAS
	PASJava.initConnection( System.getenv("PAS_IIS_SERVER_IP") );
	PASJava.logon(	System.getenv("PAS_ADMIN_NAME"), System.getenv("PAS_ADMIN_PASSWORD") );

	processAccessRequest( getAccessRequest() );

    } // main()

    // ==========================================
    // getAccessRequest()
    //
    public static AccessRequest getAccessRequest() {
	String inputJson = System.getenv("ACCESS_REQUEST_JSON_FILE");

	if (OnboardProject.DEBUG) {
	  System.out.println("Input filename:" + inputJson);
        }

	String ARjson = "";
	try
	{
            ARjson = new String ( Files.readAllBytes( Paths.get(inputJson) ) );
	} 
	catch (IOException e) 
	{
	    e.printStackTrace();
	}
        Gson gson = new Gson();
        AccessRequest newAR = (AccessRequest) gson.fromJson( ARjson, AccessRequest.class );

	if (OnboardProject.DEBUG) {
	  newAR.print();
        }

	return newAR;

    } // getAccessRequest

    // ==========================================
    // processAccessRequest(accessRequest)
    //
    public static void processAccessRequest(AccessRequest _accessRequest) {

	PASSafe newSafe = addSafe(_accessRequest);// add Safe if it doesn't exist
	if (newSafe != null) {
	    addAccounts(_accessRequest);	// provision accounts in new safe
	}
	addMembers(_accessRequest);	// add LOB and Administrator members
	applyPolicy(_accessRequest);	// Apply Vault/Conjur sync policies

	addIdentities(_accessRequest);

    } // processAccessRequest

    // ==========================================
    // addSafe(accessRequest)
    //
    public static PASSafe addSafe(AccessRequest _accessRequest) {
	return PASJava.addSafe(_accessRequest.safeName, _accessRequest.cpmName);
    } // addSafe

    // ==========================================
    // addMembers(accessRequest)
    //
    public static void addMembers(AccessRequest _accessRequest) {
	// add safe members, with appropriate access privileges
	PASJava.addSafeMember(_accessRequest.safeName, _accessRequest.lobName);
	PASJava.addSafeMember(_accessRequest.safeName, "Administrator");
    } // addSafe

    // ==========================================
    // addAccounts(accessRequest)
    //
    public static void addAccounts(AccessRequest _accessRequest) {
	for(Integer i=0; i<_accessRequest.accountRequests.length; i++) {
	    if (OnboardProject.DEBUG) {
	        System.out.println("Creating account:\n"
			 + "  Account: " + _accessRequest.accountRequests[i].accountName + "\n"
			 + "  Platform ID: " + _accessRequest.accountRequests[i].platformId);
	    }
 	    _accessRequest.accountRequests[i].secretValue = getRandomHexString(16);
 	    PASAccount pasAccount = PASJava.addAccount(_accessRequest.safeName,
						_accessRequest.accountRequests[i].accountName,
						_accessRequest.accountRequests[i].platformId,
						_accessRequest.accountRequests[i].address,
						_accessRequest.accountRequests[i].userName,
						_accessRequest.accountRequests[i].secretType,
						_accessRequest.accountRequests[i].secretValue);
	}
    } //addAccounts

    // ==========================================
    // applyPolicy(accessRequest)
    //
    public static void applyPolicy(AccessRequest _accessRequest) {
	String _vaultName = _accessRequest.vaultName;
	String _lobName = _accessRequest.lobName;
	String _safeName = _accessRequest.safeName;

	if (OnboardProject.DEBUG) {
            System.out.println("Preloading sync policy:\n"
			 + "  Vault name: " + _vaultName + "\n"
			 + "  LOB name: " + _lobName + "\n"
			 + "  Safe name: " +_safeName);
	}

        DAPJava.initConnection(
                                System.getenv("CONJUR_APPLIANCE_URL"),
                                System.getenv("CONJUR_ACCOUNT")
                                );
        String userApiKey = DAPJava.authnLogin(
                                System.getenv("CONJUR_ADMIN_USERNAME"),
                                System.getenv("CONJUR_ADMIN_PASSWORD")
                                );
        DAPJava.authenticate(
                                System.getenv("CONJUR_ADMIN_USERNAME"),
                                userApiKey
                                );

	// generate policy - REST method accepts text - no need to create a file
        String policyText = "---\n"
                            + "- !policy\n"
                            + "  id: " + _vaultName + "\n"
                            + "  body:\n"
                            + "  - !group " + _lobName + "-admins\n"
                            + "  - !policy\n"
                            + "    id: " + _lobName + "\n"
                            + "    owner: !group /" + _vaultName + "/" + _lobName + "-admins\n"
                            + "    body:\n"
                            + "    - !group " + _safeName + "-admins\n"
                            + "    - !policy\n"
                            + "      id: " + _safeName + "\n"
                            + "      body:\n"
                            + "      - !policy\n"
                            + "        id: delegation\n"
                            + "        owner: !group /" + _vaultName + "/" + _lobName + "/" + _safeName + "-admins\n"
                            + "        body:\n"
                            + "        - !group consumers\n";

        // load policy using default "append" method 
        DAPJava.loadPolicy("append", "root", policyText);

    } // applyPolicy()

    // ==========================================
    // addIdentities(accessRequest)
    //
    public static void addIdentities(AccessRequest _accessRequest) {
	// create "- !xxx yyy" yaml string and apply it
	for(Integer i=0; i<_accessRequest.identities.length; i++) {
          DAPJava.loadPolicy("append", "root", "- " + _accessRequest.identities[i].identity);
	}
    } // addIdentities

/*********************************************************
 *********************************************************
 **                    PRIVATE MEMBERS			**
 *********************************************************
 *********************************************************/

    // ==========================================
    // void disableSSL()
    //   from: https://nakov.com/blog/2009/07/16/disable-certificate-validation-in-java-ssl-connections/
    //
    private static void disableSSL() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
        };
 
        // Install the all-trusting trust manager
	try {
	        SSLContext sc = SSLContext.getInstance("SSL");
        	sc.init(null, trustAllCerts, new java.security.SecureRandom());
        	HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
	} catch(NoSuchAlgorithmException e) {
		e.printStackTrace();
	} catch(KeyManagementException e) {
		e.printStackTrace();
	}

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
 
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

    } // disableSSL
 
    // ==========================================
    // void getRandomHexString()
    //
    private static String getRandomHexString(int numchars){
        Random r = new Random();
        StringBuffer sb = new StringBuffer();
        while(sb.length() < numchars){
            sb.append(String.format("%08x", r.nextInt()));
        }

        return sb.toString().substring(0, numchars);
    }

} // OnboardProject
