public class PASPlatformProperties {
	String AWSAccessKeyID;
  	String ConjurAccount;
        String HostName;
        String ApplianceURL;

    public void print() {
	System.out.println( "  ConjurAccount: " + this.ConjurAccount);
	System.out.println( "  HostName: " + this.HostName);
	System.out.println( "  ApplianceURL: " + this.ApplianceURL);
	System.out.println( "  AWSAccessKeyID: " + this.AWSAccessKeyID);
    };
}
