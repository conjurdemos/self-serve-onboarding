# self-serve-onboarding

#### End-users want access to secrets without a lot of hassle.

#### Security teams don't want a lot of extra work, but they can't sacrifice good security hygiene.

#### This prototype is a step towards satisfying both. 

The workflow driver is a json record (access-request.json) that defines parameters for a software project's read-only access to a CyberArk PAS Safe and its accounts. The access request has several parts:
 - Vault, LOB member, Safe and project names
 - identities - an array of new or existing Conjur application identities in "!host foo" format.
 - accountRequests - an array of account parameters to use in provisioning accounts in a new safe.

The request is processed as follows:
 - If the safe named in the request does not exist, it is created, the LOB member added, and the requested accounts created. If the safe exists, no new accounts are created.
 - A Conjur policy is applied to enable the Vault-Conjur Synchronizer (VCS) to push the accounts to Conjur/DAP. Note that the secrets will appear in Conjur on the next run of the Synchronizer.
 - A Conjur policy is applied to grant a "consumer" role to the requested identities such that they have read-only access to the synchronized secrets.
 - A Conjur policy is applied for the application identities, which creates them if they don't exist.


To run the demo:
 1) update onboard-demo.config with the Conjur and PAS connection details for your environment.
 2) run the build.sh script to compile the java code under java/
 3) edit access-request.json with appropriate values - or just use the ones provided.
 4) run ./grant to process the access request
 5) run ./revoke to revoke access to secrets for the identities in the access request.
 6) run ./dynamic-access to effect temporary access to secrets.
 7) run ./deleteid to completely delete the application identities.
