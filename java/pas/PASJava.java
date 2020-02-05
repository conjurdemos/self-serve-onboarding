import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;
import java.io.UnsupportedEncodingException;
import com.google.gson.Gson;

/******************************************************************
 ******************************************************************
			PUBLIC STATIC METHODS
  Initialization:
  - void initConnection(serverHost)
  - void logon(username,password)

  Platforms:
  - PASPlatform[] getPlatforms(filter)

  Safes:
  - PASSafe addSafe(safeName,cpmName)
  - PASSafe[] listSafes()
  - void addSafeMember(safeName,memberName)
  - void deleteSafe(safeName)

  Accounts:
  - PASAccount addAccount(safeName,accountName,platformId,address,userName,secretType,secretValue) 
  - PASAccount[] getAccounts(safeName)
  - PASAccountDetail[] getAccountDetails(keyWords,safeName)
  - PASAccountGroup[] getAccountGroups(safeName)
  - PASAccountGroupMember[] getAccountGroupMembers(safeName)

******************************************************************
******************************************************************/

public class PASJava {

     public static Boolean DEBUG=false; // set to true for debug output


/*****************************************************************
 *****************************************************************
 **			     INITIALIZATION			**
 *****************************************************************
 *****************************************************************/

    // ===============================================================
    // void initConnection() - initializes base server URL
    //
    public static void initConnection(String _pasServerHost) {
	pasServerUrl = "https://" + _pasServerHost + "/PasswordVault/API";

	// cuz sometimes the old way is the only way to get what you want
	pasServerUrlClassic = "https://" + _pasServerHost + "/PasswordVault/WebServices/PIMServices.svc";

    } // initConnection


    // ===============================================================
    // logon(username,password) - logs user in and sets session token
    //
    public static void logon(String _user, String _password) {

	String requestUrl = pasServerUrl + "/auth/Cyberark/Logon";
	String bodyContent = "{"
				+ "\"username\":\"" + _user + "\","
				+ "\"password\":\"" + _password + "\""
			   + "}";

	// get session token and save w/o double quotes
	pasSessionToken = JavaREST.httpPost(requestUrl, bodyContent, "").replace("\"","");

	if(PASJava.DEBUG) {
	    System.out.println("");
	    System.out.println("====== PASJava.login() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("bodyContent: " + bodyContent);
	    System.out.println("sessionToken: " + pasSessionToken);
	    System.out.println("=============================");
	    System.out.println("");
	}

    } //logon

/*****************************************************************
 *****************************************************************
 **			     	PLATFORMS			**
 *****************************************************************
 *****************************************************************/

    // ===============================================================
    // PASPlatform[] getPlatforms(filter)
    //
    public static PASPlatform[] getPlatforms(String _filter) {
        String requestUrl = pasServerUrl + "/Platforms";
        String authHeader = pasSessionToken;

	if(_filter != null) {
	  requestUrl = requestUrl + "?" + _filter;
	}

        if(PASJava.DEBUG) {
	    System.out.println("requestUrl: " + requestUrl);
	}

        String platformOutput = JavaREST.httpGet(requestUrl, authHeader);

        if(PASJava.DEBUG) {
            System.out.println("Raw platform listing:");
            System.out.println(platformOutput);
            System.out.println("");
        }

        // parse account json output into PASSafeList
        Gson gson = new Gson();
        PASPlatformList pasPlatformList = (PASPlatformList) gson.fromJson( platformOutput, PASPlatformList.class );

        if(PASJava.DEBUG) {
            System.out.println("PAS Platform List =====");
            pasPlatformList.print();
            System.out.println("======================");
            System.out.println("");
        }

	return pasPlatformList.Platforms;

    } // getPlatforms

/*****************************************************************
 *****************************************************************
 **			     	SAFES				**
 *****************************************************************
 *****************************************************************/

    // ===============================================================
    // PASSafe addSafe(safeName) - creates safe w/ given name
    //
    public static PASSafe addSafe(String _safeName, String _managingCpm) {

	String requestUrl = pasServerUrlClassic + "/Safes";
	String authHeader = pasSessionToken;
	String bodyContent = "{"
				+ "\"safe\":{"
				+ "\"SafeName\":\"" + _safeName + "\","
				+ "\"Description\":\"Safe created with Java\","
				+ "\"OLACEnabled\":false,"
				+ "\"ManagingCPM\":\"" + _managingCpm + "\","
				+ "\"NumberOfDaysRetention\":1"
			     + "}"
			   + "}";

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.addSafe() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("bodyContent: " + bodyContent);
	    System.out.println("===================================");
	    System.out.println("");
	}

	String addSafeOutput = JavaREST.httpPost(requestUrl, bodyContent, authHeader);
	if (addSafeOutput == null) {
	    return null;
	}

	if(PASJava.DEBUG) {
	    System.out.println("Raw addSafe output:");
	    System.out.println(addSafeOutput);
	    System.out.println("");
	}

        // parse account json output into PASAccountList
        Gson gson = new Gson();
        PASSafeAdd pasSafeAdd = (PASSafeAdd) gson.fromJson( addSafeOutput, PASSafeAdd.class );

        if(PASJava.DEBUG) {
            System.out.println("PAS Safe =====");
            pasSafeAdd.print();
            System.out.println("======================");
            System.out.println("");
        }

	return pasSafeAdd.AddSafeResult;

    } // addSafe

