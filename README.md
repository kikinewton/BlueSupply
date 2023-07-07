### Technical Debt:
When faced with the error <br/> 
`Unable to instantiate class [org.bouncycastle.jce.provider.BouncyCastleProvider]
io.jsonwebtoken.lang.InstantiationException: Unable to instantiate class [org.bouncycastle.jce.provider.BouncyCastleProvider]` 
<br/> 

* Navigate to the 
Project Structure -> Libraries
and delete all dependencies with tag _org.bouncycastle_

* Run ``` mvn clean``` then ```mvn dependency:purge-local-repository```


# Manual
The system can be used to make 3 types of requests:
* LPO request - Goods request, Service Request, Project & Works (involves GRN)
* Float order - This request HOD approval
* Petty cash - Use your money and the company reimburses


## LPO request flow
1. User makes a request, selects the user department and receiving store
2. The HOD of the user department endorses or cancel the request
3. Endorsed request are sent to procurement for processing
4. The procurement generates Request For Quotation document (RFQ) and shares with selected suppliers for the request item
5. The suppliers respond to RFQ by sending their invoices
6. The procurement attaches the quotation to the related supplier and request item


## Petty Cash
1. User creates a request
2. Request is sent to HOD of user 
3. 
