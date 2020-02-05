public class AccessRequest {
    String requestor;
    String projectName;

    String vaultName;
    String cpmName;
    String lobName;
    String safeName;
    Identity[] identities;
    AccountRequest[] accountRequests;

    public void print() {
	System.out.println("Access Request ==================");
	System.out.println( "requestor: " + this.requestor);
	System.out.println( "projectName: " + this.projectName);

	System.out.println( "vaultName: " + this.vaultName);
	System.out.println( "cpmName: " + this.cpmName);
	System.out.println( "lobName: " + this.lobName);
	System.out.println( "safeName: " + this.safeName);
	System.out.println( "identities: ");
	for(Integer i=0; i<this.identities.length; i++) {
	    this.identities[i].print();
	}
	System.out.println( "accountRequests: ");
	for(Integer i=0; i<this.accountRequests.length; i++) {
	    this.accountRequests[i].print();
	    System.out.println("");
	}
	System.out.println("=================================");
    };
}