    // ===============================================================
    // PASSafe[] listSafes() - produces list of safes in vault
    //
    public static PASSafe[] listSafes() {

	String requestUrl = pasServerUrlClassic + "/Safes";
	String authHeader = pasSessionToken;

        if(PASJava.DEBUG) {
            System.out.println("====== PASJava.listSafes() ======");
            System.out.println("requestUrl: " + requestUrl);
            System.out.println("authHeader: " + authHeader);
            System.out.println("===================================");
            System.out.println("");
        }

        String safeOutput = JavaREST.httpGet(requestUrl, authHeader);

        if(PASJava.DEBUG) {
            System.out.println("Raw safe listing:");
            System.out.println(safeOutput);
            System.out.println("");
        }

        // parse account json output into PASSafeList
        Gson gson = new Gson();
        PASSafeList pasSafeList = (PASSafeList) gson.fromJson( safeOutput, PASSafeList.class );

        if(PASJava.DEBUG) {
            System.out.println("PAS Safe List =====");
            pasSafeList.print();
            System.out.println("======================");
            System.out.println("");
        }

        return pasSafeList.GetSafesResult;
 
    } // listSafes

    // ===============================================================
    // void addSafeMember(safeName,memberName) 
    // Description: adds existing member to safe with LOBUser permissions
    //
    public static void addSafeMember(String _safeName, String _memberName) {

	String requestUrl = pasServerUrlClassic + "/Safes/" + _safeName + "/Members";
	String authHeader = pasSessionToken;
/*
	String bodyContent = "{"
  				+ "\"member\":{"
				+ "\"MemberName\":\"" + _memberName + "\","
    				+ "\"Permissions\":["
				+    "{\"Key\":\"UseAccounts\", \"Value\":true},"
				+    "{\"Key\":\"RetrieveAccounts\", \"Value\":true},"
				+    "{\"Key\":\"ListAccounts\", \"Value\":true},"
				+    "{\"Key\":\"AccessWithoutConfirmation\", \"Value\":true}"
				+   "]"
			        + "}"
			   + "}";
*/
	String bodyContent = "{"
  				+ "\"member\":{"
				+ "\"MemberName\":\"" + _memberName + "\","
    				+ "\"Permissions\":["
				+    "{\"Key\":\"UseAccounts\", \"Value\":true},"
				+    "{\"Key\":\"RetrieveAccounts\", \"Value\":true},"
				+    "{\"Key\":\"ListAccounts\", \"Value\":true},"
				+    "{\"Key\":\"AddAccounts\", \"Value\":true},"
				+    "{\"Key\":\"UpdateAccountContent\", \"Value\":true},"
				+    "{\"Key\":\"UpdateAccountProperties\", \"Value\":true},"
				+    "{\"Key\":\"InitiateCPMAccountManagementOperations\", \"Value\":true},"
				+    "{\"Key\":\"SpecifyNextAccountContent\", \"Value\":true},"
				+    "{\"Key\":\"RenameAccounts\", \"Value\":true},"
				+    "{\"Key\":\"DeleteAccounts\", \"Value\":true},"
				+    "{\"Key\":\"UnlockAccounts\", \"Value\":true},"
				+    "{\"Key\":\"ManageSafe\", \"Value\":true},"
				+    "{\"Key\":\"ManageSafeMembers\", \"Value\":true},"
				+    "{\"Key\":\"BackupSafe\", \"Value\":true},"
				+    "{\"Key\":\"ViewAuditLog\", \"Value\":true},"
				+    "{\"Key\":\"ViewSafeMembers\", \"Value\":true},"
				+    "{\"Key\":\"RequestsAuthorizationLevel\", \"Value\":0},"
				+    "{\"Key\":\"AccessWithoutConfirmation\", \"Value\":true},"
				+    "{\"Key\":\"CreateFolders\", \"Value\":true},"
				+    "{\"Key\":\"DeleteFolders\", \"Value\":true},"
				+    "{\"Key\":\"MoveAccountsAndFolders\", \"Value\":true}"
				+   "]"
			        + "}"
			   + "}";

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.addSafeMember() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("bodyContent: " + bodyContent);
	    System.out.println("===================================");
	    System.out.println("");
	}

