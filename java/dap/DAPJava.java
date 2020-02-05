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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class DAPJava {

  /******************************************************************
   * 			PUBLIC MEMBERS
   *
   * void initJavaKeyStore(file,password) - opens Java key store containing server cert
   * void initConnection(url,account) - sets private members for appliance URL and account 
   * void getHealth() - basic DAP health check
   * String authnLogin(uname,password) - Logs in human user with password, returns user's API key 
   * void authenticate(name,apikey) - authenticates with API key, sets private access token member
   * void setAccessToken(token) - sets private access token member, use with authn-k8s
   * String search(searchstr) - returns json array for variables where id or annotations match searchstr
   * String variableValue(varname) - gets variable value by name using private members
   * void loadPolicy(method,branchId,policyText) - loads policy text at branchId using method
   *
   ******************************************************************/

    public static Boolean DEBUG=false;

    // ===============================================================
    // void initJavaKeyStore() - opens Java key store containing server cert
    //
    public static void initJavaKeyStore(String _jksFile, String _jksPassword) {
	  System.setProperty("javax.net.ssl.trustStore", _jksFile);
	  System.setProperty("javax.net.ssl.trustStorePassword", _jksPassword);
	  System.setProperty("javax.net.ssl.trustStoreType", "JKS");
    }

    // ===============================================================
    // void initConnection() - sets private appliance URL and account members
    //
    public static void initConnection(String _applianceUrl, String _account) {
	dapApplianceUrl = _applianceUrl;
	dapAccount = _account;
    }

    // ===============================================================
    // void getHealth() - basic health check
    //
    public static void getHealth() {
	System.out.println( JavaREST.httpGet(dapApplianceUrl + "/health", "") );
    }

    // ===============================================================
    // String authnLogin() - Logs in human user with password, returns user's API key 
    //
    public static String authnLogin(String _user, String _password) {
	String authHeader = "Basic " + base64Encode(_user + ":" + _password);
	String requestUrl = dapApplianceUrl
				+ "/authn/" + dapAccount + "/login";
	String authnApiKey = JavaREST.httpGet(requestUrl, authHeader);
  	if(DAPJava.DEBUG) {
	    System.out.println("API key: " + authnApiKey);
	}
	return authnApiKey;
    }

    // ===============================================================
    // void authenticate() - authenticates with API key, sets private access token member
    //
    public static void authenticate(String _authnLogin, String _apiKey) {
	String requestUrl = dapApplianceUrl;
	try {
	    requestUrl = requestUrl + "/authn/" + dapAccount + "/" 
				+ URLEncoder.encode(_authnLogin, "UTF-8")+ "/authenticate";
  	    if(DAPJava.DEBUG) {
  	 	System.out.println("Authenticate requestUrl: " + requestUrl);
	    }
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}

	String rawToken = JavaREST.httpPost(requestUrl, _apiKey, "");
  	if(DAPJava.DEBUG) System.out.println("Raw token: " + rawToken);
	dapAccessToken = base64Encode(rawToken);
	if(DAPJava.DEBUG) System.out.println("Access token: " + dapAccessToken);
    }

    // ===============================================================
    // void setAccessToken() - sets private access token member, use with authn-k8s
    //
    public static void setAccessToken(String _rawToken) {
	dapAccessToken = base64Encode(_rawToken);
    }

    // ===============================================================
    // String search() - returns json array for variables where id or annotations match searchStr
    //
    public static String search(String _searchStr) {
	String authHeader = "Token token=\"" + dapAccessToken + "\"";
	String requestUrl = dapApplianceUrl
				+ "/resources/" + dapAccount + "?kind=variable" 
				+ "&search=" + _searchStr.replace(" ","%20");
	if(DAPJava.DEBUG) System.out.println("Search request: " + requestUrl);
  	return JavaREST.httpGet(requestUrl, authHeader);
    }

    // ===============================================================
    // String variableValue() - gets variable value by name using private members
    //
    public static String variableValue(String _varId) {
	String authHeader = "Token token=\"" + dapAccessToken + "\"";
	String requestUrl = dapApplianceUrl;
	try {
	    // Java URLEncoder encodes a space as + instead of %20 - DAP REST doesn't accept +
	    requestUrl = requestUrl + "/secrets/" + dapAccount 
				+ "/variable/" 
				+ URLEncoder.encode(_varId, "UTF-8").replace("+","%20");
  	    if(DAPJava.DEBUG) System.out.println("Variable requestUrl: " + requestUrl);
	} catch (UnsupportedEncodingException e) {
		e.printStackTrace();
	}
	return JavaREST.httpGet(requestUrl, authHeader);
    }

    // ===============================================================
    // void loadPolicy() - loads policy at a given branch using specfied method
    //
    public static void loadPolicy(String _method, String _branchId, String _policyText) {
	String authHeader = "Token token=\"" + dapAccessToken + "\"";
	String requestUrl = dapApplianceUrl + "/policies/" + dapAccount + "/policy/" + _branchId;
	switch(_method) {
	    case "delete":
		if(DEBUG) {
		    System.out.println("loadPolicy:");
		    System.out.println("  requestUrl: " + requestUrl);
		    System.out.println("  method: delete/patch");
		    System.out.println("policyText:");
		    System.out.println(_policyText);
		    System.out.println("");
		}
		JavaREST.httpPatch(requestUrl, _policyText, authHeader);
		break;
	    case "replace":
		System.out.println("\"replace/put\" policy load method not implemented.");
		break;
	    default:
		if(DEBUG) {
		    System.out.println("loadPolicy:");
		    System.out.println("  requestUrl: " + requestUrl);
		    System.out.println("  method: append/post");
		    System.out.println("policyText:");
		    System.out.println(_policyText);
		    System.out.println("");
		}
		JavaREST.httpPost(requestUrl, _policyText, authHeader);
	} // switch
    } // loadPolicy

  /******************************************************************
   * 			PRIVATE MEMBERS
   ******************************************************************/

    private static String dapApplianceUrl;;
    private static String dapAccount;
    private static String dapAccessToken;

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

} // DAPJava
