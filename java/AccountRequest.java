public class AccountRequest {
    String accountName;
    String platformId;
    String address;
    String userName;
    String secretType;
    String secretValue;

    public void print() {
	System.out.println( "  accountName: " + this.accountName);
	System.out.println( "  platformId: " + this.platformId);
	System.out.println( "  address: " + this.address);
	System.out.println( "  userName: " + this.userName);
	System.out.println( "  secretType: " + this.secretType);
	System.out.println( "  secretValue: " + this.secretValue);
    };
}