	String addSafeMemberOutput = JavaREST.httpPost(requestUrl, bodyContent, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw addSafeMember output:");
	    System.out.println(addSafeMemberOutput);
	    System.out.println("");
	}

    } // addSafeMember

    // ===============================================================
    // void deleteSafe(safeName) - deletes safe w/ given name
    //
    public static void deleteSafe(String _safeName) {

        String requestUrl = pasServerUrlClassic + "/Safes/" + _safeName;
        String authHeader = pasSessionToken;

        Integer responseCode = JavaREST.httpDelete(requestUrl, authHeader);

	if(responseCode != 201) {
            System.out.println("Cannot delete PAS Safe \"" + _safeName + "\"."); 
	} else if(PASJava.DEBUG) {
            System.out.println("PAS Safe \"" + _safeName + "\" deleted."); 
	}

    } // deleteSafe

/*****************************************************************
 *****************************************************************
 **			     ACCOUNTS				**
 *****************************************************************
 *****************************************************************/

    // ===============================================================
    // PASAccount addAccount(safeName,accountName,platformId,address,userName,secretType,secretValue) 
    // Description: creates account in safe
    //
    public static PASAccount addAccount(String _safeName,
					String _accountName,
					String _platformId,
					String _address,
					String _userName,
					String _secretType,
					String _secretValue) {

	String requestUrl = pasServerUrl + "/Accounts";
	String authHeader = pasSessionToken;
	String bodyContent = "{"
				+ "\"name\":\"" + _accountName + "\","
				+ "\"address\":\"" + _address + "\","
				+ "\"userName\":\"" + _userName + "\","
				+ "\"platformId\":\"" + _platformId + "\","
				+ "\"safeName\":\"" + _safeName + "\","
				+ "\"secretType\":\"" + _secretType + "\","
				+ "\"secret\":\"" + _secretValue + "\""
			   + "}";

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.addAccount() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("bodyContent: " + bodyContent);
	    System.out.println("===================================");
	    System.out.println("");
	}

	String addAccountOutput = JavaREST.httpPost(requestUrl, bodyContent, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw addAccount output:");
	    System.out.println(addAccountOutput);
	    System.out.println("");
	}

        // parse account json output into PASAccount
        Gson gson = new Gson();
        PASAccount pasAccount= (PASAccount) gson.fromJson( addAccountOutput, PASAccount.class );

        if(PASJava.DEBUG) {
            System.out.println("PAS Account =====");
            pasAccount.print();
            System.out.println("======================");
            System.out.println("");
        }

	return pasAccount;

    } // addAccount

    // ===============================================================
    // PASAccount[] getAccounts(safeName) 
    // Description: returns array of account objects for all accounts in a safe
    //
    public static PASAccount[] getAccounts(String _safeName) {

	String requestUrl = pasServerUrl + "/accounts?filter=safeName%20eq%20" + _safeName;
	String authHeader = pasSessionToken;

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.getAccounts() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("===================================");
	    System.out.println("");
	}

	String accountOutput = JavaREST.httpGet(requestUrl, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw account listing:");
	    System.out.println(accountOutput);
	    System.out.println("");
	}

        // parse account json output into PASAccountList
        Gson gson = new Gson();
        PASAccountList pasAccountList = 
		(PASAccountList) gson.fromJson( accountOutput, PASAccountList.class );

	if(PASJava.DEBUG) {
	    System.out.println("PAS Account List =====");
	    pasAccountList.print();
	    System.out.println("======================");
	    System.out.println("");
	}

	return pasAccountList.value;

    } // getAccounts

    // ===============================================================
    // PASAccountDetail[] getAccountDetails(keywordString,safeName) - 
    // Description: returns array of detail objects for account matching keywords
    //
    public static PASAccountDetail[] getAccountDetails(String _keywordString, String _safeName) {

	String requestUrl = pasServerUrlClassic + "/Accounts?Keywords=" 
				+ _keywordString.replace(" ","%20") + "&Safe=" + _safeName;
	String authHeader = pasSessionToken;

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.getAccountDetails() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("=========================================");
	    System.out.println("");
	}

	String detailOutput = JavaREST.httpGet(requestUrl, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw detail listing:");
	    System.out.println(detailOutput);
	    System.out.println("");
	}

        // parse account json output into PASAccountDetail
        Gson gson = new Gson();
        PASAccountDetailList pasAccountDetails = 
		(PASAccountDetailList) gson.fromJson( detailOutput, PASAccountDetailList.class );

	if(PASJava.DEBUG) {
	    System.out.println("PAS Account Details =====");
	    pasAccountDetails.print();
	    System.out.println("=========================");
	    System.out.println("");
	}

	return pasAccountDetails.accounts;

    } // getAccountDetailss

    // ===============================================================
    // getAccountGroups(safeName) - returns array of all account groups in safe
    //
    public static PASAccountGroup[] getAccountGroups(String _safeName) {

	String requestUrl = pasServerUrl + "/AccountGroups?Safe=" + _safeName;
	String authHeader = pasSessionToken;

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.getAccountGroups() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("========================================");
	    System.out.println("");
	}

	String groupOutput = JavaREST.httpGet(requestUrl, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw json account group output:");
	    System.out.println(groupOutput);
	    System.out.println("");
	}

        // parse account group json output into PASAccountGroup array
        Gson gson = new Gson();
        PASAccountGroup[] pasAccountGroups = 
		(PASAccountGroup[]) gson.fromJson( groupOutput, PASAccountGroup[].class );

	if(PASJava.DEBUG) {
	    System.out.println("PAS Account Groups =====");
	    for(Integer i=0; i < pasAccountGroups.length; i++) {
	        pasAccountGroups[i].print();
	    }
	    System.out.println("========================");
	    System.out.println("");
	}

	return pasAccountGroups;

    } // getAccountGroups

    // ===============================================================
    // getAccountGroupMembers(GroupID) - returns array of all members in account group
    //
    public static PASAccountGroupMember[] getAccountGroupMembers(String _groupId) {

	String requestUrl = pasServerUrl + "/AccountGroups/" + _groupId + "/Members";
	String authHeader = pasSessionToken;

	if(PASJava.DEBUG) {
	    System.out.println("====== PASJava.getAccountGroupMembers() ======");
	    System.out.println("requestUrl: " + requestUrl);
	    System.out.println("authHeader: " + authHeader);
	    System.out.println("==============================================");
	    System.out.println("");
	}

	String memberOutput = JavaREST.httpGet(requestUrl, authHeader);

	if(PASJava.DEBUG) {
	    System.out.println("Raw account group member output:");
	    System.out.println(memberOutput);
	    System.out.println("");
	}

        // parse account group member json output into PASAccountGroupMember array
        Gson gson = new Gson();
        PASAccountGroupMember[] pasAccountGroupMembers = 
		(PASAccountGroupMember[]) gson.fromJson( memberOutput, PASAccountGroupMember[].class );

	if(PASJava.DEBUG) {
	    System.out.println("PAS Account Group Members =====");
	    for(Integer i=0; i < pasAccountGroupMembers.length; i++) {
	        pasAccountGroupMembers[i].print();
	        System.out.println("");
	    }
	    System.out.println("==============================");
	    System.out.println("");
	}

	return pasAccountGroupMembers;

    } // getAccountGroupMembers


/*****************************************************************
 *****************************************************************
 **			PRIVATE MEMBERS				**
 *****************************************************************
 *****************************************************************/

    static private String pasServerUrl;
    static private String pasServerUrlClassic;
    static private String pasSessionToken;

    // ===============================================================
    // String base64Encode() - base64 encodes argument and returns encoded string
    //
    private static String base64Encode(String input) {
	String encodedString = "";
	try {
	    encodedString = Base64.getEncoder().encodeToString(input.getBytes("utf-8"));
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
	  return encodedString;
    } // base64Encode

    // ===============================================================
    // String base64Decode() - base64 decodes argument and returns decoded string
    //
    private static String base64Decode(String input) {
	byte[] decodedBytes = Base64.getDecoder().decode(input);
	return new String(decodedBytes);
    } // base64Decode

} // PASJava
